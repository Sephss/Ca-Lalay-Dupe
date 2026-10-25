package com.example.ca_lalay.firebase;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.ca_lalay.firebase.callback.OnServiceReportsCallback;
import com.example.ca_lalay.model.ServiceReportModel;
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

public class FirebaseServiceReportsManager {

    private static final DatabaseReference database =
            FirebaseDatabase.getInstance().getReference("service_reports");

    public static DatabaseReference getDatabase() {
        return database;
    }

    /**
     * Call this ONLY when a booking becomes DONE.
     */
    public static void updateServiceReport(String shopUid,
                                           String serviceId,
                                           String serviceName,
                                           String serviceDate) {

        try {

            LocalDate date = LocalDate.parse(serviceDate);

            WeekFields weekFields = WeekFields.ISO;

            String dailyKey = date.toString();

            String weeklyKey =
                    date.getYear() +
                            "-W" +
                            date.get(weekFields.weekOfWeekBasedYear());

            String monthlyKey =
                    date.getYear() +
                            "-" +
                            String.format("%02d", date.getMonthValue());

            String yearlyKey =
                    String.valueOf(date.getYear());
            Log.d("SERVICE_REPORT", "Daily: " + dailyKey);
            Log.d("SERVICE_REPORT", "Weekly: " + weeklyKey);
            Log.d("SERVICE_REPORT", "Monthly: " + monthlyKey);
            Log.d("SERVICE_REPORT", "Yearly: " + yearlyKey);

            updatePeriod(
                    shopUid,
                    "daily",
                    dailyKey,
                    serviceId,
                    serviceName
            );

            updatePeriod(
                    shopUid,
                    "weekly",
                    weeklyKey,
                    serviceId,
                    serviceName
            );

            updatePeriod(
                    shopUid,
                    "monthly",
                    monthlyKey,
                    serviceId,
                    serviceName
            );

            updatePeriod(
                    shopUid,
                    "yearly",
                    yearlyKey,
                    serviceId,
                    serviceName
            );

        } catch (Exception e) {

            e.printStackTrace();

        }

    }

    private static void updatePeriod(String shopUid,
                                     String type,
                                     String period,
                                     String serviceId,
                                     String serviceName) {
        Log.d("SERVICE_REPORT",
                "Updating " + type + " -> " + period);

        DatabaseReference ref = database
                .child(shopUid)
                .child(type)
                .child(period)
                .child(serviceId);

        ref.runTransaction(new Transaction.Handler() {

            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {

                ServiceReportModel report =
                        currentData.getValue(ServiceReportModel.class);

                if (report == null) {

                    report = new ServiceReportModel();

                    report.setServiceId(serviceId);
                    report.setServiceName(serviceName);
                    report.setTotalBookings(0);

                }

                report.setServiceName(serviceName);

                report.setTotalBookings(
                        report.getTotalBookings() + 1
                );

                currentData.setValue(report);

                return Transaction.success(currentData);

            }

            @Override
            public void onComplete(DatabaseError error,
                                   boolean committed,
                                   DataSnapshot currentData) {

            }

        });

    }

    public static void getDailyServiceReports(String shopUid,
                                              String date,
                                              OnServiceReportsCallback callback) {

        getServiceReports(
                shopUid,
                "daily",
                date,
                callback
        );

    }

    public static void getWeeklyServiceReports(String shopUid,
                                               String week,
                                               OnServiceReportsCallback callback) {

        getServiceReports(
                shopUid,
                "weekly",
                week,
                callback
        );

    }

    public static void getMonthlyServiceReports(String shopUid,
                                                String month,
                                                OnServiceReportsCallback callback) {

        getServiceReports(
                shopUid,
                "monthly",
                month,
                callback
        );

    }

    public static void getYearlyServiceReports(String shopUid,
                                               String year,
                                               OnServiceReportsCallback callback) {

        getServiceReports(
                shopUid,
                "yearly",
                year,
                callback
        );

    }

    private static void getServiceReports(String shopUid,
                                          String type,
                                          String period,
                                          OnServiceReportsCallback callback) {

        database.child(shopUid)
                .child(type)
                .child(period)
                .get()
                .addOnSuccessListener(snapshot -> {

                    ArrayList<ServiceReportModel> reports =
                            new ArrayList<>();

                    for (DataSnapshot ds : snapshot.getChildren()) {

                        ServiceReportModel report =
                                ds.getValue(ServiceReportModel.class);

                        if (report != null) {

                            reports.add(report);

                        }

                    }

                    Collections.sort(
                            reports,
                            (a, b) -> Integer.compare(
                                    b.getTotalBookings(),
                                    a.getTotalBookings()
                            )
                    );

                    callback.onSuccess(reports);

                })
                .addOnFailureListener(e ->
                        callback.onFailure(e.getMessage()));

    }

}