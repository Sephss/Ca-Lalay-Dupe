package com.example.ca_lalay.firebase;

import androidx.annotation.NonNull;

import com.example.ca_lalay.firebase.callback.OnReportCallback;
import com.example.ca_lalay.firebase.callback.OnReportsListCallback;
import com.example.ca_lalay.model.BookingReportModel;
import com.example.ca_lalay.model.CustomerBookingModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Collections;

public class FirebaseReportsManager {

    private static final DatabaseReference database =
            FirebaseDatabase.getInstance().getReference("booking_reports");

    public static DatabaseReference getDatabase() {
        return database;
    }
    public static void updateReports(
            CustomerBookingModel booking,
            String oldStatus,
            String newStatus
    ) {

        String shopUid = booking.getProviderId();

        double priceOfService = Double.parseDouble(booking.getOverallPriceOfService());
        double priceOfServiceAdded = Double.parseDouble(booking.getServiceAddedTotalPrice());
        double TOTAL_ICOME = priceOfService + priceOfServiceAdded;

        double income = Double.parseDouble(booking.getServicePrice());

        LocalDate localDate = LocalDate.parse(booking.getServiceDate());

        String dailyKey = localDate.toString();

        WeekFields weekFields = WeekFields.ISO;

        String weeklyKey = localDate.getYear() +
                "-W" +
                localDate.get(weekFields.weekOfWeekBasedYear());

        String monthlyKey = localDate.getYear() +
                "-" +
                String.format("%02d", localDate.getMonthValue());

        String yearlyKey = String.valueOf(localDate.getYear());

        updatePeriod("daily", shopUid, dailyKey, TOTAL_ICOME, oldStatus, newStatus);

        updatePeriod("weekly", shopUid, weeklyKey, TOTAL_ICOME, oldStatus, newStatus);

        updatePeriod("monthly", shopUid, monthlyKey, TOTAL_ICOME, oldStatus, newStatus);

        updatePeriod("yearly", shopUid, yearlyKey, TOTAL_ICOME, oldStatus, newStatus);
    }
    private static void updatePeriod(String type,
                                     String shopUid,
                                     String period,
                                     double income,
                                     String oldStatus,
                                     String newStatus) {

        DatabaseReference ref = database
                .child(type)
                .child(shopUid)
                .child(period);

        ref.runTransaction(new Transaction.Handler() {

            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {

                BookingReportModel report =
                        currentData.getValue(BookingReportModel.class);

                if (report == null) {

                    report = new BookingReportModel();

                    report.setPeriod(period);
                    report.setTotalBookings(0);
                    report.setCompletedBookings(0);
                    report.setCancelledBookings(0);
                    report.setPendingBookings(0);
                    report.setTotalIncome(0);
                }

                // Don't do anything if the status didn't actually change
                if (oldStatus.equals(newStatus)) {
                    currentData.setValue(report);
                    return Transaction.success(currentData);
                }

                /*
                 * CUSTOMER CREATED A NEW BOOKING
                 * "" -> pending
                 */
                if (oldStatus.isEmpty() && newStatus.equals("pending")) {

                    report.setTotalBookings(
                            report.getTotalBookings() + 1);

                    report.setPendingBookings(
                            report.getPendingBookings() + 1);
                }

                /*
                 * PENDING -> IN PROGRESS
                 */
                else if (oldStatus.equals("pending")
                        && newStatus.equals("inProgress")) {

                    report.setPendingBookings(
                            Math.max(0, report.getPendingBookings() - 1));
                }
                /*
                 * confirmed -> IN PROGRESS
                 */
                else if (oldStatus.equals("confirmed")
                        && newStatus.equals("inProgress")) {

                    report.setPendingBookings(
                            Math.max(0, report.getPendingBookings() - 1));
                }

                /*
                 * PENDING -> DONE
                 */
                else if (oldStatus.equals("pending")
                        && newStatus.equals("done")) {

                    report.setPendingBookings(
                            Math.max(0, report.getPendingBookings() - 1));

                    report.setCompletedBookings(
                            report.getCompletedBookings() + 1);

                    report.setTotalIncome(
                            report.getTotalIncome() + income);
                }

                /*
                 * IN PROGRESS -> DONE
                 */
                else if (oldStatus.equals("inProgress")
                        && newStatus.equals("done")) {

                    report.setCompletedBookings(
                            report.getCompletedBookings() + 1);

                    report.setTotalIncome(
                            report.getTotalIncome() + income);
                }

                /*
                 * PENDING -> CANCELLED
                 */
                else if (oldStatus.equals("pending")
                        && newStatus.equals("cancelled")) {

                    report.setPendingBookings(
                            Math.max(0, report.getPendingBookings() - 1));

                    report.setCancelledBookings(
                            report.getCancelledBookings() + 1);
                }

                /*
                 * PENDING -> REJECTED
                 */
                else if (oldStatus.equals("pending")
                        && newStatus.equals("rejected")) {

                    report.setPendingBookings(
                            Math.max(0, report.getPendingBookings() - 1));

                    report.setCancelledBookings(
                            report.getCancelledBookings() + 1);
                }

                /*
                 * CONFIRMED -> CANCELLED
                 */
                else if (oldStatus.equals("confirmed")
                        && newStatus.equals("cancelled")) {

                    report.setCancelledBookings(
                            report.getCancelledBookings() + 1);
                    report.setCancelledBookings(report.getCancelledBookings() + 1);
                }

                /*
                 * IN PROGRESS -> CANCELLED
                 */
                else if (oldStatus.equals("inProgress")
                        && newStatus.equals("cancelled")) {

                    report.setCancelledBookings(
                            report.getCancelledBookings() + 1);
                }


                currentData.setValue(report);

                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(@NonNull DatabaseError error,
                                   boolean committed,
                                   DataSnapshot currentData) {

                // Optional: log success/failure here

            }
        });
    }
    public static void getDailyReport(String shopUid,
                                      String date,
                                      OnReportCallback callback) {

        database.child("daily")
                .child(shopUid)
                .child(date)
                .get()
                .addOnSuccessListener(snapshot -> {

                    if (!snapshot.exists()) {
                        BookingReportModel report = new BookingReportModel();

                        report.setPeriod(date);

                        report.setTotalBookings(0);
                        report.setCompletedBookings(0);
                        report.setCancelledBookings(0);
                        report.setPendingBookings(0);
                        report.setTotalIncome(0);

                        callback.onSuccess(report);

                        return;
                    }

                    BookingReportModel report =
                            snapshot.getValue(BookingReportModel.class);

                    if (report != null) {
                        callback.onSuccess(report);
                    } else {
                        callback.onFailure("Failed to load daily report.");
                    }

                })
                .addOnFailureListener(e ->
                        callback.onFailure(e.getMessage()));
    }
    public static void getMonthlyReports(String shopUid,
                                         int year,
                                         OnReportsListCallback callback) {

        database.child("monthly")
                .child(shopUid)
                .get()
                .addOnSuccessListener(snapshot -> {

                    ArrayList<BookingReportModel> reports = new ArrayList<>();

                    for (DataSnapshot ds : snapshot.getChildren()) {

                        BookingReportModel report =
                                ds.getValue(BookingReportModel.class);

                        if (report == null) {
                            continue;
                        }

                        // Example period: 2026-07
                        String period = report.getPeriod();

                        if (period != null &&
                                period.startsWith(String.valueOf(year))) {

                            reports.add(report);

                        }

                    }

                    Collections.sort(reports,
                            (r1, r2) ->
                                    r1.getPeriod().compareTo(r2.getPeriod()));

                    callback.onSuccess(reports);

                })
                .addOnFailureListener(e ->
                        callback.onFailure(e.getMessage()));

    }
    public static void getLast7DaysReports(String shopUid,
                                           OnReportsListCallback callback) {

        database.child("daily")
                .child(shopUid)
                .get()
                .addOnSuccessListener(snapshot -> {

                    ArrayList<BookingReportModel> reports = new ArrayList<>();

                    LocalDate today = LocalDate.now();

                    LocalDate sevenDaysAgo = today.minusDays(6);

                    for (DataSnapshot ds : snapshot.getChildren()) {

                        BookingReportModel report =
                                ds.getValue(BookingReportModel.class);

                        if (report == null)
                            continue;

                        try {

                            LocalDate reportDate =
                                    LocalDate.parse(report.getPeriod());

                            if (!reportDate.isBefore(sevenDaysAgo)
                                    && !reportDate.isAfter(today)) {

                                reports.add(report);

                            }

                        } catch (Exception ignored) {

                        }

                    }

                    Collections.sort(
                            reports,
                            (r1, r2) ->
                                    r1.getPeriod().compareTo(r2.getPeriod())
                    );

                    callback.onSuccess(reports);

                })
                .addOnFailureListener(e ->
                        callback.onFailure(e.getMessage()));

    }
    public static void getOverallReport(String shopUid,
                                        OnReportCallback callback) {

        database.child("yearly")
                .child(shopUid)
                .get()
                .addOnSuccessListener(snapshot -> {

                    BookingReportModel overall = new BookingReportModel();

                    overall.setTotalIncome(0);
                    overall.setTotalBookings(0);
                    overall.setCompletedBookings(0);
                    overall.setCancelledBookings(0);
                    overall.setPendingBookings(0);

                    for (DataSnapshot ds : snapshot.getChildren()) {

                        BookingReportModel report =
                                ds.getValue(BookingReportModel.class);

                        if (report == null) continue;

                        overall.setTotalIncome(
                                overall.getTotalIncome() + report.getTotalIncome()
                        );

                        overall.setTotalBookings(
                                overall.getTotalBookings() + report.getTotalBookings()
                        );

                        overall.setCompletedBookings(
                                overall.getCompletedBookings() + report.getCompletedBookings()
                        );
                        overall.setCancelledBookings(
                                overall.getCancelledBookings() + report.getCancelledBookings()
                        );

                        overall.setPendingBookings(
                                overall.getPendingBookings() + report.getPendingBookings()
                        );
                    }

                    callback.onSuccess(overall);

                })
                .addOnFailureListener(e ->
                        callback.onFailure(e.getMessage()));

    }
    public static void getWeeklyReport(String shopUid,
                                       String week,
                                       OnReportCallback callback) {

        database.child("weekly")
                .child(shopUid)
                .child(week)
                .get()
                .addOnSuccessListener(snapshot -> {

                    if (!snapshot.exists()) {

                        BookingReportModel report = new BookingReportModel();

                        report.setPeriod(week);
                        report.setTotalBookings(0);
                        report.setCompletedBookings(0);
                        report.setCancelledBookings(0);
                        report.setPendingBookings(0);
                        report.setTotalIncome(0);

                        callback.onSuccess(report);
                        return;
                    }

                    BookingReportModel report =
                            snapshot.getValue(BookingReportModel.class);

                    if (report != null) {

                        callback.onSuccess(report);

                    } else {

                        callback.onFailure("Failed to load weekly report.");

                    }

                })
                .addOnFailureListener(e ->
                        callback.onFailure(e.getMessage()));

    }
    public static void getMonthlyReport(String shopUid,
                                        String month,
                                        OnReportCallback callback) {

        database.child("monthly")
                .child(shopUid)
                .child(month)
                .get()
                .addOnSuccessListener(snapshot -> {

                    if (!snapshot.exists()) {

                        BookingReportModel report = new BookingReportModel();

                        report.setPeriod(month);
                        report.setTotalBookings(0);
                        report.setCompletedBookings(0);
                        report.setCancelledBookings(0);
                        report.setPendingBookings(0);
                        report.setTotalIncome(0);

                        callback.onSuccess(report);

                        return;
                    }

                    BookingReportModel report =
                            snapshot.getValue(BookingReportModel.class);

                    if (report != null) {

                        callback.onSuccess(report);

                    } else {

                        callback.onFailure("Failed to load monthly report.");

                    }

                })
                .addOnFailureListener(e ->
                        callback.onFailure(e.getMessage()));

    }
    public static void getYearlyReport(String shopUid,
                                       String year,
                                       OnReportCallback callback) {

        database.child("yearly")
                .child(shopUid)
                .child(year)
                .get()
                .addOnSuccessListener(snapshot -> {

                    if (!snapshot.exists()) {

                        BookingReportModel report = new BookingReportModel();

                        report.setPeriod(year);
                        report.setTotalBookings(0);
                        report.setCompletedBookings(0);
                        report.setCancelledBookings(0);
                        report.setPendingBookings(0);
                        report.setTotalIncome(0);

                        callback.onSuccess(report);

                        return;
                    }

                    BookingReportModel report =
                            snapshot.getValue(BookingReportModel.class);

                    if (report != null) {

                        callback.onSuccess(report);

                    } else {

                        callback.onFailure("Failed to load yearly report.");

                    }

                })
                .addOnFailureListener(e ->
                        callback.onFailure(e.getMessage()));

    }
    public static String getCurrentWeekKey() {

        LocalDate today = LocalDate.now();

        WeekFields weekFields = WeekFields.ISO;

        return today.getYear()
                + "-W"
                + today.get(weekFields.weekOfWeekBasedYear());

    }
}