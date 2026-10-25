package com.example.ca_lalay.model;

import java.util.Calendar;

public class ShopOpeningDaysModel {
    private boolean monday = true;
    private boolean tuesday = true;
    private boolean wednesday = true;
    private boolean thursday = true;
    private boolean friday = true;
    private boolean saturday = true;
    private boolean sunday = true;

    public ShopOpeningDaysModel() {
        // Required empty constructor for Firebase
    }

    public boolean isMonday() { return monday; }
    public void setMonday(boolean monday) { this.monday = monday; }

    public boolean isTuesday() { return tuesday; }
    public void setTuesday(boolean tuesday) { this.tuesday = tuesday; }

    public boolean isWednesday() { return wednesday; }
    public void setWednesday(boolean wednesday) { this.wednesday = wednesday; }

    public boolean isThursday() { return thursday; }
    public void setThursday(boolean thursday) { this.thursday = thursday; }

    public boolean isFriday() { return friday; }
    public void setFriday(boolean friday) { this.friday = friday; }

    public boolean isSaturday() { return saturday; }
    public void setSaturday(boolean saturday) { this.saturday = saturday; }

    public boolean isSunday() { return sunday; }
    public void setSunday(boolean sunday) { this.sunday = sunday; }

    /**
     * Pass in Calendar.DAY_OF_WEEK (Calendar.MONDAY, Calendar.SUNDAY, etc.)
     */
    public boolean isOpenOn(int calendarDayOfWeek) {
        switch (calendarDayOfWeek) {
            case Calendar.MONDAY:    return monday;
            case Calendar.TUESDAY:   return tuesday;
            case Calendar.WEDNESDAY: return wednesday;
            case Calendar.THURSDAY:  return thursday;
            case Calendar.FRIDAY:    return friday;
            case Calendar.SATURDAY:  return saturday;
            case Calendar.SUNDAY:    return sunday;
            default: return true;
        }
    }
}