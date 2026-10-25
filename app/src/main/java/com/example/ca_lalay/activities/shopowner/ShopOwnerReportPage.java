package com.example.ca_lalay.activities.shopowner;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.ca_lalay.firebase.FirebaseAuthManager;
import com.example.ca_lalay.firebase.FirebaseDatabaseManager;
import com.example.ca_lalay.firebase.FirebaseReportsManager;
import com.example.ca_lalay.firebase.FirebaseServiceReportsManager;
import com.example.ca_lalay.firebase.PDFReportManager;
import com.example.ca_lalay.firebase.callback.OnReportCallback;
import com.example.ca_lalay.firebase.callback.OnReportsListCallback;
import com.example.ca_lalay.firebase.callback.OnServiceReportsCallback;
import com.example.ca_lalay.firebase.callback.ShopOwnerDataCallback;
import com.example.ca_lalay.model.BookingReportModel;
import com.example.ca_lalay.model.ServiceReportModel;
import com.example.ca_lalay.model.ShopOwnerModel;
import com.example.ca_lalay.utils.PesoValueFormatter;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;

import com.example.ca_lalay.R;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import androidx.appcompat.app.AlertDialog;
import com.example.ca_lalay.utils.ReportExportData;
import com.example.ca_lalay.utils.ReportFileExporter;

import com.example.ca_lalay.firebase.FirebaseBookingReportsLoader;
import com.example.ca_lalay.utils.BookingReportDialog;
import com.example.ca_lalay.utils.BookingsPdfExporter;
import com.example.ca_lalay.utils.BookingsReportData;

public class ShopOwnerReportPage extends AppCompatActivity {
    private PieChart bookingPieChart;
    private BarChart monthlyRevenueChart;
    private LineChart weeklyBookingChart;
    private HorizontalBarChart topServicesChart;

    private String uid;
    private Button generatePdfBtn;

    private ImageView backBtn;

    private TextView overviewBtn;
    private TextView todayBtn;
    private TextView thisWeekBtn;
    private TextView thisMonthBtn;
    private TextView thisYearBtn;

    private TextView bookingStatus, monthlyRevenue, weeklyBookingTrend, topServices;

    private String theShopName, theShopImage, theShopContactNumber, theShopLocation;

    private String selectedFilter = "overview";
    @Override
    public void onBackPressed() {
        super.onBackPressed();

        finish();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_shop_owner_report_page);
        monthlyRevenueChart =
                findViewById(R.id.monthlyRevenueChart);
        bookingPieChart = findViewById(R.id.bookingPieChart);
        weeklyBookingChart = findViewById(R.id.weeklyBookingChart);
        topServicesChart = findViewById(R.id.topServicesChart);

        bookingStatus = findViewById(R.id.bookingStatus);
        monthlyRevenue = findViewById(R.id.monthlyRevenue);
        weeklyBookingTrend = findViewById(R.id.weeklyBookingTrend);
        topServices = findViewById(R.id.topServices);

        overviewBtn = findViewById(R.id.overviewBtn);
        todayBtn = findViewById(R.id.todayBtn);
        thisWeekBtn = findViewById(R.id.thisWeekBtn);
        thisMonthBtn = findViewById(R.id.thisMonthBtn);
        thisYearBtn = findViewById(R.id.thisYearBtn);

        backBtn = findViewById(R.id.backBtn);

        generatePdfBtn = findViewById(R.id.generatePdfBtn);
        uid = FirebaseAuthManager.getCurrentUserUid();
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        getShopData();
        setupPieChart();
        setupMonthlyRevenueChart();
        setupWeeklyBookingChart();
        displayTopServicesAndLoadData();
        setupBasicData();

        overviewBtn.setOnClickListener(v -> {
            selectFilter(overviewBtn);
            selectedFilter = "overview";

            setupPieChart();
            setupMonthlyRevenueChart();
            setupWeeklyBookingChart();
            displayTopServicesAndLoadData();
            setupBasicData();

            bookingStatus.setText("Booking Status");
            monthlyRevenue.setText("Monthly Revenue");
            weeklyBookingTrend.setText("Weekly Booking Trend");
            topServices.setText("Top Services");
        });

        backBtn.setOnClickListener(s -> {
            finish();
        });

        todayBtn.setOnClickListener(v -> {
            selectFilter(todayBtn);
            selectedFilter = "today";

            setupPieChart();
            displayDailyRevenueChartAndLoadData();
            displayDailyBookingTrendAndLoadData();
            displayDailyTopServicesAndLoadData();

            bookingStatus.setText("Today's Bookings Status");
            monthlyRevenue.setText("Today's Revenue");
            weeklyBookingTrend.setText("Today's Booking Trend");
            topServices.setText("Today's Top Services");
        });

        thisWeekBtn.setOnClickListener(v -> {
            selectFilter(thisWeekBtn);
            selectedFilter = "weekly";

            displayWeeklyPieChartAndLoadData();
            displayWeeklyRevenueChartAndLoadData();
            setupWeeklyBookingChart();
            displayWeeklyTopServicesAndLoadData();

            bookingStatus.setText("Weekly Bookings Status");
            monthlyRevenue.setText("Weekly Revenue");
            weeklyBookingTrend.setText("Weekly Booking Trend");
            topServices.setText("Weekly Top Services");
        });

        thisMonthBtn.setOnClickListener(v -> {
            selectFilter(thisMonthBtn);
            selectedFilter = "monthly";

            displayMonthlyPieChartAndLoadData();
            setupMonthlyRevenueChart();
            displayMonthlyBookingTrendAndLoadData();
            displayMonthlyTopServicesAndLoadData();

            bookingStatus.setText("Monthly Bookings Status");
            monthlyRevenue.setText("Monthly Revenue");
            weeklyBookingTrend.setText("Monthly Booking Trend");
            topServices.setText("Monthly Top Services");
        });

        thisYearBtn.setOnClickListener(v -> {
            selectFilter(thisYearBtn);
            selectedFilter = "yearly";

            displayYearlyPieChartAndLoadData();
            setupMonthlyRevenueChart();
            displayMonthlyBookingTrendAndLoadData();
            displayYearlyTopServicesAndLoadData();

            bookingStatus.setText("Yearly Bookings Status");
            monthlyRevenue.setText("Yearly Revenue");
            weeklyBookingTrend.setText("Yearly Booking Trend");
            topServices.setText("Yearly Top Services");
        });

        generatePdfBtn.setOnClickListener(v -> showExportOptions());
    }
    private void generateYearlyPDF() {

        String yearKey = String.valueOf(
                Calendar.getInstance().get(Calendar.YEAR)
        );

        FirebaseReportsManager.getYearlyReport(
                uid,
                yearKey,
                new OnReportCallback() {

                    @Override
                    public void onSuccess(BookingReportModel report) {

                        FirebaseServiceReportsManager.getYearlyServiceReports(
                                uid,
                                yearKey,
                                new OnServiceReportsCallback() {

                                    @Override
                                    public void onSuccess(ArrayList<ServiceReportModel> services) {

                                        PDFReportManager.generateReport(
                                                ShopOwnerReportPage.this,
                                                "Yearly Report",
                                                report,
                                                services,theShopName,
                                                theShopContactNumber,
                                                theShopLocation
                                        );

                                    }

                                    @Override
                                    public void onFailure(String message) {

                                        Toast.makeText(
                                                ShopOwnerReportPage.this,
                                                message,
                                                Toast.LENGTH_SHORT
                                        ).show();

                                    }

                                });

                    }

                    @Override
                    public void onFailure(String message) {

                        Toast.makeText(
                                ShopOwnerReportPage.this,
                                message,
                                Toast.LENGTH_SHORT
                        ).show();

                    }

                });

    }
    private void generateMonthlyPDF() {

        String monthKey = new SimpleDateFormat(
                "yyyy-MM",
                Locale.getDefault()
        ).format(new Date());

        FirebaseReportsManager.getMonthlyReport(
                uid,
                monthKey,
                new OnReportCallback() {

                    @Override
                    public void onSuccess(BookingReportModel report) {

                        FirebaseServiceReportsManager.getMonthlyServiceReports(
                                uid,
                                monthKey,
                                new OnServiceReportsCallback() {

                                    @Override
                                    public void onSuccess(ArrayList<ServiceReportModel> services) {

                                        PDFReportManager.generateReport(
                                                ShopOwnerReportPage.this,
                                                "Monthly Report",
                                                report,
                                                services,theShopName,
                                                theShopContactNumber,
                                                theShopLocation
                                        );

                                    }

                                    @Override
                                    public void onFailure(String message) {

                                        Toast.makeText(
                                                ShopOwnerReportPage.this,
                                                message,
                                                Toast.LENGTH_SHORT
                                        ).show();

                                    }

                                });

                    }

                    @Override
                    public void onFailure(String message) {

                        Toast.makeText(
                                ShopOwnerReportPage.this,
                                message,
                                Toast.LENGTH_SHORT
                        ).show();

                    }

                });

    }
    private void generateTodayPDF() {

        String today = new SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.getDefault()
        ).format(new Date());

        FirebaseReportsManager.getDailyReport(
                uid,
                today,
                new OnReportCallback() {

                    @Override
                    public void onSuccess(BookingReportModel report) {

                        FirebaseServiceReportsManager.getDailyServiceReports(
                                uid,
                                today,
                                new OnServiceReportsCallback() {

                                    @Override
                                    public void onSuccess(ArrayList<ServiceReportModel> services) {

                                        PDFReportManager.generateReport(
                                                ShopOwnerReportPage.this,
                                                "Today's Report",
                                                report,
                                                services,theShopName,
                                                theShopContactNumber,
                                                theShopLocation
                                        );

                                    }

                                    @Override
                                    public void onFailure(String message) {
                                        Toast.makeText(ShopOwnerReportPage.this, message, Toast.LENGTH_SHORT).show();
                                    }

                                });

                    }

                    @Override
                    public void onFailure(String message) {

                    }

                });

    }
    private void generateWeeklyPDF() {

        String weekKey = FirebaseReportsManager.getCurrentWeekKey();

        FirebaseReportsManager.getWeeklyReport(
                uid,
                weekKey,
                new OnReportCallback() {

                    @Override
                    public void onSuccess(BookingReportModel report) {

                        FirebaseServiceReportsManager.getWeeklyServiceReports(
                                uid,
                                weekKey,
                                new OnServiceReportsCallback() {

                                    @Override
                                    public void onSuccess(ArrayList<ServiceReportModel> services) {

                                        PDFReportManager.generateReport(
                                                ShopOwnerReportPage.this,
                                                "Weekly Report",
                                                report,
                                                services,theShopName,
                                                theShopContactNumber,
                                                theShopLocation
                                        );

                                    }

                                    @Override
                                    public void onFailure(String message) {

                                        Toast.makeText(
                                                ShopOwnerReportPage.this,
                                                message,
                                                Toast.LENGTH_SHORT
                                        ).show();

                                    }

                                });

                    }

                    @Override
                    public void onFailure(String message) {

                        Toast.makeText(
                                ShopOwnerReportPage.this,
                                message,
                                Toast.LENGTH_SHORT
                        ).show();

                    }

                });

    }
    private void generateOverviewPDF() {

        FirebaseReportsManager.getOverallReport(
                uid,
                new OnReportCallback() {

                    @Override
                    public void onSuccess(BookingReportModel report) {

                        int currentYear = Calendar.getInstance().get(Calendar.YEAR);

                        FirebaseServiceReportsManager.getYearlyServiceReports(
                                uid,
                                String.valueOf(currentYear),
                                new OnServiceReportsCallback() {

                                    @Override
                                    public void onSuccess(ArrayList<ServiceReportModel> services) {

                                        PDFReportManager.generateReport(
                                                ShopOwnerReportPage.this,
                                                "Overall Business Report",
                                                report,
                                                services,theShopName,
                                                theShopContactNumber,
                                                theShopLocation
                                        );

                                    }

                                    @Override
                                    public void onFailure(String message) {

                                        Toast.makeText(
                                                ShopOwnerReportPage.this,
                                                message,
                                                Toast.LENGTH_SHORT
                                        ).show();

                                    }

                                });

                    }

                    @Override
                    public void onFailure(String message) {

                        Toast.makeText(
                                ShopOwnerReportPage.this,
                                message,
                                Toast.LENGTH_SHORT
                        ).show();

                    }

                });

    }
    private void selectFilter(TextView selectedButton) {

        TextView[] buttons = {
                overviewBtn,
                todayBtn,
                thisWeekBtn,
                thisMonthBtn,
                thisYearBtn
        };

        // Reset all buttons
        for (TextView button : buttons) {
            button.setBackgroundResource(R.drawable.rating_filter_bg);
            button.setTextColor(ContextCompat.getColor(this, R.color.semiblack));
        }

        // Highlight selected button
        selectedButton.setBackgroundResource(R.drawable.rating_filter_bg_selected);
        selectedButton.setTextColor(ContextCompat.getColor(this, R.color.white));
    }
    private void setupBasicData() {
        FirebaseReportsManager.getOverallReport(uid, new OnReportCallback() {
            @Override
            public void onSuccess(BookingReportModel report) {
                String revenue = String.valueOf(report.getTotalIncome());

            }

            @Override
            public void onFailure(String message) {

            }
        });
    }

    private void setupPieChart() {

        bookingPieChart.setUsePercentValues(true);

        bookingPieChart.getDescription().setEnabled(false);

        bookingPieChart.setDrawHoleEnabled(true);

        bookingPieChart.setHoleRadius(60f);

        bookingPieChart.setTransparentCircleRadius(65f);

        bookingPieChart.setCenterText("Bookings");

        bookingPieChart.setCenterTextSize(18f);

        bookingPieChart.setRotationEnabled(true);

        bookingPieChart.setHighlightPerTapEnabled(true);

        bookingPieChart.setEntryLabelTextSize(12f);

        bookingPieChart.setEntryLabelColor(Color.WHITE);


        Legend legend = bookingPieChart.getLegend();

        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);

        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);

        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);

        legend.setDrawInside(false);

        legend.setTextSize(12f);

        displayPieChartAndLoadData();

    }

    private void displayPieChart(BookingReportModel report) {

        ArrayList<PieEntry> entries = new ArrayList<>();

        int inProgress = Math.max(
                0,
                report.getTotalBookings()
                        - report.getCompletedBookings()
                        - report.getPendingBookings()
                        - report.getCancelledBookings()
        );

        if (report.getCompletedBookings() > 0) {
            entries.add(new PieEntry(
                    report.getCompletedBookings(),
                    "Completed"
            ));
        }

        if (report.getPendingBookings() > 0) {
            entries.add(new PieEntry(
                    report.getPendingBookings(),
                    "Pending"
            ));
        }

        if (inProgress > 0) {
            entries.add(new PieEntry(
                    inProgress,
                    "In Progress"
            ));
        }

        if (report.getCancelledBookings() > 0) {
            entries.add(new PieEntry(
                    report.getCancelledBookings(),
                    "Cancelled"
            ));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");

        dataSet.setColors(
                Color.parseColor("#EA5050"), // Completed
                Color.parseColor("#FFB3B3"), // Pending
                Color.parseColor("#C62828"), // In Progress
                Color.parseColor("#7F0000")  // Cancelled
        );

        dataSet.setSliceSpace(3f);

        dataSet.setSelectionShift(8f);

        dataSet.setValueTextColor(Color.WHITE);

        dataSet.setValueTextSize(13f);

        PieData data = new PieData(dataSet);

        data.setValueFormatter(new PercentFormatter(bookingPieChart));

        bookingPieChart.setCenterText(
                String.valueOf(report.getTotalBookings())
        );

        bookingPieChart.setData(data);

        bookingPieChart.animateY(1000);

        bookingPieChart.invalidate();

    }

    private void displayPieChartAndLoadData() {
        String today = new SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.getDefault()
        ).format(new Date());
        FirebaseReportsManager.getDailyReport(uid, today, new OnReportCallback() {
            @Override
            public void onSuccess(BookingReportModel report) {
                displayPieChart(report);
            }

            @Override
            public void onFailure(String message) {

            }
        });
    }

    private void setupMonthlyRevenueChart() {

        monthlyRevenueChart.getDescription().setEnabled(false);

        monthlyRevenueChart.setFitBars(true);

        monthlyRevenueChart.setDrawGridBackground(false);

        monthlyRevenueChart.setDrawBarShadow(false);

        monthlyRevenueChart.setPinchZoom(false);

        monthlyRevenueChart.setScaleEnabled(false);

        monthlyRevenueChart.animateY(1000);

        Legend legend = monthlyRevenueChart.getLegend();

        legend.setEnabled(false);

        XAxis xAxis = monthlyRevenueChart.getXAxis();

        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        xAxis.setGranularity(1f);

        xAxis.setDrawGridLines(false);

        xAxis.setDrawAxisLine(true);

        YAxis leftAxis = monthlyRevenueChart.getAxisLeft();

        leftAxis.setDrawGridLines(true);

        leftAxis.setAxisMinimum(0f);
        leftAxis.setValueFormatter(new PesoValueFormatter());

        monthlyRevenueChart.getAxisRight().setEnabled(false);

        displayBarChartAndLoadData();

    }

    private void displayMonthlyRevenueChart(ArrayList<BookingReportModel> reports) {

        ArrayList<BarEntry> entries = new ArrayList<>();

        ArrayList<String> labels = new ArrayList<>();

        float[] monthlyRevenue = new float[12];

        labels.add("Jan");
        labels.add("Feb");
        labels.add("Mar");
        labels.add("Apr");
        labels.add("May");
        labels.add("Jun");
        labels.add("Jul");
        labels.add("Aug");
        labels.add("Sep");
        labels.add("Oct");
        labels.add("Nov");
        labels.add("Dec");

// Fill the correct month with revenue
        for (BookingReportModel report : reports) {

            String period = report.getPeriod(); // Example: 2026-07

            int month = Integer.parseInt(period.substring(5, 7));

            monthlyRevenue[month - 1] = (float) report.getTotalIncome();

        }

// Create 12 bars
        for (int i = 0; i < 12; i++) {

            entries.add(new BarEntry(i, monthlyRevenue[i]));

        }

        BarDataSet dataSet = new BarDataSet(entries, "Revenue");

        dataSet.setColor(Color.parseColor("#EA5050"));

        dataSet.setValueTextSize(11f);

        BarData data = new BarData(dataSet);
        data.setValueFormatter(new PesoValueFormatter());

        data.setBarWidth(0.6f);

        monthlyRevenueChart.setData(data);

        XAxis xAxis = monthlyRevenueChart.getXAxis();

        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));

        xAxis.setLabelCount(labels.size());

        monthlyRevenueChart.invalidate();

    }

    private void displayBarChartAndLoadData() {
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);

        FirebaseReportsManager.getMonthlyReports(
                uid,
                currentYear,
                new OnReportsListCallback() {
                    @Override
                    public void onSuccess(ArrayList<BookingReportModel> reports) {

                        displayMonthlyRevenueChart(reports);

                    }

                    @Override
                    public void onFailure(String message) {

                    }
                });
    }
    private void setupWeeklyBookingChart() {

        weeklyBookingChart.getDescription().setEnabled(false);

        weeklyBookingChart.setDrawGridBackground(false);

        weeklyBookingChart.setTouchEnabled(true);

        weeklyBookingChart.setDragEnabled(false);

        weeklyBookingChart.setScaleEnabled(false);

        weeklyBookingChart.setPinchZoom(false);

        weeklyBookingChart.animateX(1000);

        Legend legend = weeklyBookingChart.getLegend();

        legend.setEnabled(false);

        XAxis xAxis = weeklyBookingChart.getXAxis();

        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        xAxis.setGranularity(1f);

        xAxis.setDrawGridLines(false);

        xAxis.setValueFormatter(
                new IndexAxisValueFormatter(
                        new String[]{
                                "Mon",
                                "Tue",
                                "Wed",
                                "Thu",
                                "Fri",
                                "Sat",
                                "Sun"
                        }));

        YAxis left = weeklyBookingChart.getAxisLeft();

        left.setAxisMinimum(0);

        left.setGranularity(1f);

        weeklyBookingChart.getAxisRight().setEnabled(false);

        loadLineChartAndLoadData();

    }
    private void displayWeeklyBookingChart(ArrayList<BookingReportModel> reports) {

        ArrayList<Entry> entries = new ArrayList<>();

        ArrayList<String> labels = new ArrayList<>();

        float[] bookings = new float[7];

        LocalDate today = LocalDate.now();
        LocalDate start = today.minusDays(6);

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("EEE", Locale.getDefault());

        // Create labels for the last 7 days
        for (int i = 0; i < 7; i++) {

            LocalDate date = start.plusDays(i);

            labels.add(date.format(formatter));

        }

        // Fill the booking counts
        for (BookingReportModel report : reports) {

            LocalDate reportDate = LocalDate.parse(report.getPeriod());

            long index = ChronoUnit.DAYS.between(start, reportDate);

            if (index >= 0 && index < 7) {

                bookings[(int) index] = report.getTotalBookings();

            }

        }

        // Create entries
        for (int i = 0; i < 7; i++) {

            entries.add(new Entry(i, bookings[i]));

        }

        LineDataSet dataSet = new LineDataSet(entries, "Bookings");

        dataSet.setColor(Color.parseColor("#EA5050"));

        dataSet.setCircleColor(Color.parseColor("#3F51B5"));

        dataSet.setLineWidth(3f);

        dataSet.setCircleRadius(5f);

        dataSet.setDrawFilled(true);

        dataSet.setDrawValues(true);

        dataSet.setValueTextSize(11f);

        LineData data = new LineData(dataSet);

        weeklyBookingChart.setData(data);

        XAxis xAxis = weeklyBookingChart.getXAxis();

        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));

        xAxis.setGranularity(1f);

        xAxis.setLabelCount(7);

        weeklyBookingChart.animateX(1000);

        weeklyBookingChart.invalidate();

    }
    private void loadLineChartAndLoadData() {
        FirebaseReportsManager.getLast7DaysReports(
                uid,
                new OnReportsListCallback() {

                    @Override
                    public void onSuccess(ArrayList<BookingReportModel> reports) {

                        displayWeeklyBookingChart(reports);

                    }

                    @Override
                    public void onFailure(String message) {

                    }
                });
    }
    private void displayTopServicesChart(ArrayList<ServiceReportModel> reports) {

        if (reports == null) {
            reports = new ArrayList<>();
        }

        ArrayList<BarEntry> entries = new ArrayList<>();

        ArrayList<String> labels = new ArrayList<>();

        for(int i = 0; i < reports.size(); i++){

            ServiceReportModel report = reports.get(i);

            entries.add(
                    new BarEntry(
                            i,
                            report.getTotalBookings()
                    )
            );

            labels.add(report.getServiceName());

        }

        BarDataSet dataSet =
                new BarDataSet(entries, "Completed Services");

        dataSet.setColor(Color.parseColor("#EA5050"));

        dataSet.setValueTextSize(12f);

        BarData data = new BarData(dataSet);

        topServicesChart.setData(data);

        XAxis xAxis = topServicesChart.getXAxis();

        xAxis.setGranularity(1f);

        xAxis.setValueFormatter(
                new IndexAxisValueFormatter(labels)
        );

        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        xAxis.setDrawGridLines(false);

        topServicesChart.getDescription().setEnabled(false);

        topServicesChart.getAxisRight().setEnabled(false);

        topServicesChart.animateY(1000);

        topServicesChart.invalidate();


    }
    private void displayTopServicesAndLoadData() {
        String month =
                LocalDate.now().getYear() + "-" +
                        String.format("%02d", LocalDate.now().getMonthValue());

        FirebaseServiceReportsManager.getMonthlyServiceReports(
                uid,
                month,
                new OnServiceReportsCallback() {

                    @Override
                    public void onSuccess(ArrayList<ServiceReportModel> reports) {

                        displayTopServicesChart(reports);

                    }

                    @Override
                    public void onFailure(String message) {

                    }

                });
    }
    private void displayDailyRevenueChart(BookingReportModel report) {

        ArrayList<BarEntry> entries = new ArrayList<>();

        entries.add(
                new BarEntry(
                        0,
                        (float) report.getTotalIncome()
                )
        );

        BarDataSet dataSet =
                new BarDataSet(entries, "Today's Revenue");

        dataSet.setValueTextSize(12f);

        BarData data = new BarData(dataSet);

        data.setBarWidth(0.5f);

        monthlyRevenueChart.setData(data);

        XAxis xAxis = monthlyRevenueChart.getXAxis();

        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        xAxis.setGranularity(1f);

        xAxis.setDrawGridLines(false);

        xAxis.setValueFormatter(
                new IndexAxisValueFormatter(
                        Collections.singletonList("Today")
                )
        );

        monthlyRevenueChart.getDescription().setEnabled(false);

        monthlyRevenueChart.getLegend().setEnabled(false);

        monthlyRevenueChart.invalidate();


    }
    private void displayDailyRevenueChartAndLoadData() {
        Log.d("DAILY_CHART", "Method called");
        String today = new SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.getDefault()
        ).format(new Date());

        FirebaseReportsManager.getDailyReport(
                uid,
                today,
                new OnReportCallback() {
                    @Override
                    public void onSuccess(BookingReportModel report) {
                        Log.d("REPORT", "Income = " + report.getTotalIncome());
                        displayDailyRevenueChart(report);

                    }

                    @Override
                    public void onFailure(String message) {

                    }
                });

    }
    private void displayDailyBookingTrendAndLoadData() {

        String today = new SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.getDefault()
        ).format(new Date());

        FirebaseReportsManager.getDailyReport(
                uid,
                today,
                new OnReportCallback() {

                    @Override
                    public void onSuccess(BookingReportModel report) {

                        displayDailyBookingTrend(report);

                    }

                    @Override
                    public void onFailure(String message) {

                    }

                });

    }
    private void displayDailyBookingTrend(BookingReportModel report) {

        ArrayList<Entry> entries = new ArrayList<>();

        entries.add(
                new Entry(
                        0,
                        report.getTotalBookings()
                )
        );

        LineDataSet dataSet =
                new LineDataSet(entries, "Today's Bookings");

        dataSet.setValueTextSize(12f);

        LineData data = new LineData(dataSet);

        weeklyBookingChart.setData(data);

        XAxis xAxis = weeklyBookingChart.getXAxis();

        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        xAxis.setGranularity(1f);

        xAxis.setDrawGridLines(false);

        xAxis.setValueFormatter(
                new IndexAxisValueFormatter(
                        Collections.singletonList("Today")
                )
        );

        weeklyBookingChart.getDescription().setEnabled(false);

        weeklyBookingChart.getLegend().setEnabled(false);

        weeklyBookingChart.invalidate();

    }
    private void displayDailyTopServicesAndLoadData() {

        String today = new SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.getDefault()
        ).format(new Date());

        FirebaseServiceReportsManager.getDailyServiceReports(
                uid,
                today,
                new OnServiceReportsCallback() {

                    @Override
                    public void onSuccess(ArrayList<ServiceReportModel> reports) {

                        displayTopServicesChart(reports);

                    }

                    @Override
                    public void onFailure(String message) {

                        displayTopServicesChart(new ArrayList<>());

                    }

                });

    }
    private void displayWeeklyPieChartAndLoadData() {

        LocalDate today = LocalDate.now();

        WeekFields weekFields = WeekFields.ISO;

        String weekKey =
                today.getYear() +
                        "-W" +
                        today.get(weekFields.weekOfWeekBasedYear());

        FirebaseReportsManager.getWeeklyReport(
                uid,
                weekKey,
                new OnReportCallback() {

                    @Override
                    public void onSuccess(BookingReportModel report) {

                        displayPieChart(report);

                    }

                    @Override
                    public void onFailure(String message) {

                    }

                });

    }
    private void displayWeeklyRevenueChartAndLoadData() {

        FirebaseReportsManager.getLast7DaysReports(
                uid,
                new OnReportsListCallback() {

                    @Override
                    public void onSuccess(ArrayList<BookingReportModel> reports) {

                        displayWeeklyRevenueChart(reports);

                    }

                    @Override
                    public void onFailure(String message) {

                        displayWeeklyRevenueChart(new ArrayList<>());

                    }

                });

    }
    private void displayWeeklyRevenueChart(ArrayList<BookingReportModel> reports) {

        ArrayList<BarEntry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();

        for (int i = 0; i < reports.size(); i++) {

            BookingReportModel report = reports.get(i);

            entries.add(
                    new BarEntry(
                            i,
                            (float) report.getTotalIncome()
                    )
            );

            labels.add(
                    report.getPeriod().substring(5) // MM-dd
            );

        }

        BarDataSet dataSet =
                new BarDataSet(entries, "Weekly Revenue");

        dataSet.setValueTextSize(12f);

        BarData data = new BarData(dataSet);

        monthlyRevenueChart.setData(data);

        XAxis xAxis = monthlyRevenueChart.getXAxis();

        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        xAxis.setGranularity(1f);

        xAxis.setDrawGridLines(false);

        xAxis.setValueFormatter(
                new IndexAxisValueFormatter(labels)
        );

        monthlyRevenueChart.getAxisRight().setEnabled(false);

        monthlyRevenueChart.getLegend().setEnabled(false);

        monthlyRevenueChart.getDescription().setEnabled(false);

        monthlyRevenueChart.invalidate();

    }
    private void displayWeeklyTopServicesAndLoadData() {

        LocalDate today = LocalDate.now();

        WeekFields weekFields = WeekFields.ISO;

        String weekKey =
                today.getYear() +
                        "-W" +
                        today.get(weekFields.weekOfWeekBasedYear());

        FirebaseServiceReportsManager.getWeeklyServiceReports(
                uid,
                weekKey,
                new OnServiceReportsCallback() {

                    @Override
                    public void onSuccess(ArrayList<ServiceReportModel> reports) {

                        displayTopServicesChart(reports);

                    }

                    @Override
                    public void onFailure(String message) {

                        displayTopServicesChart(new ArrayList<>());

                    }

                });

    }
    private void displayMonthlyPieChartAndLoadData() {

        String currentMonth = new SimpleDateFormat(
                "yyyy-MM",
                Locale.getDefault()
        ).format(new Date());

        FirebaseReportsManager.getMonthlyReport(
                uid,
                currentMonth,
                new OnReportCallback() {

                    @Override
                    public void onSuccess(BookingReportModel report) {

                        displayPieChart(report);

                    }

                    @Override
                    public void onFailure(String message) {

                    }

                });

    }
    private void displayMonthlyBookingTrendAndLoadData() {

        int currentYear = Calendar.getInstance().get(Calendar.YEAR);

        FirebaseReportsManager.getMonthlyReports(
                uid,
                currentYear,
                new OnReportsListCallback() {

                    @Override
                    public void onSuccess(ArrayList<BookingReportModel> reports) {

                        displayMonthlyBookingTrend(reports);

                    }

                    @Override
                    public void onFailure(String message) {

                        displayMonthlyBookingTrend(new ArrayList<>());

                    }

                });

    }
    private void displayMonthlyBookingTrend(ArrayList<BookingReportModel> reports) {

        ArrayList<Entry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();

        for (int i = 0; i < reports.size(); i++) {

            BookingReportModel report = reports.get(i);

            entries.add(
                    new Entry(
                            i,
                            report.getTotalBookings()
                    )
            );

            labels.add(
                    report.getPeriod().substring(5) // 07, 08, 09...
            );

        }

        LineDataSet dataSet =
                new LineDataSet(entries, "Monthly Booking Trend");

        dataSet.setValueTextSize(10f);

        LineData data = new LineData(dataSet);

        weeklyBookingChart.setData(data);

        XAxis xAxis = weeklyBookingChart.getXAxis();

        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        xAxis.setGranularity(1f);

        xAxis.setDrawGridLines(false);

        xAxis.setValueFormatter(
                new IndexAxisValueFormatter(labels)
        );

        weeklyBookingChart.getDescription().setEnabled(false);

        weeklyBookingChart.getLegend().setEnabled(false);

        weeklyBookingChart.invalidate();

    }
    private void displayMonthlyTopServicesAndLoadData() {

        String currentMonth = new SimpleDateFormat(
                "yyyy-MM",
                Locale.getDefault()
        ).format(new Date());

        FirebaseServiceReportsManager.getMonthlyServiceReports(
                uid,
                currentMonth,
                new OnServiceReportsCallback() {

                    @Override
                    public void onSuccess(ArrayList<ServiceReportModel> reports) {

                        displayTopServicesChart(reports);

                    }

                    @Override
                    public void onFailure(String message) {

                        displayTopServicesChart(new ArrayList<>());

                    }

                });

    }
    private void displayYearlyPieChartAndLoadData() {

        String year = String.valueOf(
                Calendar.getInstance().get(Calendar.YEAR));

        FirebaseReportsManager.getYearlyReport(
                uid,
                year,
                new OnReportCallback() {

                    @Override
                    public void onSuccess(BookingReportModel report) {

                        displayPieChart(report);

                    }

                    @Override
                    public void onFailure(String message) {

                    }

                });

    }
    private void displayYearlyTopServicesAndLoadData() {

        String year = String.valueOf(
                Calendar.getInstance().get(Calendar.YEAR));

        FirebaseServiceReportsManager.getYearlyServiceReports(
                uid,
                year,
                new OnServiceReportsCallback() {

                    @Override
                    public void onSuccess(ArrayList<ServiceReportModel> reports) {

                        displayTopServicesChart(reports);

                    }

                    @Override
                    public void onFailure(String message) {

                        displayTopServicesChart(new ArrayList<>());

                    }

                });

    }
    private void getShopData() {
        FirebaseDatabaseManager.getShopOwnerData(uid, new ShopOwnerDataCallback() {
            @Override
            public void onSuccess(ShopOwnerModel data) {
                theShopName = data.getShopName();
                theShopImage = data.getShopImage();
                theShopContactNumber = data.getContactNum();
                theShopLocation = data.getLocation();
            }

            @Override
            public void onFailure(String failed) {

            }
        });
    }
    private void setLoadingState() {
        generatePdfBtn.setEnabled(false);
        generatePdfBtn.setAlpha(0.5f);
        generatePdfBtn.setText("GENERATING...");
    }

    private void setNormalState() {
        generatePdfBtn.setEnabled(true);
        generatePdfBtn.setAlpha(1f);
        generatePdfBtn.setText("Generate Report");
    }

    private static final int FORMAT_PDF = 0, FORMAT_XLSX = 1, FORMAT_CSV = 2;

    private String filterLabel() {
        switch (selectedFilter) {
            case "today":   return "Today's";
            case "weekly":  return "Weekly";
            case "monthly": return "Monthly";
            case "yearly":  return "Yearly";
            default:        return "Overall";
        }
    }

    /** Format picker shown when the button is tapped. */
    private void showExportOptions() {
        if (uid == null) {
            Toast.makeText(this, "Please log in again.", Toast.LENGTH_SHORT).show();
            return;
        }
        String[] options = {
                "PDF Report",
                "Excel Report (.xlsx)",
                "CSV Report (.csv)",
                "Detailed Bookings Report (date range / service)..."
        };
        new AlertDialog.Builder(this)
                .setTitle(filterLabel() + " Report")
                .setItems(options, (dialog, which) -> {
                    if (which == 3) {
                        startBookingsReport();
                    } else {
                        exportReport(which);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    private void startBookingsReport() {

        BookingReportDialog.show(this, uid, opts -> {

            setLoadingState();

            FirebaseBookingReportsLoader.loadBookings(
                    uid, opts.from, opts.to, opts.serviceId, opts.status,
                    new FirebaseBookingReportsLoader.BookingsListener() {

                        @Override
                        public void onLoaded(ArrayList<BookingsReportData.Row> rows) {

                            if (rows.isEmpty()) {
                                setNormalState();
                                Toast.makeText(ShopOwnerReportPage.this,
                                        "No bookings found for the selected filters.",
                                        Toast.LENGTH_LONG).show();
                                return;
                            }

                            BookingsReportData data = new BookingsReportData();
                            data.title = "Bookings Report";
                            data.shopName = theShopName != null ? theShopName : "";
                            data.contact = theShopContactNumber != null ? theShopContactNumber : "";
                            data.location = theShopLocation != null ? theShopLocation : "";
                            data.periodLabel = opts.periodLabel;
                            data.rangeLabel = opts.rangeLabel;
                            data.serviceLabel = opts.serviceName;
                            data.statusLabel = opts.statusLabel;
                            data.rows = rows;

                            new Thread(() -> {
                                try {
                                    byte[] bytes;
                                    String mime, ext;

                                    switch (opts.format) {
                                        case BookingReportDialog.FORMAT_PDF:
                                            bytes = BookingsPdfExporter.build(data);
                                            mime = "application/pdf";
                                            ext = "pdf";
                                            break;
                                        case BookingReportDialog.FORMAT_XLSX:
                                            bytes = ReportFileExporter.buildBookingsXlsx(data);
                                            mime = ReportFileExporter.MIME_XLSX;
                                            ext = "xlsx";
                                            break;
                                        default:
                                            bytes = ReportFileExporter.buildBookingsCsv(data);
                                            mime = ReportFileExporter.MIME_CSV;
                                            ext = "csv";
                                            break;
                                    }

                                    String name = ReportFileExporter.buildFileName(
                                            "Bookings_" + opts.fileTag, ext);
                                    Uri uri = ReportFileExporter.saveToDownloads(
                                            ShopOwnerReportPage.this, name, mime, bytes);

                                    runOnUiThread(() -> {
                                        setNormalState();
                                        showSavedDialog(uri, mime, name);
                                    });

                                } catch (Exception e) {
                                    runOnUiThread(() -> {
                                        setNormalState();
                                        Toast.makeText(ShopOwnerReportPage.this,
                                                "Failed to create report: " + e.getMessage(),
                                                Toast.LENGTH_LONG).show();
                                    });
                                }
                            }).start();
                        }

                        @Override
                        public void onError(String message) {
                            setNormalState();
                            Toast.makeText(ShopOwnerReportPage.this, message, Toast.LENGTH_LONG).show();
                        }
                    });
        });
    }

    private void exportReport(int format) {
        setLoadingState();

        loadReportData(selectedFilter, new ReportExportData.Listener() {
            @Override
            public void onLoaded(ReportExportData data) {
                if (format == FORMAT_PDF) {
                    setNormalState();
                    PDFReportManager.generateReport(
                            ShopOwnerReportPage.this,
                            data.title,
                            data.report,
                            data.services,
                            data.shopName,
                            data.contact,
                            data.location
                    );
                    return;
                }

                // Excel / CSV: build the file off the main thread
                new Thread(() -> {
                    try {
                        boolean xlsx = format == FORMAT_XLSX;
                        byte[] bytes = xlsx ? ReportFileExporter.buildXlsx(data)
                                : ReportFileExporter.buildCsv(data);
                        String mime = xlsx ? ReportFileExporter.MIME_XLSX : ReportFileExporter.MIME_CSV;
                        String name = ReportFileExporter.buildFileName(data.title, xlsx ? "xlsx" : "csv");
                        Uri uri = ReportFileExporter.saveToDownloads(ShopOwnerReportPage.this, name, mime, bytes);

                        runOnUiThread(() -> {
                            setNormalState();
                            showSavedDialog(uri, mime, name);
                        });
                    } catch (Exception e) {
                        runOnUiThread(() -> {
                            setNormalState();
                            Toast.makeText(ShopOwnerReportPage.this,
                                    "Failed to create report: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        });
                    }
                }).start();
            }

            @Override
            public void onError(String message) {
                setNormalState();
                Toast.makeText(ShopOwnerReportPage.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showSavedDialog(Uri uri, String mime, String fileName) {
        new AlertDialog.Builder(this)
                .setTitle("Report saved")
                .setMessage(fileName + "\n\nSaved in Downloads/" + ReportFileExporter.FOLDER)
                .setPositiveButton("Open", (d, w) -> {
                    Intent open = new Intent(Intent.ACTION_VIEW);
                    open.setDataAndType(uri, mime);
                    open.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    try {
                        startActivity(open);
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(this, "No app found to open this file. Try Share.", Toast.LENGTH_LONG).show();
                    }
                })
                .setNeutralButton("Share", (d, w) -> {
                    Intent send = new Intent(Intent.ACTION_SEND);
                    send.setType(mime);
                    send.putExtra(Intent.EXTRA_STREAM, uri);
                    send.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(Intent.createChooser(send, "Share report"));
                })
                .setNegativeButton("Close", null)
                .show();
    }

    /**
     * Loads everything a report needs for the given filter.
     * Mirrors what the old generateXPDF() methods fetched, plus an optional breakdown.
     */
    private void loadReportData(String filter, ReportExportData.Listener listener) {

        final ReportExportData data = new ReportExportData();
        data.shopName = theShopName != null ? theShopName : "";
        data.contact = theShopContactNumber != null ? theShopContactNumber : "";
        data.location = theShopLocation != null ? theShopLocation : "";

        final int year = Calendar.getInstance().get(Calendar.YEAR);
        final String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        final String monthKey = new SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(new Date());
        final String weekKey = FirebaseReportsManager.getCurrentWeekKey();
        final String yearKey = String.valueOf(year);

        OnReportCallback reportCallback = new OnReportCallback() {
            @Override
            public void onSuccess(BookingReportModel report) {
                data.report = report;
                loadServicesForExport(filter, data, listener, today, weekKey, monthKey, yearKey, year);
            }

            @Override
            public void onFailure(String message) {
                listener.onError(message);
            }
        };

        switch (filter) {
            case "today":
                data.title = "Today's Report";
                data.periodLabel = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(new Date());
                FirebaseReportsManager.getDailyReport(uid, today, reportCallback);
                break;
            case "weekly":
                data.title = "Weekly Report";
                data.periodLabel = weekKey;
                FirebaseReportsManager.getWeeklyReport(uid, weekKey, reportCallback);
                break;
            case "monthly":
                data.title = "Monthly Report";
                data.periodLabel = new SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(new Date());
                FirebaseReportsManager.getMonthlyReport(uid, monthKey, reportCallback);
                break;
            case "yearly":
                data.title = "Yearly Report";
                data.periodLabel = yearKey;
                FirebaseReportsManager.getYearlyReport(uid, yearKey, reportCallback);
                break;
            default:
                data.title = "Overall Business Report";
                data.periodLabel = "All time";
                FirebaseReportsManager.getOverallReport(uid, reportCallback);
                break;
        }
    }

    private void loadServicesForExport(String filter, ReportExportData data,
                                       ReportExportData.Listener listener,
                                       String today, String weekKey, String monthKey,
                                       String yearKey, int year) {

        OnServiceReportsCallback serviceCallback = new OnServiceReportsCallback() {
            @Override
            public void onSuccess(ArrayList<ServiceReportModel> services) {
                data.services = services != null ? services : new ArrayList<>();
                loadBreakdownForExport(filter, data, listener, year);
            }

            @Override
            public void onFailure(String message) {
                listener.onError(message);
            }
        };

        switch (filter) {
            case "today":
                FirebaseServiceReportsManager.getDailyServiceReports(uid, today, serviceCallback);
                break;
            case "weekly":
                FirebaseServiceReportsManager.getWeeklyServiceReports(uid, weekKey, serviceCallback);
                break;
            case "monthly":
                FirebaseServiceReportsManager.getMonthlyServiceReports(uid, monthKey, serviceCallback);
                break;
            default: // yearly + overview (same as your old overview PDF)
                FirebaseServiceReportsManager.getYearlyServiceReports(uid, yearKey, serviceCallback);
                break;
        }
    }

    private void loadBreakdownForExport(String filter, ReportExportData data,
                                        ReportExportData.Listener listener, int year) {

        if ("weekly".equals(filter)) {
            data.breakdownTitle = "Daily Breakdown";
            FirebaseReportsManager.getLast7DaysReports(uid, new OnReportsListCallback() {
                @Override
                public void onSuccess(ArrayList<BookingReportModel> reports) {
                    data.breakdown = reports;
                    listener.onLoaded(data);
                }

                @Override
                public void onFailure(String message) {
                    listener.onLoaded(data); // breakdown is optional
                }
            });

        } else if ("yearly".equals(filter) || "overview".equals(filter)) {
            data.breakdownTitle = "Monthly Breakdown";
            FirebaseReportsManager.getMonthlyReports(uid, year, new OnReportsListCallback() {
                @Override
                public void onSuccess(ArrayList<BookingReportModel> reports) {
                    data.breakdown = reports;
                    listener.onLoaded(data);
                }

                @Override
                public void onFailure(String message) {
                    listener.onLoaded(data); // breakdown is optional
                }
            });

        } else {
            listener.onLoaded(data); // today / monthly: no breakdown yet
        }
    }

}