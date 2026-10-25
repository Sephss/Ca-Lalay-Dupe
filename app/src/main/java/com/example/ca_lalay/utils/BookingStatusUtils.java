package com.example.ca_lalay.utils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Single source of truth for which booking statuses occupy a shop's capacity.
 * Adjust this set if your status flow changes - everything else (capacity
 * transaction, available-times generator) reads from here.
 */
public class BookingStatusUtils {

    // Bookings that are still "holding a spot" for their scheduled interval.
    // "done"/"cancelled"/"rejected" are terminal and free up capacity.
    private static final Set<String> CAPACITY_CONSUMING_STATUSES = new HashSet<>(Arrays.asList(
            "pending",
            "confirmed",
            "inProgress"
    ));

    public static boolean consumesCapacity(String status) {
        if (status == null) return false;
        return CAPACITY_CONSUMING_STATUSES.contains(status);
    }
}