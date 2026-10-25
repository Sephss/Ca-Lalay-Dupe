package com.example.ca_lalay.model;

import java.util.List;

public class ShopScheduleResult {
    private ShopOpeningDaysModel openingDays;
    private List<ShopClosedDateModel> closedDates;
    private String formattedWeeklySchedule; // e.g. "Mon, Tue, Wed, Thu, Fri" (open days only)
    private String formattedClosedDaysOfWeek; // e.g. "Sat, Sun" (closed days only)

    public ShopOpeningDaysModel getOpeningDays() { return openingDays; }
    public void setOpeningDays(ShopOpeningDaysModel openingDays) { this.openingDays = openingDays; }

    public List<ShopClosedDateModel> getClosedDates() { return closedDates; }
    public void setClosedDates(List<ShopClosedDateModel> closedDates) { this.closedDates = closedDates; }

    public String getFormattedWeeklySchedule() { return formattedWeeklySchedule; }
    public void setFormattedWeeklySchedule(String formattedWeeklySchedule) { this.formattedWeeklySchedule = formattedWeeklySchedule; }

    public String getFormattedClosedDaysOfWeek() { return formattedClosedDaysOfWeek; }
    public void setFormattedClosedDaysOfWeek(String formattedClosedDaysOfWeek) { this.formattedClosedDaysOfWeek = formattedClosedDaysOfWeek; }
}