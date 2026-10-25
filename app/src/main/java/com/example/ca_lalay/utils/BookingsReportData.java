package com.example.ca_lalay.utils;

import java.util.ArrayList;

/**
 * Booking-level report data (one Row per booking) + filter labels.
 */
public class BookingsReportData {

    public static class Row {
        public String bookingId = "", serviceId = "", serviceName = "";
        public String customer = "", contact = "", vehicle = "", vehicleSize = "";
        public String date = "", time = "";
        public String statusRaw = "", status = "";
        public String paymentMethod = "", paid = "", billingNumber = "";
        public String addOnsDescription = "";
        public double basePrice, addOnsTotal, total;
    }

    public String title = "Bookings Report";
    public String periodLabel = "";
    public String rangeLabel = "";
    public String serviceLabel = "All Services";
    public String statusLabel = "All Statuses";
    public String shopName = "", contact = "", location = "";

    public ArrayList<Row> rows = new ArrayList<>();

    public int count(String statusRaw) {
        int n = 0;
        for (Row r : rows) if (statusRaw.equalsIgnoreCase(r.statusRaw)) n++;
        return n;
    }

    /** Same rule as your booking_reports: income only counts when the booking is done. */
    public double completedRevenue() {
        double sum = 0;
        for (Row r : rows) if ("done".equalsIgnoreCase(r.statusRaw)) sum += r.total;
        return sum;
    }

    public double totalValue() {
        double sum = 0;
        for (Row r : rows) sum += r.total;
        return sum;
    }

    public static String prettyStatus(String raw) {
        if (raw == null) return "";
        switch (raw) {
            case "done":       return "Completed";
            case "inProgress": return "In Progress";
            case "pending":    return "Pending";
            case "confirmed":  return "Confirmed";
            case "cancelled":  return "Cancelled";
            case "rejected":   return "Rejected";
            default:           return raw;
        }
    }
}