package com.example.ca_lalay.firebase;

import androidx.annotation.NonNull;

import com.example.ca_lalay.firebase.callback.BookingRatingCallback;
import com.example.ca_lalay.firebase.callback.CancelBookingCallback;
import com.example.ca_lalay.firebase.callback.CustomerBookingCallback;
import com.example.ca_lalay.firebase.callback.CustomerBookingListCallback;
import com.example.ca_lalay.firebase.callback.OnStatusUpdateCallback;
import com.example.ca_lalay.firebase.callback.SendServiceModificationRequestCallback;
import com.example.ca_lalay.firebase.callback.UpdateBookingCallback;
import com.example.ca_lalay.model.CustomerBookingModel;
import com.example.ca_lalay.utils.BookingTimeUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseBookingManager {
    private static DatabaseReference getDatabase() {
        return FirebaseDatabase.getInstance().getReference("bookings");

    }

    private static DatabaseReference getSlotsDatabase() {
        return FirebaseDatabase.getInstance().getReference("shopBookingSlots");
    }

    /**
     * NOTE: for the customer-facing booking screen, prefer
     * FirebaseCapacityManager.reserveSlotAndCreateBooking(...) instead - it performs
     * an atomic capacity check before creating the booking. This method is kept for
     * any other existing call sites and now also best-effort mirrors the booking
     * into the shopBookingSlots capacity index (not atomic, since no capacity check
     * is performed here).
     */
    public static void createBooking (String serviceId, String serviceProviderId, String bookerId, String bookerImage, String serviceImage, String serviceProviderImage, String bookerName, String serviceName, String serviceProviderName, String serviceProviderLocation, String bookerContactNum, String bookerEmail, String bookerVehicle, String bookerAdditionalNotes, String bookerRating, String bookerFeedback, String serviceType, String servicePrice, String serviceDuration, String serviceDate, String serviceTime, String bookingStatus,
                                      String createdAt, String bookerVehicleSize, String bookerNumber, String timestamp, String bookerPaymentMethod, String vehicleSizeAdditionalFee, CustomerBookingCallback callback) {
        String bookingId = getDatabase().push().getKey();

        CustomerBookingModel bookingDetails = new CustomerBookingModel(
                bookingId,
                serviceId,
                serviceProviderId,
                bookerId,
                bookerImage,
                serviceImage,
                serviceProviderImage,
                bookerName,
                serviceName,
                serviceProviderName,
                serviceProviderLocation,
                bookerContactNum,
                bookerEmail,
                bookerVehicle,
                bookerAdditionalNotes,
                bookerRating,
                bookerFeedback,
                serviceType,
                servicePrice,
                serviceDuration,
                serviceDate,
                serviceTime,
                bookingStatus,
                createdAt,
                "",
                "",
                "",
                "",
                "no"
                ,bookerVehicleSize,
                bookerNumber,
                timestamp,
                bookerPaymentMethod,
                "no",
                vehicleSizeAdditionalFee,
                "","", "", "", "", "","","", "", ""
        );

        getDatabase().child(bookingId).setValue(bookingDetails).addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                FirebaseReportsManager.updateReports(
                        bookingDetails,
                        "",
                        "pending"
                );
                mirrorIntoSlotsIndex(serviceProviderId, serviceDate, bookingId, serviceTime, serviceDuration, bookingStatus);
                callback.onSuccess("Service Booked Successfully", bookingId);
            } else {
                callback.onFailure("Failed to book service");
            }
        }).addOnFailureListener(failed -> {
            callback.onFailure("Failed to book service");
        });
    }

    /** Best-effort, non-atomic mirror of a booking into the capacity index. */
    private static void mirrorIntoSlotsIndex(String providerId, String dateISO, String bookingId,
                                             String serviceTime, String serviceDurationHours, String status) {
        try {
            long start = BookingTimeUtils.combineDateAndTime(dateISO, serviceTime);
            int durationMinutes = BookingTimeUtils.durationHoursStringToMinutes(serviceDurationHours);
            long end = start + BookingTimeUtils.minutesToMillis(durationMinutes);

            Map<String, Object> slot = new HashMap<>();
            slot.put("start", start);
            slot.put("end", end);
            slot.put("status", status);

            getSlotsDatabase().child(providerId).child(dateISO).child(bookingId).setValue(slot);
        } catch (ParseException | NumberFormatException e) {
            // If parsing fails here, the slot index just won't have this entry -
            // it won't block the booking itself.
        }
    }

    public static void updateBookingRating(
            String bookingId,
            String starRatings,
            String ratingID,
            String ratingComment,
            String dateRated,
            BookingRatingCallback callback
    ) {

        HashMap<String, Object> updates = new HashMap<>();
        updates.put("starRatings", starRatings);
        updates.put("ratingID", ratingID);
        updates.put("ratingComment", ratingComment);
        updates.put("dateRated", dateRated);
        updates.put("isServiceRated", "yes");

        getDatabase()
                .child(bookingId)
                .updateChildren(updates)
                .addOnSuccessListener(unused -> {
                    callback.onSuccess("Ratings saved successfully");
                })
                .addOnFailureListener(e -> {
                    callback.onFailure(e.getMessage());
                });
    }

    public static void cancelBooking(String bookingId, CancelBookingCallback callback) {

        DatabaseReference bookingRef = getDatabase().child(bookingId);

        bookingRef.get().addOnSuccessListener(snapshot -> {
            if (!snapshot.exists()) {
                callback.onFailure("Booking not found.");
                return;
            }

            CustomerBookingModel booking = snapshot.getValue(CustomerBookingModel.class);
            if (booking == null) {
                callback.onFailure("Failed to load booking.");
                return;
            }

            Map<String, Object> updates = new HashMap<>();
            updates.put("/bookings/" + bookingId + "/bookingStatus", "cancelled");
            updates.put("/shopBookingSlots/" + booking.getProviderId() + "/" + booking.getServiceDate()
                    + "/" + bookingId + "/status", "cancelled");

            FirebaseDatabase.getInstance().getReference()
                    .updateChildren(updates)
                    .addOnSuccessListener(unused -> callback.onSuccess("Booking cancelled successfully"))
                    .addOnFailureListener(e -> callback.onFailure(e.getMessage()));

        }).addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public static void getBookingsByUser(String userId, CustomerBookingListCallback callback) {

        getDatabase()
                .orderByChild("bookerId")
                .equalTo(userId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        List<CustomerBookingModel> list = new ArrayList<>();

                        for (DataSnapshot data : snapshot.getChildren()) {
                            CustomerBookingModel booking = data.getValue(CustomerBookingModel.class);
                            if (booking != null) {
                                list.add(booking);
                            }
                        }

                        callback.onSuccess(list);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onFailure(error.getMessage());
                    }
                });
    }

    public static void updateBookingDateAndTime(
            String bookingId,
            String newDate,
            String newTime,
            UpdateBookingCallback callback) {

        if (bookingId == null || bookingId.trim().isEmpty()) {
            callback.onFailure("Booking ID is required.");
            return;
        }

        if (newDate == null || newDate.trim().isEmpty()) {
            callback.onFailure("Service date is required.");
            return;
        }

        if (newTime == null || newTime.trim().isEmpty()) {
            callback.onFailure("Service time is required.");
            return;
        }

        // NOTE: this does not re-run a capacity check against the new date/time.
        // If you expose rescheduling to customers, route it through
        // FirebaseCapacityManager instead so capacity is re-validated.
        Map<String, Object> updates = new HashMap<>();
        updates.put("serviceDate", newDate);
        updates.put("serviceTime", newTime);

        getDatabase()
                .child(bookingId)
                .updateChildren(updates)
                .addOnSuccessListener(unused -> {
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    callback.onFailure(e.getMessage());
                });
    }

    public static void getBookingsByProvider(String providerId, CustomerBookingListCallback callback) {

        getDatabase()
                .orderByChild("providerId")
                .equalTo(providerId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        List<CustomerBookingModel> list = new ArrayList<>();

                        for (DataSnapshot data : snapshot.getChildren()) {
                            CustomerBookingModel booking = data.getValue(CustomerBookingModel.class);
                            if (booking != null) {
                                list.add(booking);
                            }
                        }

                        callback.onSuccess(list);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onFailure(error.getMessage());
                    }
                });
    }

    public static void updateBookingStatus(String bookingId,
                                           String newStatus,
                                           OnStatusUpdateCallback callback) {

        DatabaseReference bookingRef = getDatabase().child(bookingId);

        bookingRef.get().addOnSuccessListener(snapshot -> {

            if (!snapshot.exists()) {
                callback.onFailure("Booking not found.");
                return;
            }

            CustomerBookingModel booking =
                    snapshot.getValue(CustomerBookingModel.class);

            if (booking == null) {
                callback.onFailure("Failed to load booking.");
                return;
            }

            String oldStatus = booking.getBookingStatus();

            Map<String, Object> updates = new HashMap<>();
            updates.put("/bookings/" + bookingId + "/bookingStatus", newStatus);
            updates.put("/shopBookingSlots/" + booking.getProviderId() + "/" + booking.getServiceDate()
                    + "/" + bookingId + "/status", newStatus);

            FirebaseDatabase.getInstance().getReference()
                    .updateChildren(updates)
                    .addOnSuccessListener(unused -> {

                        // Update the local object so the report manager
                        // sees the latest status if it needs it.
                        booking.setBookingStatus(newStatus);

                        FirebaseReportsManager.updateReports(
                                booking,
                                oldStatus,
                                newStatus
                        );

                        callback.onSuccess("Status updated successfully");
                    })
                    .addOnFailureListener(e -> {
                        callback.onFailure(e.getMessage());
                    });

        }).addOnFailureListener(e -> {
            callback.onFailure(e.getMessage());
        });
    }
    public static void sendServiceModificationRequest(
            String bookingId,
            SendServiceModificationRequestCallback callback) {

        if (bookingId == null || bookingId.trim().isEmpty()) {
            callback.onFailure("Booking ID is required.");
            return;
        }

        DatabaseReference bookingRef =
                getDatabase().child(bookingId);

        bookingRef.get()
                .addOnSuccessListener(snapshot -> {

                    if (!snapshot.exists()) {
                        callback.onFailure("Booking not found.");
                        return;
                    }

                    Map<String, Object> updates = new HashMap<>();

                    // Shop has submitted a modification request
                    updates.put("shopSubmitServiceAddRequest", "yes");

                    // Customer has not responded yet
                    updates.put("customerRespondedToServiceChanges", "no");

                    // No approval yet
                    updates.put("customerApprovesServiceChanges", "no");

                    bookingRef.updateChildren(updates)
                            .addOnSuccessListener(unused -> {

                                callback.onSuccess(
                                        "Modification request sent successfully."
                                );

                            })
                            .addOnFailureListener(e -> {

                                callback.onFailure(
                                        e.getMessage() != null
                                                ? e.getMessage()
                                                : "Failed to send modification request."
                                );

                            });

                })
                .addOnFailureListener(e -> {

                    callback.onFailure(
                            e.getMessage() != null
                                    ? e.getMessage()
                                    : "Failed to access booking."
                    );
                });
    }
    public static void approveServiceModificationRequest(
            String bookingId,
            SendServiceModificationRequestCallback callback) {

        if (bookingId == null || bookingId.trim().isEmpty()) {
            callback.onFailure("Booking ID is required.");
            return;
        }

        DatabaseReference bookingRef =
                getDatabase().child(bookingId);

        bookingRef.get()
                .addOnSuccessListener(snapshot -> {

                    if (!snapshot.exists()) {
                        callback.onFailure("Booking not found.");
                        return;
                    }

                    Map<String, Object> updates = new HashMap<>();

                    // Customer has responded
                    updates.put(
                            "customerRespondedToServiceChanges",
                            "yes"
                    );

                    // Customer approved the modification
                    updates.put(
                            "customerApprovesServiceChanges",
                            "yes"
                    );

                    bookingRef.updateChildren(updates)
                            .addOnSuccessListener(unused -> {

                                callback.onSuccess(
                                        "Modification request approved successfully."
                                );

                            })
                            .addOnFailureListener(e -> {

                                callback.onFailure(
                                        e.getMessage() != null
                                                ? e.getMessage()
                                                : "Failed to approve modification request."
                                );

                            });

                })
                .addOnFailureListener(e -> {

                    callback.onFailure(
                            e.getMessage() != null
                                    ? e.getMessage()
                                    : "Failed to access booking."
                    );

                });
    }
    public static void rejectServiceModificationRequest(
            String bookingId,
            SendServiceModificationRequestCallback callback) {

        if (bookingId == null || bookingId.trim().isEmpty()) {
            callback.onFailure("Booking ID is required.");
            return;
        }

        DatabaseReference bookingRef =
                getDatabase().child(bookingId);

        bookingRef.get()
                .addOnSuccessListener(snapshot -> {

                    if (!snapshot.exists()) {
                        callback.onFailure("Booking not found.");
                        return;
                    }

                    Map<String, Object> updates = new HashMap<>();

                    // Customer has responded
                    updates.put(
                            "customerRespondedToServiceChanges",
                            "yes"
                    );

                    // Customer rejected the modification
                    updates.put(
                            "customerApprovesServiceChanges",
                            "no"
                    );

                    bookingRef.updateChildren(updates)
                            .addOnSuccessListener(unused -> {

                                callback.onSuccess(
                                        "Modification request rejected."
                                );

                            })
                            .addOnFailureListener(e -> {

                                callback.onFailure(
                                        e.getMessage() != null
                                                ? e.getMessage()
                                                : "Failed to reject modification request."
                                );

                            });

                })
                .addOnFailureListener(e -> {

                    callback.onFailure(
                            e.getMessage() != null
                                    ? e.getMessage()
                                    : "Failed to access booking."
                    );

                });
    }
    public static void saveServiceModification(
            String bookingId,
            String serviceAdded,
            String serviceAddedTotalPrice,
            String serviceAddedRemarks,
            SendServiceModificationRequestCallback callback) {

        if (bookingId == null || bookingId.trim().isEmpty()) {
            callback.onFailure("Booking ID is required.");
            return;
        }

        DatabaseReference bookingRef =
                getDatabase().child(bookingId);

        bookingRef.get()
                .addOnSuccessListener(snapshot -> {

                    if (!snapshot.exists()) {
                        callback.onFailure("Booking not found.");
                        return;
                    }

                    Map<String, Object> updates = new HashMap<>();

                    updates.put("serviceAdded", serviceAdded);
                    updates.put("serviceAddedTotalPrice", serviceAddedTotalPrice);
                    updates.put("serviceAddedRemarks", serviceAddedRemarks);

                    bookingRef.updateChildren(updates)
                            .addOnSuccessListener(unused -> {

                                callback.onSuccess(
                                        "Booking modification saved successfully."
                                );

                            })
                            .addOnFailureListener(e -> {

                                callback.onFailure(
                                        e.getMessage() != null
                                                ? e.getMessage()
                                                : "Failed to save booking modification."
                                );

                            });
                })
                .addOnFailureListener(e -> {

                    callback.onFailure(
                            e.getMessage() != null
                                    ? e.getMessage()
                                    : "Failed to access booking."
                    );

                });
    }
    public static void markBookingAsPaid(
            String bookingId,
            OnMarkBookingPaidListener listener) {

        getDatabase()
                .child(bookingId)
                .child("isBookerPaid")
                .setValue("yes")
                .addOnSuccessListener(unused -> {
                    if (listener != null) {
                        listener.onSuccess();
                    }
                })
                .addOnFailureListener(e -> {
                    if (listener != null) {
                        listener.onFailure(e.getMessage());
                    }
                });
    }
    public static void updateBillingNumber(
            String bookingId,
            String billingNumber,
            OnUpdateBillingNumberListener listener) {

        getDatabase()
                .child(bookingId)
                .child("billingNumber")
                .setValue(billingNumber)
                .addOnSuccessListener(unused -> {
                    if (listener != null) {
                        listener.onSuccess();
                    }
                })
                .addOnFailureListener(e -> {
                    if (listener != null) {
                        listener.onFailure(e.getMessage());
                    }
                });
    }

    public static void bookingRejectedReason(
            String bookingId,
            String reason,
            OnUpdateRejectedBookingReason listener) {

        getDatabase()
                .child(bookingId)
                .child("rejectedBookingReason")
                .setValue(reason)
                .addOnSuccessListener(unused -> {
                    if (listener != null) {
                        listener.onSuccess();
                    }
                })
                .addOnFailureListener(e -> {
                    if (listener != null) {
                        listener.onFailure(e.getMessage());
                    }
                });
    }
    public static void updateReceipt(
            String bookingId,
            String imageReceiptURL,
            OnUpdateImageReceipt listener) {

        getDatabase()
                .child(bookingId)
                .child("bookerReceiptImage")
                .setValue(imageReceiptURL)
                .addOnSuccessListener(unused -> {
                    if (listener != null) {
                        listener.onSuccess();
                    }
                })
                .addOnFailureListener(e -> {
                    if (listener != null) {
                        listener.onFailure(e.getMessage());
                    }
                });
    }
    public interface OnUpdateImageReceipt {
        void onSuccess();
        void onFailure(String error);
    }
    public interface OnUpdateRejectedBookingReason {
        void onSuccess();
        void onFailure(String error);
    }

    public interface OnUpdateBillingNumberListener {
        void onSuccess();
        void onFailure(String error);
    }

    public interface OnMarkBookingPaidListener {
        void onSuccess();
        void onFailure(String error);
    }
}