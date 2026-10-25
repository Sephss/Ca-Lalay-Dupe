package com.example.ca_lalay.utils;

import com.example.ca_lalay.model.BookingReportModel;
import com.example.ca_lalay.model.ServiceReportModel;

import java.util.ArrayList;

/**
 * Everything a report needs, loaded once and reused by PDF / Excel / CSV.
 */
public class ReportExportData {

    public String title;            // e.g. "Monthly Report"
    public String periodLabel;      // e.g. "July 2026"
    public String shopName = "";
    public String contact = "";
    public String location = "";

    public BookingReportModel report;
    public ArrayList<ServiceReportModel> services = new ArrayList<>();

    // Optional per-period breakdown (7 days for weekly, 12 months for yearly/overview)
    public ArrayList<BookingReportModel> breakdown;
    public String breakdownTitle;

    public interface Listener {
        void onLoaded(ReportExportData data);

        void onError(String message);
    }
}