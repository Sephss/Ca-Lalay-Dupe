package com.example.ca_lalay.model;

public class BookingReportModel {

    private String period;

    private int totalBookings;
    private int completedBookings;
    private int cancelledBookings;
    private int pendingBookings;

    private double totalIncome;

    public BookingReportModel() {
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public int getTotalBookings() {
        return totalBookings;
    }

    public void setTotalBookings(int totalBookings) {
        this.totalBookings = totalBookings;
    }

    public int getCompletedBookings() {
        return completedBookings;
    }

    public void setCompletedBookings(int completedBookings) {
        this.completedBookings = completedBookings;
    }

    public int getCancelledBookings() {
        return cancelledBookings;
    }

    public void setCancelledBookings(int cancelledBookings) {
        this.cancelledBookings = cancelledBookings;
    }

    public int getPendingBookings() {
        return pendingBookings;
    }

    public void setPendingBookings(int pendingBookings) {
        this.pendingBookings = pendingBookings;
    }

    public double getTotalIncome() {
        return totalIncome;
    }

    public void setTotalIncome(double totalIncome) {
        this.totalIncome = totalIncome;
    }

    public BookingReportModel(String period,
                              int totalBookings,
                              int completedBookings,
                              int cancelledBookings,
                              int pendingBookings,
                              double totalIncome) {

        this.period = period;
        this.totalBookings = totalBookings;
        this.completedBookings = completedBookings;
        this.cancelledBookings = cancelledBookings;
        this.pendingBookings = pendingBookings;
        this.totalIncome = totalIncome;
    }

    // getters & setters
}