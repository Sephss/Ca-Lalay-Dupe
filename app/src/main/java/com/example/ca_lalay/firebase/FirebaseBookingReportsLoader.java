package com.example.ca_lalay.firebase;

import com.example.ca_lalay.utils.BookingsReportData;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;

/**
 * Read-only helper for booking-level reports.
 * Uses a ONE-SHOT query (get()) on bookings ordered by providerId (already indexed),
 * so it does not leave a listener attached like getBookingsByProvider() does.
 */
public class FirebaseBookingReportsLoader {

    public interface BookingsListener {
        void onLoaded(ArrayList<BookingsReportData.Row> rows);

        void onError(String message);
    }

    public interface ServicesListener {
        /** serviceId -> service name (insertion ordered) */
        void onLoaded(LinkedHashMap<String, String> services);

        void onError(String message);
    }

    /** car_services/{shopUid}/{serviceId}/name */
    public static void loadServices(String shopUid, ServicesListener listener) {
        FirebaseDatabase.getInstance().getReference("car_services")
                .child(shopUid)
                .get()
                .addOnSuccessListener(snapshot -> {
                    LinkedHashMap<String, String> map = new LinkedHashMap<>();
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        String name = str(ds, "name");
                        map.put(ds.getKey(), name.isEmpty() ? ds.getKey() : name);
                    }
                    listener.onLoaded(map);
                })
                .addOnFailureListener(e -> listener.onError(e.getMessage()));
    }

    /**
     * @param from      inclusive
     * @param to        inclusive
     * @param serviceId null = all services
     * @param status    null = all statuses (raw values: pending, confirmed, inProgress, done, cancelled, rejected)
     */
    public static void loadBookings(String shopUid,
                                    LocalDate from,
                                    LocalDate to,
                                    String serviceId,
                                    String status,
                                    BookingsListener listener) {

        FirebaseDatabase.getInstance().getReference("bookings")
                .orderByChild("providerId")
                .equalTo(shopUid)
                .get()
                .addOnSuccessListener(snapshot -> {

                    ArrayList<BookingsReportData.Row> rows = new ArrayList<>();

                    for (DataSnapshot ds : snapshot.getChildren()) {

                        String dateStr = str(ds, "serviceDate");
                        LocalDate date;
                        try {
                            date = LocalDate.parse(dateStr);
                        } catch (Exception e) {
                            continue; // skip bookings with an unreadable date
                        }

                        if (date.isBefore(from) || date.isAfter(to)) continue;

                        String sId = str(ds, "serviceId");
                        if (serviceId != null && !serviceId.equals(sId)) continue;

                        String st = str(ds, "bookingStatus");
                        if (status != null && !status.equalsIgnoreCase(st)) continue;

                        BookingsReportData.Row r = new BookingsReportData.Row();
                        r.bookingId = ds.getKey();
                        r.serviceId = sId;
                        r.serviceName = str(ds, "serviceName");
                        r.customer = str(ds, "bookerName");
                        r.contact = str(ds, "bookerContactNum");
                        r.vehicle = str(ds, "bookerVehicle");
                        r.vehicleSize = str(ds, "bookerVehicleSize");
                        r.date = dateStr;
                        r.time = str(ds, "serviceTime");
                        r.statusRaw = st;
                        r.status = BookingsReportData.prettyStatus(st);
                        r.paymentMethod = str(ds, "bookerPaymentMethod");
                        r.paid = "yes".equalsIgnoreCase(str(ds, "isBookerPaid")) ? "Paid" : "Unpaid";
                        r.billingNumber = str(ds, "billingNumber");
                        r.addOnsDescription = str(ds, "serviceAdded");

                        // Same formula your booking_reports uses: overallPriceOfService + serviceAddedTotalPrice
                        double base = money(str(ds, "overallPriceOfService"));
                        if (base == 0) base = money(str(ds, "servicePrice"));
                        double addOns = money(str(ds, "serviceAddedTotalPrice"));
                        r.basePrice = base;
                        r.addOnsTotal = addOns;
                        r.total = base + addOns;

                        rows.add(r);
                    }

                    Collections.sort(rows, (a, b) -> {
                        int c = a.date.compareTo(b.date);
                        if (c != 0) return c;
                        return Integer.compare(minutesOfDay(a.time), minutesOfDay(b.time));
                    });

                    listener.onLoaded(rows);
                })
                .addOnFailureListener(e -> listener.onError(e.getMessage()));
    }

    // ---------------------------------------------------------------- helpers

    private static String str(DataSnapshot ds, String key) {
        Object v = ds.child(key).getValue();
        return v == null ? "" : String.valueOf(v).trim();
    }

    private static double money(String s) {
        if (s == null) return 0;
        String clean = s.replaceAll("[^0-9.\\-]", "");
        if (clean.isEmpty() || clean.equals("-") || clean.equals(".")) return 0;
        try {
            return Double.parseDouble(clean);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static int minutesOfDay(String time) {
        if (time == null || time.isEmpty()) return 0;
        String[] patterns = {"hh:mm a", "h:mm a", "HH:mm", "H:mm"};
        for (String p : patterns) {
            try {
                Date d = new SimpleDateFormat(p, Locale.US).parse(time.trim());
                if (d != null) {
                    java.util.Calendar c = java.util.Calendar.getInstance();
                    c.setTime(d);
                    return c.get(java.util.Calendar.HOUR_OF_DAY) * 60 + c.get(java.util.Calendar.MINUTE);
                }
            } catch (Exception ignored) {
            }
        }
        return 0;
    }
}