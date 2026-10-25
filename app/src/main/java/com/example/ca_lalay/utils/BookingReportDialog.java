package com.example.ca_lalay.utils;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.graphics.Typeface;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.ca_lalay.firebase.FirebaseBookingReportsLoader;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Filter dialog for the Detailed Bookings Report.
 * Built in code, so no XML/layout files are touched.
 */
public class BookingReportDialog {

    public static final int FORMAT_PDF = 0, FORMAT_XLSX = 1, FORMAT_CSV = 2;

    private static final String[] PERIODS =
            {"Today", "This Week", "This Month", "This Year", "Custom Range"};

    private static final String[] STATUS_LABELS =
            {"All Statuses", "Pending", "Confirmed", "In Progress", "Completed", "Cancelled", "Rejected"};
    private static final String[] STATUS_VALUES =
            {null, "pending", "confirmed", "inProgress", "done", "cancelled", "rejected"};

    private static final String[] FORMATS = {"PDF", "Excel (.xlsx)", "CSV (.csv)"};

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    public static class Options {
        public LocalDate from, to;
        public String periodLabel;   // "This Month"
        public String rangeLabel;    // "Jul 01, 2026 - Jul 31, 2026"
        public String fileTag;       // "2026-07-01_to_2026-07-31"
        public String serviceId;     // null = all
        public String serviceName;   // "All Services" or the name
        public String status;        // null = all
        public String statusLabel;
        public int format;
    }

    public interface OnConfirm {
        void onConfirm(Options options);
    }

    public static void show(Activity activity, String shopUid, OnConfirm callback) {
        FirebaseBookingReportsLoader.loadServices(shopUid,
                new FirebaseBookingReportsLoader.ServicesListener() {
                    @Override
                    public void onLoaded(LinkedHashMap<String, String> services) {
                        build(activity, services, callback);
                    }

                    @Override
                    public void onError(String message) {
                        build(activity, new LinkedHashMap<>(), callback); // still usable: "All Services"
                    }
                });
    }

    private static void build(Activity a, LinkedHashMap<String, String> services, OnConfirm callback) {

        List<String> serviceIds = new ArrayList<>();
        List<String> serviceNames = new ArrayList<>();
        serviceIds.add(null);
        serviceNames.add("All Services");
        for (Map.Entry<String, String> e : services.entrySet()) {
            serviceIds.add(e.getKey());
            serviceNames.add(e.getValue());
        }

        LinearLayout root = new LinearLayout(a);
        root.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(a, 20);
        root.setPadding(pad, dp(a, 8), pad, 0);

        Spinner periodSp = spinner(a, PERIODS);
        Spinner serviceSp = spinner(a, serviceNames.toArray(new String[0]));
        Spinner statusSp = spinner(a, STATUS_LABELS);
        Spinner formatSp = spinner(a, FORMATS);

        // custom range row
        final LocalDate[] range = {LocalDate.now(), LocalDate.now()};
        LinearLayout dateRow = new LinearLayout(a);
        dateRow.setOrientation(LinearLayout.HORIZONTAL);
        dateRow.setVisibility(View.GONE);
        Button fromBtn = new Button(a);
        Button toBtn = new Button(a);
        fromBtn.setAllCaps(false);
        toBtn.setAllCaps(false);
        fromBtn.setText("From: " + range[0].format(FMT));
        toBtn.setText("To: " + range[1].format(FMT));
        LinearLayout.LayoutParams half = new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        dateRow.addView(fromBtn, half);
        dateRow.addView(toBtn, half);

        fromBtn.setOnClickListener(v -> new DatePickerDialog(a, (view, y, m, d) -> {
            range[0] = LocalDate.of(y, m + 1, d);
            fromBtn.setText("From: " + range[0].format(FMT));
        }, range[0].getYear(), range[0].getMonthValue() - 1, range[0].getDayOfMonth()).show());

        toBtn.setOnClickListener(v -> new DatePickerDialog(a, (view, y, m, d) -> {
            range[1] = LocalDate.of(y, m + 1, d);
            toBtn.setText("To: " + range[1].format(FMT));
        }, range[1].getYear(), range[1].getMonthValue() - 1, range[1].getDayOfMonth()).show());

        periodSp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                dateRow.setVisibility(position == 4 ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        root.addView(label(a, "Period"));
        root.addView(periodSp);
        root.addView(dateRow);
        root.addView(label(a, "Service"));
        root.addView(serviceSp);
        root.addView(label(a, "Booking Status"));
        root.addView(statusSp);
        root.addView(label(a, "Export As"));
        root.addView(formatSp);

        ScrollView scroll = new ScrollView(a);
        scroll.addView(root);

        AlertDialog dialog = new AlertDialog.Builder(a)
                .setTitle("Detailed Bookings Report")
                .setView(scroll)
                .setPositiveButton("Generate", null) // set below so we can validate without dismissing
                .setNegativeButton("Cancel", null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {

            LocalDate today = LocalDate.now();
            LocalDate from, to;

            switch (periodSp.getSelectedItemPosition()) {
                case 0:
                    from = today;
                    to = today;
                    break;
                case 1:
                    from = today.with(DayOfWeek.MONDAY);
                    to = from.plusDays(6);
                    break;
                case 2:
                    from = today.withDayOfMonth(1);
                    to = today.withDayOfMonth(today.lengthOfMonth());
                    break;
                case 3:
                    from = LocalDate.of(today.getYear(), 1, 1);
                    to = LocalDate.of(today.getYear(), 12, 31);
                    break;
                default:
                    from = range[0];
                    to = range[1];
                    break;
            }

            if (from.isAfter(to)) {
                Toast.makeText(a, "\"From\" date must be before \"To\" date.", Toast.LENGTH_SHORT).show();
                return;
            }

            Options o = new Options();
            o.from = from;
            o.to = to;
            o.periodLabel = PERIODS[periodSp.getSelectedItemPosition()];
            o.rangeLabel = from.equals(to) ? from.format(FMT) : from.format(FMT) + " - " + to.format(FMT);
            o.fileTag = from.equals(to) ? from.toString() : from + "_to_" + to;
            int sPos = serviceSp.getSelectedItemPosition();
            o.serviceId = serviceIds.get(sPos);
            o.serviceName = serviceNames.get(sPos);
            int stPos = statusSp.getSelectedItemPosition();
            o.status = STATUS_VALUES[stPos];
            o.statusLabel = STATUS_LABELS[stPos];
            o.format = formatSp.getSelectedItemPosition();

            dialog.dismiss();
            callback.onConfirm(o);
        }));

        dialog.show();
    }

    private static Spinner spinner(Activity a, String[] items) {
        Spinner sp = new Spinner(a);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(a,
                android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp.setAdapter(adapter);
        return sp;
    }

    private static TextView label(Activity a, String text) {
        TextView tv = new TextView(a);
        tv.setText(text);
        tv.setTextSize(13f);
        tv.setTypeface(null, Typeface.BOLD);
        tv.setPadding(0, dp(a, 14), 0, dp(a, 2));
        return tv;
    }

    private static int dp(Activity a, int v) {
        return Math.round(v * a.getResources().getDisplayMetrics().density);
    }
}