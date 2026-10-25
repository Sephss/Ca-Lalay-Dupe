package com.example.ca_lalay.firebase;

import com.example.ca_lalay.firebase.callback.ShopClosedDatesListCallback;
import com.example.ca_lalay.firebase.callback.ShopOpeningDaysCallback;
import com.example.ca_lalay.firebase.callback.ShopScheduleCallback;
import com.example.ca_lalay.model.ShopClosedDateModel;
import com.example.ca_lalay.model.ShopOpeningDaysModel;
import com.example.ca_lalay.model.ShopScheduleResult;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FirebaseShopScheduleHelper {

    private static final String[] DAY_NAMES = {
            "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"
    };

    /**
     * Fetches a shop's full schedule: recurring weekly opening days plus any
     * upcoming one-off closed dates (e.g. renovation, holiday), and returns
     * both raw and pre-formatted results in a single callback.
     */
    public static void getShopSchedule(String shopId, ShopScheduleCallback callback) {

        ShopScheduleResult result = new ShopScheduleResult();

        // Track completion of both async calls before firing the final callback
        boolean[] completedFlags = new boolean[2]; // [0] = opening days, [1] = closed dates
        String[] failureHolder = new String[1];

        Runnable checkDone = () -> {
            if (completedFlags[0] && completedFlags[1]) {
                if (failureHolder[0] != null) {
                    callback.onFailure(failureHolder[0]);
                } else {
                    callback.onSuccess(result);
                }
            }
        };

        FirebaseShopOpeningDaysManager.getOpeningDays(shopId, new ShopOpeningDaysCallback() {
            @Override
            public void onSuccess(ShopOpeningDaysModel openingDays) {
                result.setOpeningDays(openingDays);
                result.setFormattedWeeklySchedule(buildOpenDaysString(openingDays));
                result.setFormattedClosedDaysOfWeek(buildClosedDaysString(openingDays));
                completedFlags[0] = true;
                checkDone.run();
            }

            @Override
            public void onFailure(String errorMessage) {
                failureHolder[0] = errorMessage;
                completedFlags[0] = true;
                checkDone.run();
            }
        });

        FirebaseShopClosedDatesManager.getAllClosedDates(shopId, new ShopClosedDatesListCallback() {
            @Override
            public void onSuccess(List<ShopClosedDateModel> closedDates) {
                result.setClosedDates(closedDates);
                completedFlags[1] = true;
                checkDone.run();
            }

            @Override
            public void onFailure(String errorMessage) {
                failureHolder[0] = errorMessage;
                completedFlags[1] = true;
                checkDone.run();
            }
        });
    }

    private static String buildOpenDaysString(ShopOpeningDaysModel days) {
        List<String> open = new ArrayList<>();
        if (days.isMonday()) open.add("Mon");
        if (days.isTuesday()) open.add("Tue");
        if (days.isWednesday()) open.add("Wed");
        if (days.isThursday()) open.add("Thu");
        if (days.isFriday()) open.add("Fri");
        if (days.isSaturday()) open.add("Sat");
        if (days.isSunday()) open.add("Sun");

        if (open.isEmpty()) return "Closed all week";
        return String.join(", ", open);
    }

    private static String buildClosedDaysString(ShopOpeningDaysModel days) {
        List<String> closed = new ArrayList<>();
        if (!days.isMonday()) closed.add("Mon");
        if (!days.isTuesday()) closed.add("Tue");
        if (!days.isWednesday()) closed.add("Wed");
        if (!days.isThursday()) closed.add("Thu");
        if (!days.isFriday()) closed.add("Fri");
        if (!days.isSaturday()) closed.add("Sat");
        if (!days.isSunday()) closed.add("Sun");

        if (closed.isEmpty()) return "None";
        return String.join(", ", closed);
    }
}