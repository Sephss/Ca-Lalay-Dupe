package com.example.ca_lalay.utils;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Builds a landscape A4, multi-page bookings table PDF (no external libraries).
 */
public class BookingsPdfExporter {

    private static final int PAGE_W = 842, PAGE_H = 595, MARGIN = 32, ROW_H = 20;
    private static final int RED = Color.parseColor("#EA5050");

    private static final String[] COL_H = {"#", "Date", "Time", "Customer", "Service", "Vehicle", "Status", "Payment", "Total"};
    private static final float[] COL_W = {28, 70, 58, 130, 150, 100, 76, 80, 86}; // = 778

    private static class Ctx {
        final PdfDocument doc = new PdfDocument();
        PdfDocument.Page page;
        Canvas c;
        float y;
        int n;
    }

    public static byte[] build(BookingsReportData d) throws IOException {
        Ctx x = new Ctx();
        newPage(x, d);

        int i = 1;
        for (BookingsReportData.Row r : d.rows) {
            if (x.y + ROW_H > PAGE_H - 36) newPage(x, d);
            drawRow(x, i++, r);
        }

        if (x.y + 54 > PAGE_H - 36) newPage(x, d);
        drawTotals(x, d);
        finish(x);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        x.doc.writeTo(bos);
        x.doc.close();
        return bos.toByteArray();
    }

    // ------------------------------------------------------------ pages

    private static void newPage(Ctx x, BookingsReportData d) {
        if (x.page != null) finish(x);
        x.n++;
        x.page = x.doc.startPage(new PdfDocument.PageInfo.Builder(PAGE_W, PAGE_H, x.n).create());
        x.c = x.page.getCanvas();
        x.y = MARGIN;
        if (x.n == 1) drawHeaderBlock(x, d);
        drawTableHeader(x);
    }

    private static void finish(Ctx x) {
        Paint p = paint(8, Color.GRAY, false);
        x.c.drawText("CA-Lalay Bookings Report", MARGIN, PAGE_H - 14, p);
        String pg = "Page " + x.n;
        x.c.drawText(pg, PAGE_W - MARGIN - p.measureText(pg), PAGE_H - 14, p);
        x.doc.finishPage(x.page);
        x.page = null;
    }

    private static void drawHeaderBlock(Ctx x, BookingsReportData d) {
        Canvas c = x.c;

        x.y += 18;
        c.drawText(d.title, MARGIN, x.y, paint(20, RED, true));

        x.y += 18;
        c.drawText(nz(d.shopName), MARGIN, x.y, paint(12, Color.BLACK, true));

        x.y += 14;
        String contactLine = (nz(d.contact) + "   " + nz(d.location)).trim();
        c.drawText(contactLine, MARGIN, x.y, paint(10, Color.DKGRAY, false));

        x.y += 16;
        c.drawText("Period: " + d.periodLabel + " (" + d.rangeLabel + ")", MARGIN, x.y, paint(10, Color.BLACK, false));

        x.y += 14;
        c.drawText("Service: " + d.serviceLabel + "     Status: " + d.statusLabel,
                MARGIN, x.y, paint(10, Color.BLACK, false));

        x.y += 14;
        String gen = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault()).format(new Date());
        c.drawText("Generated: " + gen, MARGIN, x.y, paint(9, Color.GRAY, false));

        // summary strip
        x.y += 12;
        Paint bg = new Paint();
        bg.setColor(Color.parseColor("#FDECEC"));
        c.drawRect(MARGIN, x.y, PAGE_W - MARGIN, x.y + 30, bg);

        String summary = "Total: " + d.rows.size()
                + "   Completed: " + d.count("done")
                + "   Pending: " + d.count("pending")
                + "   Confirmed: " + d.count("confirmed")
                + "   In Progress: " + d.count("inProgress")
                + "   Cancelled: " + d.count("cancelled")
                + "   Rejected: " + d.count("rejected");
        c.drawText(summary, MARGIN + 8, x.y + 13, paint(10, Color.BLACK, true));
        c.drawText("Revenue (completed only): " + money(d.completedRevenue()),
                MARGIN + 8, x.y + 25, paint(10, RED, true));

        x.y += 42;
    }

    private static void drawTableHeader(Ctx x) {
        Paint bg = new Paint();
        bg.setColor(RED);
        x.c.drawRect(MARGIN, x.y, PAGE_W - MARGIN, x.y + 22, bg);

        Paint p = paint(10, Color.WHITE, true);
        float cx = MARGIN;
        for (int k = 0; k < COL_H.length; k++) {
            float tx = (k == 8) ? cx + COL_W[k] - 4 - p.measureText(COL_H[k]) : cx + 4;
            x.c.drawText(COL_H[k], tx, x.y + 15, p);
            cx += COL_W[k];
        }
        x.y += 22;
    }

    private static void drawRow(Ctx x, int index, BookingsReportData.Row r) {
        if (index % 2 == 0) {
            Paint bg = new Paint();
            bg.setColor(Color.parseColor("#FAF0F0"));
            x.c.drawRect(MARGIN, x.y, PAGE_W - MARGIN, x.y + ROW_H, bg);
        }

        String[] cells = {
                String.valueOf(index), r.date, r.time, r.customer, r.serviceName,
                r.vehicle, r.status, r.paymentMethod, money(r.total)
        };

        Paint p = paint(9, Color.BLACK, false);
        float cx = MARGIN;
        for (int k = 0; k < cells.length; k++) {
            String t = fit(cells[k], COL_W[k] - 8, p);
            float tx = (k == 8) ? cx + COL_W[k] - 4 - p.measureText(t) : cx + 4;
            x.c.drawText(t, tx, x.y + 14, p);
            cx += COL_W[k];
        }
        x.y += ROW_H;
    }

    private static void drawTotals(Ctx x, BookingsReportData d) {
        Paint line = new Paint();
        line.setColor(RED);
        line.setStrokeWidth(1.5f);
        x.c.drawLine(MARGIN, x.y + 4, PAGE_W - MARGIN, x.y + 4, line);

        Paint b = paint(10, Color.BLACK, true);
        float right = PAGE_W - MARGIN - 4;

        String a = "Total value of all listed bookings: " + money(d.totalValue());
        x.c.drawText(a, right - b.measureText(a), x.y + 22, b);

        Paint red = paint(11, RED, true);
        String r = "Revenue (completed only): " + money(d.completedRevenue());
        x.c.drawText(r, right - red.measureText(r), x.y + 40, red);

        x.y += 44;
    }

    // ------------------------------------------------------------ helpers

    private static Paint paint(float size, int color, boolean bold) {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setTextSize(size);
        p.setColor(color);
        p.setTypeface(bold ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
        return p;
    }

    private static String fit(String s, float max, Paint p) {
        if (s == null) return "";
        if (p.measureText(s) <= max) return s;
        String ell = "\u2026";
        int n = s.length();
        while (n > 0 && p.measureText(s.substring(0, n) + ell) > max) n--;
        return s.substring(0, n) + ell;
    }

    private static String money(double v) {
        return String.format(Locale.US, "\u20B1%,.2f", v);
    }

    private static String nz(String s) {
        return s == null ? "" : s;
    }
}