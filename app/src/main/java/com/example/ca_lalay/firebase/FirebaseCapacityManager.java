package com.example.ca_lalay.firebase;

import androidx.annotation.NonNull;

import com.example.ca_lalay.firebase.callback.AvailableTimesCallback;
import com.example.ca_lalay.firebase.callback.CustomerBookingCallback;
import com.example.ca_lalay.firebase.callback.ShopOwnerDataCallback;
import com.example.ca_lalay.firebase.callback.UpdateBookingCallback;
import com.example.ca_lalay.model.BookingCreationParams;
import com.example.ca_lalay.model.CustomerBookingModel;
import com.example.ca_lalay.model.ShopOwnerModel;
import com.example.ca_lalay.model.TimeSlotOption;
import com.example.ca_lalay.utils.BookingStatusUtils;
import com.example.ca_lalay.utils.BookingTimeUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles capacity-aware slot reservation.
 *
 * Structure added to the database (alongside the existing /bookings node):
 *
 *   shopBookingSlots/{providerId}/{dateISO}/{bookingId}
 *       start:  <long epoch millis>
 *       end:    <long epoch millis>
 *       status: <string booking status>
 *
 * This lightweight per-shop-per-day index is what the Firebase Transaction
 * mutates atomically. It lets many customers race for the same slot safely:
 * Firebase retries the transaction handler locally on write conflicts, so the
 * overlap recount always happens against the latest committed data.
 */
public class FirebaseCapacityManager {

    private static final int SLOT_STEP_MINUTES = 30;

    private static DatabaseReference slotsRootRef() {
        return FirebaseDatabase.getInstance().getReference("shopBookingSlots");
    }

    private static DatabaseReference bookingsRef() {
        return FirebaseDatabase.getInstance().getReference("bookings");
    }

    /**
     * Builds the list of selectable start times for a given shop/date/service duration,
     * marking which ones are already full. This is for display only - the authoritative
     * check happens again, atomically, inside reserveSlotAndCreateBooking.
     */
    public static void getAvailableStartTimes(String providerId, String dateISO, String serviceDurationHoursStr,
                                              AvailableTimesCallback callback) {

        FirebaseDatabaseManager.getShopOwnerData(providerId, new ShopOwnerDataCallback() {
            @Override
            public void onSuccess(ShopOwnerModel shop) {
                final int capacity;
                final long openMillis, closeMillis;
                final int durationMinutes;
                try {
                    capacity = Integer.parseInt(shop.getShopSlot().trim());
                    long[] window = BookingTimeUtils.getOperatingWindowMillis(
                            dateISO, shop.getShopOpenAt(), shop.getShopClosedAt());
                    openMillis = window[0];
                    closeMillis = window[1];
                    durationMinutes = BookingTimeUtils.durationHoursStringToMinutes(serviceDurationHoursStr);
                } catch (ParseException | NumberFormatException | NullPointerException e) {
                    callback.onFailure("Shop hours or capacity are not configured correctly.");
                    return;
                }

                long durationMillis = BookingTimeUtils.minutesToMillis(durationMinutes);
                long stepMillis = BookingTimeUtils.minutesToMillis(SLOT_STEP_MINUTES);

                slotsRootRef().child(providerId).child(dateISO)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot daySnapshot) {
                                List<long[]> activeIntervals = new ArrayList<>();
                                for (DataSnapshot child : daySnapshot.getChildren()) {
                                    Long start = child.child("start").getValue(Long.class);
                                    Long end = child.child("end").getValue(Long.class);
                                    String status = child.child("status").getValue(String.class);
                                    if (start == null || end == null) continue;
                                    if (!BookingStatusUtils.consumesCapacity(status)) continue;
                                    activeIntervals.add(new long[]{start, end});
                                }

                                List<TimeSlotOption> options = new ArrayList<>();
                                for (long start = openMillis; start + durationMillis <= closeMillis; start += stepMillis) {
                                    long end = start + durationMillis;

                                    int overlapCount = 0;
                                    for (long[] interval : activeIntervals) {
                                        if (BookingTimeUtils.rangesOverlap(start, end, interval[0], interval[1])) {
                                            overlapCount++;
                                        }
                                    }

                                    boolean available = overlapCount < capacity;
                                    int remaining = Math.max(0, capacity - overlapCount);

                                    options.add(new TimeSlotOption(
                                            BookingTimeUtils.formatTime(start), start, end, available, remaining));
                                }

                                callback.onSuccess(options);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                callback.onFailure(error.getMessage());
                            }
                        });
            }

            @Override
            public void onFailure(String failed) {
                callback.onFailure("Unable to load shop details.");
            }
        });
    }

    /**
     * Atomically checks capacity for [startMillis, endMillis) against the shop's
     * configured capacity, reserves the slot if there's room, and then writes the
     * full booking record. Safe against multiple customers booking concurrently.
     */
    public static void reserveSlotAndCreateBooking(String providerId, String dateISO,
                                                   long startMillis, long endMillis,
                                                   BookingCreationParams params,
                                                   CustomerBookingCallback callback) {

        FirebaseDatabaseManager.getShopOwnerData(providerId, new ShopOwnerDataCallback() {
            @Override
            public void onSuccess(ShopOwnerModel shop) {
                final int capacity;
                final long openMillis, closeMillis;
                try {
                    capacity = Integer.parseInt(shop.getShopSlot().trim());
                    long[] window = BookingTimeUtils.getOperatingWindowMillis(
                            dateISO, shop.getShopOpenAt(), shop.getShopClosedAt());
                    openMillis = window[0];
                    closeMillis = window[1];
                } catch (ParseException | NumberFormatException | NullPointerException e) {
                    callback.onFailure("Shop hours or capacity are not configured correctly.");
                    return;
                }

                if (!BookingTimeUtils.fitsWithinOperatingHours(startMillis, endMillis, openMillis, closeMillis)) {
                    callback.onFailure("Selected time does not fit within the shop's operating hours.");
                    return;
                }

                String bookingId = bookingsRef().push().getKey();
                if (bookingId == null) {
                    callback.onFailure("Failed to generate booking id.");
                    return;
                }

                DatabaseReference dayRef = slotsRootRef().child(providerId).child(dateISO);

                dayRef.runTransaction(new Transaction.Handler() {
                    @NonNull
                    @Override
                    public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                        int overlapCount = 0;
                        for (MutableData child : currentData.getChildren()) {
                            Long cStart = child.child("start").getValue(Long.class);
                            Long cEnd = child.child("end").getValue(Long.class);
                            String cStatus = child.child("status").getValue(String.class);
                            if (cStart == null || cEnd == null) continue;
                            if (!BookingStatusUtils.consumesCapacity(cStatus)) continue;
                            if (BookingTimeUtils.rangesOverlap(startMillis, endMillis, cStart, cEnd)) {
                                overlapCount++;
                            }
                        }

                        if (overlapCount >= capacity) {
                            return Transaction.abort();
                        }

                        Map<String, Object> slot = new HashMap<>();
                        slot.put("start", startMillis);
                        slot.put("end", endMillis);
                        slot.put("status", "pending");
                        currentData.child(bookingId).setValue(slot);

                        return Transaction.success(currentData);
                    }

                    @Override
                    public void onComplete(DatabaseError error, boolean committed, DataSnapshot snapshot) {
                        if (error != null) {
                            callback.onFailure("Booking failed, please try again.");
                            return;
                        }
                        if (!committed) {
                            callback.onFailure("Sorry, this time slot just became fully booked. Please choose another time.");
                            return;
                        }
                        writeFullBookingRecord(bookingId, providerId, dateISO, startMillis, endMillis, params, callback);
                    }
                });
            }

            @Override
            public void onFailure(String failed) {
                callback.onFailure("Unable to load shop details.");
            }
        });
    }

    /**
     * Capacity-checked reschedule. Re-validates operating hours and capacity for the
     * NEW date/time, excluding this booking's own existing slot entry from the overlap
     * count (so a booking doesn't block itself). If the shop/date changes, the old slot
     * index entry is removed only after the new one is safely reserved and the booking
     * record is updated - so a failure partway through never leaves the shop overbooked,
     * at worst it leaves a harmless orphaned old-slot entry that a failed rollback path
     * cleans up.
     *
     * oldDateISO/newDateISO and the time strings must already be in the same "yyyy-MM-dd"
     * / "hh:mm a" format used elsewhere (serviceDate / serviceTime).
     */
    public static void rescheduleBooking(String bookingId, String providerId,
                                         String oldDateISO, String newDateISO,
                                         long newStartMillis, long newEndMillis,
                                         String newServiceDateDisplay, String newServiceTimeDisplay,
                                         UpdateBookingCallback callback) {

        FirebaseDatabaseManager.getShopOwnerData(providerId, new ShopOwnerDataCallback() {
            @Override
            public void onSuccess(ShopOwnerModel shop) {
                final int capacity;
                final long openMillis, closeMillis;
                try {
                    capacity = Integer.parseInt(shop.getShopSlot().trim());
                    long[] window = BookingTimeUtils.getOperatingWindowMillis(
                            newDateISO, shop.getShopOpenAt(), shop.getShopClosedAt());
                    openMillis = window[0];
                    closeMillis = window[1];
                } catch (ParseException | NumberFormatException | NullPointerException e) {
                    callback.onFailure("Shop hours or capacity are not configured correctly.");
                    return;
                }

                if (!BookingTimeUtils.fitsWithinOperatingHours(newStartMillis, newEndMillis, openMillis, closeMillis)) {
                    callback.onFailure("Selected time does not fit within the shop's operating hours.");
                    return;
                }

                DatabaseReference newDayRef = slotsRootRef().child(providerId).child(newDateISO);

                newDayRef.runTransaction(new Transaction.Handler() {
                    @NonNull
                    @Override
                    public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                        int overlapCount = 0;
                        for (MutableData child : currentData.getChildren()) {
                            // Skip this booking's own existing slot entry - it shouldn't
                            // count against itself when checking the new time.
                            if (bookingId.equals(child.getKey())) continue;

                            Long cStart = child.child("start").getValue(Long.class);
                            Long cEnd = child.child("end").getValue(Long.class);
                            String cStatus = child.child("status").getValue(String.class);
                            if (cStart == null || cEnd == null) continue;
                            if (!BookingStatusUtils.consumesCapacity(cStatus)) continue;
                            if (BookingTimeUtils.rangesOverlap(newStartMillis, newEndMillis, cStart, cEnd)) {
                                overlapCount++;
                            }
                        }

                        if (overlapCount >= capacity) {
                            return Transaction.abort();
                        }

                        Map<String, Object> slot = new HashMap<>();
                        slot.put("start", newStartMillis);
                        slot.put("end", newEndMillis);
                        slot.put("status", "pending");
                        currentData.child(bookingId).setValue(slot);

                        return Transaction.success(currentData);
                    }

                    @Override
                    public void onComplete(DatabaseError error, boolean committed, DataSnapshot snapshot) {
                        if (error != null) {
                            callback.onFailure("Reschedule failed, please try again.");
                            return;
                        }
                        if (!committed) {
                            callback.onFailure("Sorry, that time just became fully booked. Please choose another time.");
                            return;
                        }

                        Map<String, Object> bookingUpdates = new HashMap<>();
                        bookingUpdates.put("/bookings/" + bookingId + "/serviceDate", newServiceDateDisplay);
                        bookingUpdates.put("/bookings/" + bookingId + "/serviceTime", newServiceTimeDisplay);

                        FirebaseDatabase.getInstance().getReference()
                                .updateChildren(bookingUpdates)
                                .addOnSuccessListener(unused -> {
                                    // Only remove the old slot entry once the new one is
                                    // safely reserved and the booking record is updated.
                                    if (!oldDateISO.equals(newDateISO)) {
                                        slotsRootRef().child(providerId).child(oldDateISO).child(bookingId).removeValue();
                                    }
                                    callback.onSuccess();
                                })
                                .addOnFailureListener(e -> {
                                    // Roll back the new reservation since the booking
                                    // record itself never got updated.
                                    slotsRootRef().child(providerId).child(newDateISO).child(bookingId).removeValue();
                                    callback.onFailure("Failed to update booking. Please try again.");
                                });
                    }
                });
            }

            @Override
            public void onFailure(String failed) {
                callback.onFailure("Unable to load shop details.");
            }
        });
    }

    private static void writeFullBookingRecord(String bookingId, String providerId, String dateISO,
                                               long startMillis, long endMillis,
                                               BookingCreationParams p, CustomerBookingCallback callback) {

        CustomerBookingModel bookingDetails = new CustomerBookingModel(
                bookingId, p.serviceId, providerId, p.bookerId, p.bookerImage, p.serviceImage,
                p.serviceProviderImage, p.bookerName, p.serviceName, p.serviceProviderName,
                p.serviceProviderLocation, p.bookerContactNum, p.bookerEmail, p.bookerVehicle,
                p.bookerAdditionalNotes, p.bookerRating, p.bookerFeedback, p.serviceType,
                p.servicePrice, p.serviceDuration, p.serviceDate, p.serviceTime, "pending",
                p.createdAt, "", "", "", "", "no", p.bookerVehicleSize, p.bookerNumber, p.timestamp,
                startMillis, endMillis, p.bookerPaymentMethod, p.isBookerPaid, p.additionalFeeVehicleSize, p.overallPriceOfService, "none", "0", "none","no", "no", "no", "none", "none", "none"
        );

        bookingsRef().child(bookingId).setValue(bookingDetails)
                .addOnSuccessListener(unused -> {
                    FirebaseReportsManager.updateReports(bookingDetails, "", "pending");
                    callback.onSuccess("Service Booked Successfully", bookingId);
                })
                .addOnFailureListener(e -> {
                    // Full record failed to write - release the slot we reserved so
                    // capacity isn't leaked, then let the customer retry.
                    slotsRootRef().child(providerId).child(dateISO).child(bookingId).removeValue();
                    callback.onFailure("Failed to book service. Please try again.");
                });
    }
}