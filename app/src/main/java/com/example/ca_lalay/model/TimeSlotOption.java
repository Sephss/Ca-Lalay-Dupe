package com.example.ca_lalay.model;

/** One selectable start-time option for the customer booking screen. */
public class TimeSlotOption {

    private String label;          // e.g. "01:00 PM"
    private long startMillis;
    private long endMillis;
    private boolean available;
    private int remainingCapacity; // spaces left if this start time is chosen

    public TimeSlotOption() {
    }

    public TimeSlotOption(String label, long startMillis, long endMillis, boolean available, int remainingCapacity) {
        this.label = label;
        this.startMillis = startMillis;
        this.endMillis = endMillis;
        this.available = available;
        this.remainingCapacity = remainingCapacity;
    }

    public String getLabel() {
        return label;
    }

    public long getStartMillis() {
        return startMillis;
    }

    public long getEndMillis() {
        return endMillis;
    }

    public boolean isAvailable() {
        return available;
    }

    public int getRemainingCapacity() {
        return remainingCapacity;
    }
}