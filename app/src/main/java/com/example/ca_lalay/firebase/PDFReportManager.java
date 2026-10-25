package com.example.ca_lalay.firebase;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Environment;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.example.ca_lalay.model.BookingReportModel;
import com.example.ca_lalay.model.ServiceReportModel;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class PDFReportManager {

    // ---- Palette: shades of #EA5050 only ----
    private static final int COLOR_PRIMARY      = Color.parseColor("#EA5050"); // brand red
    private static final int COLOR_PRIMARY_DARK = Color.parseColor("#C23E3E"); // headings / rank numbers
    private static final int COLOR_PRIMARY_DEEP = Color.parseColor("#7A2626"); // strongest emphasis
    private static final int COLOR_TINT_LIGHT   = Color.parseColor("#FDEEEE"); // card / track fill
    private static final int COLOR_TINT         = Color.parseColor("#F6D2D2"); // slightly deeper fill
    private static final int COLOR_LINE         = Color.parseColor("#EFC6C6"); // borders / hairlines
    private static final int COLOR_TEXT         = Color.parseColor("#3A2323"); // body text (warm near-black)
    private static final int COLOR_TEXT_MUTED   = Color.parseColor("#8C6A6A"); // labels / meta
    private static final int COLOR_WHITE        = Color.parseColor("#FFFFFF");
    private static final int COLOR_TAG_ON_BAND  = Color.parseColor("#FBD9D9"); // tag text on red band

    // ---- Page: US Letter, proportional margins ----
    private static final int PAGE_WIDTH = 612;
    private static final int PAGE_HEIGHT = 792;
    private static final int MARGIN_LEFT = Math.round(PAGE_WIDTH * 0.06f);
    private static final int MARGIN_RIGHT = Math.round(PAGE_WIDTH * 0.06f);
    private static final int CONTENT_RIGHT = PAGE_WIDTH - MARGIN_RIGHT;
    private static final int CONTENT_WIDTH = PAGE_WIDTH - MARGIN_LEFT - MARGIN_RIGHT;

    private static final int TOP_SERVICES_LIMIT = 5;

    public static void generateReport(
            Context context,
            String title,
            BookingReportModel report,
            ArrayList<ServiceReportModel> services,
            String shopName,
            String shopContactNumber,
            String shopLocation
    ) {

        PdfDocument pdfDocument = new PdfDocument();

        PdfDocument.PageInfo pageInfo =
                new PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create();

        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        // ---------- Paints ----------
        Paint bandPaint = solidPaint(COLOR_PRIMARY);

        Paint shopNamePaint = textPaint(20, Typeface.DEFAULT_BOLD, COLOR_WHITE);
        Paint shopSubPaint = textPaint(10.5f, Typeface.DEFAULT, Color.parseColor("#FDE4E4"));
        Paint bandTagPaint = textPaint(10, Typeface.DEFAULT_BOLD, COLOR_TAG_ON_BAND);

        Paint titlePaint = textPaint(18, Typeface.DEFAULT_BOLD, COLOR_PRIMARY_DEEP);
        Paint metaPaint = textPaint(10, Typeface.DEFAULT, COLOR_TEXT_MUTED);

        Paint sectionHeaderPaint = textPaint(13, Typeface.DEFAULT_BOLD, COLOR_PRIMARY_DEEP);
        Paint bodyPaint = textPaint(11.5f, Typeface.DEFAULT, COLOR_TEXT);
        Paint footerPaint = textPaint(8.5f, Typeface.DEFAULT, COLOR_TEXT_MUTED);

        Paint hairline = solidPaintStroke(COLOR_LINE, 1f);
        Paint sectionUnderline = solidPaintStroke(COLOR_PRIMARY, 1.2f); // thin, not thick

        Paint cardFill = solidPaint(COLOR_TINT_LIGHT);
        Paint cardStroke = solidPaintStroke(COLOR_LINE, 1f);

        DecimalFormat money = new DecimalFormat("#,##0.00");
        DecimalFormat pct = new DecimalFormat("#0.0");

        int y;

        // ---------- Header band ----------
        int bandHeight = 92;
        canvas.drawRect(0, 0, PAGE_WIDTH, bandHeight, bandPaint);

        String displayShopName = isEmpty(shopName) ? "Ca-Lalay" : shopName;
        canvas.drawText(displayShopName, MARGIN_LEFT, 38, shopNamePaint);

        StringBuilder subLine = new StringBuilder();
        if (!isEmpty(shopLocation)) subLine.append(shopLocation);
        if (!isEmpty(shopContactNumber)) {
            if (subLine.length() > 0) subLine.append("   |   ");
            subLine.append(shopContactNumber);
        }
        if (subLine.length() > 0) {
            canvas.drawText(subLine.toString(), MARGIN_LEFT, 58, shopSubPaint);
        }

        String tag = "BUSINESS REPORT";
        float tagWidth = bandTagPaint.measureText(tag);
        canvas.drawText(tag, CONTENT_RIGHT - tagWidth, 38, bandTagPaint);

        y = bandHeight + 40;

        // ---------- Title + generated date ----------
        canvas.drawText(title, MARGIN_LEFT, y, titlePaint);

        String generatedDate =
                new SimpleDateFormat("MMMM dd, yyyy  •  hh:mm a", Locale.getDefault())
                        .format(new Date());

        String genText = "Generated: " + generatedDate;
        float genWidth = metaPaint.measureText(genText);
        canvas.drawText(genText, CONTENT_RIGHT - genWidth, y - 4, metaPaint);

        y += 14;
        canvas.drawLine(MARGIN_LEFT, y, CONTENT_RIGHT, y, hairline);

        y += 34;

        // ---------- KPI cards (Pending removed — 4 cards) ----------
        double totalIncome = safeDouble(report.getTotalIncome());
        int totalBookings = report.getTotalBookings();
        int completed = report.getCompletedBookings();
        int cancelled = report.getCancelledBookings();
        int pending = report.getPendingBookings(); // still used in insights, just not shown as a card

        String[] kpiLabels = {"TOTAL REVENUE", "TOTAL BOOKINGS", "COMPLETED", "CANCELLED"};
        String[] kpiValues = {
                "\u20B1" + money.format(totalIncome),
                String.valueOf(totalBookings),
                String.valueOf(completed),
                String.valueOf(cancelled)
        };

        float gap = 10f;
        float cardWidth = (CONTENT_WIDTH - gap * (kpiLabels.length - 1)) / kpiLabels.length;
        float cardHeight = 60f;

        for (int i = 0; i < kpiLabels.length; i++) {
            float left = MARGIN_LEFT + i * (cardWidth + gap);
            float top = y;
            RectF rect = new RectF(left, top, left + cardWidth, top + cardHeight);

            canvas.drawRoundRect(rect, 5, 5, cardFill);
            canvas.drawRoundRect(rect, 5, 5, cardStroke);

            Paint smallLabel = textPaint(7.6f, Typeface.DEFAULT_BOLD, COLOR_TEXT_MUTED);
            canvas.drawText(kpiLabels[i], left + 10, top + 20, smallLabel);

            Paint smallValue = textPaint(14, Typeface.DEFAULT_BOLD, COLOR_PRIMARY_DEEP);
            float vw = smallValue.measureText(kpiValues[i]);
            while (vw > cardWidth - 16 && smallValue.getTextSize() > 9f) {
                smallValue.setTextSize(smallValue.getTextSize() - 0.5f);
                vw = smallValue.measureText(kpiValues[i]);
            }
            canvas.drawText(kpiValues[i], left + 10, top + 42, smallValue);
        }

        y += (int) cardHeight + 36;

        // ---------- Insights (expanded, sentence-style, data-driven) ----------
        canvas.drawText("Insights", MARGIN_LEFT, y, sectionHeaderPaint);
        y += 10;
        canvas.drawLine(MARGIN_LEFT, y, MARGIN_LEFT + 60, y, sectionUnderline);
        y += 22;

        ArrayList<String> insights = buildInsights(
                totalBookings, completed, cancelled, pending, totalIncome, services, pct, money
        );

        Paint bulletPaint = solidPaint(COLOR_PRIMARY);
        for (String line : insights) {
            canvas.drawCircle(MARGIN_LEFT + 3, y - 4, 2.4f, bulletPaint);
            y = drawWrappedText(canvas, line, MARGIN_LEFT + 14, y, bodyPaint,
                    CONTENT_WIDTH - 14, 16);
            y += 6;
        }

        y += 12;
        canvas.drawLine(MARGIN_LEFT, y, CONTENT_RIGHT, y, hairline);
        y += 30;

        // ---------- Top requested services (top 5 only, thin progress bars) ----------
        canvas.drawText("Top Requested Services", MARGIN_LEFT, y, sectionHeaderPaint);
        y += 10;
        canvas.drawLine(MARGIN_LEFT, y, MARGIN_LEFT + 60, y, sectionUnderline);
        y += 28;

        int maxServiceBookings = 1;
        for (ServiceReportModel s : services) {
            maxServiceBookings = Math.max(maxServiceBookings, s.getTotalBookings());
        }

        int rank = 1;
        float barHeight = 4f; // thin, not thick

        for (ServiceReportModel service : services) {

            if (rank > TOP_SERVICES_LIMIT) break;

            Paint rankPaint = textPaint(11, Typeface.DEFAULT_BOLD, COLOR_PRIMARY_DARK);
            String rankLabel = String.valueOf(rank) + ".";
            canvas.drawText(rankLabel, MARGIN_LEFT, y, rankPaint);

            Paint namePaint = textPaint(11.5f, Typeface.DEFAULT, COLOR_TEXT);
            canvas.drawText(service.getServiceName(), MARGIN_LEFT + 22, y, namePaint);

            Paint countPaint = textPaint(10, Typeface.DEFAULT_BOLD, COLOR_TEXT_MUTED);
            String countLabel = service.getTotalBookings() + " bookings";
            float countWidth = countPaint.measureText(countLabel);
            canvas.drawText(countLabel, CONTENT_RIGHT - countWidth, y, countPaint);

            y += 10;

            // full-width thin progress bar, edge to edge with page margins
            RectF track = new RectF(MARGIN_LEFT, y, CONTENT_RIGHT, y + barHeight);
            canvas.drawRoundRect(track, barHeight / 2f, barHeight / 2f, solidPaint(COLOR_TINT_LIGHT));

            float fillRatio = (float) service.getTotalBookings() / (float) maxServiceBookings;
            float fillWidth = Math.max(barHeight, CONTENT_WIDTH * fillRatio);
            RectF fill = new RectF(MARGIN_LEFT, y, MARGIN_LEFT + fillWidth, y + barHeight);
            canvas.drawRoundRect(fill, barHeight / 2f, barHeight / 2f, solidPaint(COLOR_PRIMARY));

            y += barHeight + 20;
            rank++;
        }

        // ---------- Footer ----------
        canvas.drawLine(MARGIN_LEFT, PAGE_HEIGHT - 50, CONTENT_RIGHT, PAGE_HEIGHT - 50, hairline);
        canvas.drawText(displayShopName + " — Generated automatically by the system",
                MARGIN_LEFT, PAGE_HEIGHT - 32, footerPaint);
        String pageLabel = "Page 1";
        float pageLabelWidth = footerPaint.measureText(pageLabel);
        canvas.drawText(pageLabel, CONTENT_RIGHT - pageLabelWidth, PAGE_HEIGHT - 32, footerPaint);

        pdfDocument.finishPage(page);

        try {

            File folder = new File(
                    context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                    "Reports"
            );

            if (!folder.exists()) {
                folder.mkdirs();
            }

            String fileName =
                    title.replace(" ", "_")
                            + "_"
                            + System.currentTimeMillis()
                            + ".pdf";

            File file = new File(folder, fileName);

            FileOutputStream outputStream = new FileOutputStream(file);
            pdfDocument.writeTo(outputStream);
            outputStream.flush();
            outputStream.close();
            pdfDocument.close();

            Uri uri = FileProvider.getUriForFile(
                    context,
                    context.getPackageName() + ".provider",
                    file
            );

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("application/pdf");
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, title);
            shareIntent.putExtra(Intent.EXTRA_TEXT, displayShopName + " Generated Report");
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            Intent chooser = Intent.createChooser(shareIntent, "Open or Share Report");
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            context.startActivity(chooser);

        } catch (ActivityNotFoundException e) {

            Toast.makeText(
                    context,
                    "No application found to open or share the PDF.",
                    Toast.LENGTH_LONG
            ).show();

        } catch (Exception e) {

            e.printStackTrace();

            Toast.makeText(
                    context,
                    "Failed to generate PDF: " + e.getMessage(),
                    Toast.LENGTH_LONG
            ).show();

        }

    }

    // ------------------------------------------------------------------
    // Insight generation (simple, deterministic — no external AI call)
    // Sentence-style, based directly on the report figures.
    // ------------------------------------------------------------------
    private static ArrayList<String> buildInsights(
            int totalBookings,
            int completed,
            int cancelled,
            int pending,
            double totalIncome,
            ArrayList<ServiceReportModel> services,
            DecimalFormat pct,
            DecimalFormat money
    ) {
        ArrayList<String> insights = new ArrayList<>();

        if (totalBookings <= 0) {
            insights.add("No bookings were recorded for this period, so no performance trends can be drawn yet.");
            return insights;
        }

        double completionRate = (completed * 100.0) / totalBookings;
        double cancellationRate = (cancelled * 100.0) / totalBookings;
        double pendingShare = (pending * 100.0) / totalBookings;
        double avgRevenuePerCompleted = completed > 0 ? (totalIncome / completed) : 0;
        double avgRevenuePerBooking = totalIncome / totalBookings;

        // 1. Overall summary sentence
        insights.add("Out of " + totalBookings + " total booking(s) this period, "
                + completed + " were completed, generating \u20B1" + money.format(totalIncome) + " in revenue.");

        // 2. Completion performance
        insights.add("The business closed " + pct.format(completionRate)
                + "% of bookings successfully, which reflects "
                + (completionRate >= 70 ? "strong" : completionRate >= 40 ? "moderate" : "weak")
                + " conversion from booking to completion.");

        // 3. Cancellation behavior
        if (cancellationRate >= 20) {
            insights.add("Cancellations account for " + pct.format(cancellationRate)
                    + "% of all bookings, an elevated figure that may point to scheduling conflicts, "
                    + "no-shows, or unmet customer expectations worth investigating.");
        } else if (cancellationRate > 0) {
            insights.add("Cancellations remain low at " + pct.format(cancellationRate)
                    + "% of total bookings, indicating stable customer commitment.");
        } else {
            insights.add("No cancellations were recorded this period, reflecting reliable booking follow-through.");
        }

        // 4. Pending backlog
        if (pending > 0) {
            insights.add(pending + " booking(s), or " + pct.format(pendingShare)
                    + "% of the total, are still pending and may require timely action to avoid delays or drop-off.");
        } else {
            insights.add("There is no pending backlog — all bookings this period have been resolved.");
        }

        // 5. Revenue efficiency
        if (completed > 0) {
            insights.add("Each completed booking generated an average of \u20B1" + money.format(avgRevenuePerCompleted)
                    + ", while revenue averaged \u20B1" + money.format(avgRevenuePerBooking)
                    + " across all bookings including cancellations and pending ones.");
        }

        // 6. Top service and demand concentration
        if (services != null && !services.isEmpty()) {
            ServiceReportModel top = services.get(0);
            int serviceBookingSum = 0;
            for (ServiceReportModel s : services) {
                serviceBookingSum += s.getTotalBookings();
                if (s.getTotalBookings() > top.getTotalBookings()) top = s;
            }
            double topShare = serviceBookingSum > 0
                    ? (top.getTotalBookings() * 100.0) / serviceBookingSum
                    : 0;
            insights.add("\"" + top.getServiceName() + "\" is the most requested service with "
                    + top.getTotalBookings() + " booking(s), making up "
                    + pct.format(topShare) + "% of service demand this period.");
        }

        return insights;
    }

    // ------------------------------------------------------------------
    // Small drawing helpers
    // ------------------------------------------------------------------
    private static Paint textPaint(float size, Typeface typeface, int color) {
        Paint p = new Paint();
        p.setAntiAlias(true);
        p.setTextSize(size);
        p.setTypeface(typeface);
        p.setColor(color);
        return p;
    }

    private static Paint solidPaint(int color) {
        Paint p = new Paint();
        p.setAntiAlias(true);
        p.setColor(color);
        p.setStyle(Paint.Style.FILL);
        return p;
    }

    private static Paint solidPaintStroke(int color, float strokeWidth) {
        Paint p = new Paint();
        p.setAntiAlias(true);
        p.setColor(color);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(strokeWidth);
        return p;
    }

    private static boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static double safeDouble(Object value) {
        if (value == null) return 0d;
        if (value instanceof Number) return ((Number) value).doubleValue();
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (Exception e) {
            return 0d;
        }
    }

    /** Wraps text within maxWidth, drawing each line and returning the new Y position. */
    private static int drawWrappedText(Canvas canvas, String text, float x, int startY,
                                       Paint paint, float maxWidth, int lineHeight) {
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();
        int y = startY;

        for (String word : words) {
            String candidate = line.length() == 0 ? word : line + " " + word;
            if (paint.measureText(candidate) > maxWidth && line.length() > 0) {
                canvas.drawText(line.toString(), x, y, paint);
                y += lineHeight;
                line = new StringBuilder(word);
            } else {
                line = new StringBuilder(candidate);
            }
        }
        if (line.length() > 0) {
            canvas.drawText(line.toString(), x, y, paint);
            y += lineHeight;
        }
        return y;
    }

}
