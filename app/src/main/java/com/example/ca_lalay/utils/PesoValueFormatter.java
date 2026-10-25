package com.example.ca_lalay.utils;

import com.github.mikephil.charting.formatter.ValueFormatter;

public class PesoValueFormatter extends ValueFormatter {

    @Override
    public String getFormattedValue(float value) {

        if (value >= 1_000_000) {
            return String.format("₱%.1fM", value / 1_000_000f);
        }

        if (value >= 1000) {
            return String.format("₱%.1fK", value / 1000f);
        }

        return String.format("₱%.0f", value);
    }
}