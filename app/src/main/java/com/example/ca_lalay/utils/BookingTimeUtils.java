package com.example.ca_lalay.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Helper functions for turning Ca-Lalay's existing string-based date/time/duration
 * fields into epoch-millisecond values so capacity/overlap math is reliable.
 *
 * Existing storage formats (unchanged):
 *   serviceDate   -> "yyyy-MM-dd"            e.g. "2026-09-15"
 *   serviceTime   -> "hh:mm a"               e.g. "01:00 pm"
 *   shopOpenAt    -> "hh:mm a"               e.g. "04:55 PM"
 *   shopClosedAt  -> "hh:mm a"               e.g. "04:55 AM"  (may be past midnight)
 *   serviceDuration -> a plain number of HOURS stored as a String, e.g. "1", "1.5", "2"
 */
public class BookingTimeUtils {

    private static SimpleDateFormat timeFormat() {
        return new SimpleDateFormat("hh:mm a", Locale.US);
    }

    private static SimpleDateFormat dateFormat() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    }

    /** Combine a "yyyy-MM-dd" date and a "hh:mm a" time into epoch millis. */
    public static long combineDateAndTime(String dateISO, String timeStr) throws ParseException {
        Calendar dateCal = Calendar.getInstance();
        dateCal.setTime(dateFormat().parse(dateISO));

        Calendar timeCal = Calendar.getInstance();
        timeCal.setTime(timeFormat().parse(timeStr));

        Calendar result = Calendar.getInstance();
        result.clear();
        result.set(
                dateCal.get(Calendar.YEAR),
                dateCal.get(Calendar.MONTH),
                dateCal.get(Calendar.DAY_OF_MONTH),
                timeCal.get(Calendar.HOUR_OF_DAY),
                timeCal.get(Calendar.MINUTE),
                0
        );
        result.set(Calendar.MILLISECOND, 0);
        return result.getTimeInMillis();
    }

    /** serviceDuration is stored as a number of HOURS (e.g. "1" = 60 min, "1.5" = 90 min). */
    public static int durationHoursStringToMinutes(String durationHoursStr) {
        double hours = Double.parseDouble(durationHoursStr.trim());
        return (int) Math.round(hours * 60.0);
    }

    public static long minutesToMillis(int minutes) {
        return minutes * 60_000L;
    }

    /**
     * Returns {openMillis, closeMillis} for the given service date, correctly handling
     * shops whose closing time is on the following calendar day (e.g. open 4:55 PM,
     * close 4:55 AM). If close <= open on the same date, we treat close as next-day.
     */
    public static long[] getOperatingWindowMillis(String dateISO, String openTimeStr, String closeTimeStr) throws ParseException {
        long openMillis = combineDateAndTime(dateISO, openTimeStr);
        long closeMillis = combineDateAndTime(dateISO, closeTimeStr);

        if (closeMillis <= openMillis) {
            closeMillis += 24L * 60 * 60 * 1000;
        }
        return new long[]{openMillis, closeMillis};
    }

    public static boolean fitsWithinOperatingHours(long startMillis, long endMillis, long openMillis, long closeMillis) {
        return startMillis >= openMillis && endMillis <= closeMillis;
    }

    /** Half-open interval overlap test: [startA, endA) vs [startB, endB). */
    public static boolean rangesOverlap(long startA, long endA, long startB, long endB) {
        return startA < endB && startB < endA;
    }

    public static String formatTime(long millis) {
        return timeFormat().format(new Date(millis));
    }
}