package com.example.ca_lalay.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import com.example.ca_lalay.model.BookingReportModel;
import com.example.ca_lalay.model.ServiceReportModel;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Builds .xlsx (no external library, real formulas + styling) and .csv reports,
 * and saves them to Downloads/CA-Lalay Reports.
 */
public class ReportFileExporter {

    public static final String MIME_XLSX =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    public static final String MIME_CSV = "text/csv";
    public static final String FOLDER = "CA-Lalay Reports";

    // ---- style ids (must match cellXfs order in styles.xml) ----
    private static final int S_DEFAULT = 0, S_HEADER = 1, S_TITLE = 2, S_CURRENCY = 3,
            S_INT = 4, S_BOLD = 5, S_PERCENT = 6, S_BOLD_CURRENCY = 7, S_BOLD_INT = 8;

    // =====================================================================
    //  PUBLIC API
    // =====================================================================

    public static String buildFileName(String title, String extension) {
        String clean = title.replaceAll("[^A-Za-z0-9]+", "_").replaceAll("^_|_$", "");
        String stamp = new SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(new Date());
        return "CALalay_" + clean + "_" + stamp + "." + extension;
    }

    public static byte[] buildXlsx(ReportExportData d) throws IOException {
        List<Sheet> sheets = new ArrayList<>();
        sheets.add(buildSummarySheet(d));
        sheets.add(buildServicesSheet(d));
        if (d.breakdown != null && !d.breakdown.isEmpty()) {
            sheets.add(buildBreakdownSheet(d));
        }
        return writeWorkbook(sheets);
    }

    private static byte[] writeWorkbook(List<Sheet> sheets) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(bos)) {
            put(zip, "[Content_Types].xml", contentTypes(sheets.size()));
            put(zip, "_rels/.rels",
                    "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                            + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
                            + "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"xl/workbook.xml\"/>"
                            + "</Relationships>");
            put(zip, "xl/workbook.xml", workbook(sheets));
            put(zip, "xl/_rels/workbook.xml.rels", workbookRels(sheets.size()));
            put(zip, "xl/styles.xml", styles());
            for (int i = 0; i < sheets.size(); i++) {
                put(zip, "xl/worksheets/sheet" + (i + 1) + ".xml", sheetXml(sheets.get(i)));
            }
        }
        return bos.toByteArray();
    }

    public static byte[] buildCsv(ReportExportData d) {
        BookingReportModel r = d.report;
        int total = r.getTotalBookings();
        int completed = r.getCompletedBookings();
        int pending = r.getPendingBookings();
        int cancelled = r.getCancelledBookings();
        int inProgress = Math.max(0, total - completed - pending - cancelled);
        double income = r.getTotalIncome();

        StringBuilder sb = new StringBuilder("\uFEFF");
        line(sb, d.title);
        line(sb, "Shop", d.shopName);
        line(sb, "Contact", d.contact);
        line(sb, "Location", d.location);
        line(sb, "Period", d.periodLabel);
        line(sb, "Generated", new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault()).format(new Date()));
        sb.append("\r\n");

        line(sb, "SUMMARY");
        line(sb, "Metric", "Value");
        line(sb, "Total Bookings", String.valueOf(total));
        line(sb, "Completed", String.valueOf(completed));
        line(sb, "Pending", String.valueOf(pending));
        line(sb, "In Progress", String.valueOf(inProgress));
        line(sb, "Cancelled", String.valueOf(cancelled));
        line(sb, "Total Revenue (PHP)", String.format(Locale.US, "%.2f", income));
        sb.append("\r\n");

        line(sb, "TOP SERVICES");
        line(sb, "Rank", "Service", "Completed Bookings");
        int rank = 1;
        for (ServiceReportModel s : d.services) {
            line(sb, String.valueOf(rank++), s.getServiceName(), String.valueOf(s.getTotalBookings()));
        }

        if (d.breakdown != null && !d.breakdown.isEmpty()) {
            sb.append("\r\n");
            line(sb, d.breakdownTitle.toUpperCase(Locale.ROOT));
            line(sb, "Period", "Total Bookings", "Completed", "Pending", "Cancelled", "Revenue (PHP)");
            for (BookingReportModel b : d.breakdown) {
                line(sb, prettyPeriod(b.getPeriod()),
                        String.valueOf(b.getTotalBookings()),
                        String.valueOf(b.getCompletedBookings()),
                        String.valueOf(b.getPendingBookings()),
                        String.valueOf(b.getCancelledBookings()),
                        String.format(Locale.US, "%.2f", (double) b.getTotalIncome()));
            }
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    /** Saves to Downloads/CA-Lalay Reports (Android 10+ / API 29+, no permission needed). */
    public static Uri saveToDownloads(Context ctx, String fileName, String mime, byte[] data)
            throws IOException {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            throw new IOException("Saving reports requires Android 10 or higher.");
        }
        ContentResolver resolver = ctx.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
        values.put(MediaStore.MediaColumns.MIME_TYPE, mime);
        values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/" + FOLDER);

        Uri uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
        if (uri == null) throw new IOException("Could not create file.");

        try (OutputStream out = resolver.openOutputStream(uri)) {
            if (out == null) throw new IOException("Could not open file.");
            out.write(data);
        }
        return uri;
    }

    // =====================================================================
    //  SHEETS
    // =====================================================================

    private static Sheet buildSummarySheet(ReportExportData d) {
        BookingReportModel r = d.report;
        int total = r.getTotalBookings();
        int completed = r.getCompletedBookings();
        int pending = r.getPendingBookings();
        int cancelled = r.getCancelledBookings();
        int inProgress = Math.max(0, total - completed - pending - cancelled);
        double income = r.getTotalIncome();

        Sheet s = new Sheet("Summary", 34, 24);
        s.row(c(d.title, S_TITLE));
        s.row(c("Shop", S_BOLD), c(d.shopName, S_DEFAULT));
        s.row(c("Contact", S_BOLD), c(d.contact, S_DEFAULT));
        s.row(c("Location", S_BOLD), c(d.location, S_DEFAULT));
        s.row(c("Period", S_BOLD), c(d.periodLabel, S_DEFAULT));
        s.row(c("Generated", S_BOLD), c(new SimpleDateFormat("MMM dd, yyyy hh:mm a",
                Locale.getDefault()).format(new Date()), S_DEFAULT));
        s.row();
        s.row(c("Metric", S_HEADER), c("Value", S_HEADER));
        s.row(c("Total Bookings", S_DEFAULT), c(total, S_INT));
        s.row(c("Completed", S_DEFAULT), c(completed, S_INT));
        s.row(c("Pending", S_DEFAULT), c(pending, S_INT));
        s.row(c("In Progress", S_DEFAULT), c(inProgress, S_INT));
        s.row(c("Cancelled", S_DEFAULT), c(cancelled, S_INT));
        s.row(c("Total Revenue", S_BOLD), c(income, S_BOLD_CURRENCY));
        s.row();
        s.row(c("Completion Rate", S_DEFAULT), c(total > 0 ? (double) completed / total : 0d, S_PERCENT));
        s.row(c("Cancellation Rate", S_DEFAULT), c(total > 0 ? (double) cancelled / total : 0d, S_PERCENT));
        s.row(c("Avg. Revenue per Completed Booking", S_DEFAULT),
                c(completed > 0 ? income / completed : 0d, S_CURRENCY));
        return s;
    }

    private static Sheet buildServicesSheet(ReportExportData d) {
        Sheet s = new Sheet("Top Services", 8, 36, 20, 14);
        s.row(c("Rank", S_HEADER), c("Service", S_HEADER),
                c("Completed Bookings", S_HEADER), c("Share", S_HEADER));

        if (d.services == null || d.services.isEmpty()) {
            s.row(c("-", S_DEFAULT), c("No completed services for this period", S_DEFAULT));
            return s;
        }

        int grand = 0;
        for (ServiceReportModel sv : d.services) grand += sv.getTotalBookings();

        int first = s.rows.size() + 1;
        int rank = 1;
        for (ServiceReportModel sv : d.services) {
            s.row(c(rank++, S_INT),
                    c(sv.getServiceName(), S_DEFAULT),
                    c(sv.getTotalBookings(), S_INT),
                    c(grand > 0 ? (double) sv.getTotalBookings() / grand : 0d, S_PERCENT));
        }
        int last = s.rows.size();
        s.row(c("", S_BOLD), c("TOTAL", S_BOLD), c("=SUM(C" + first + ":C" + last + ")", S_BOLD_INT));
        return s;
    }

    private static Sheet buildBreakdownSheet(ReportExportData d) {
        Sheet s = new Sheet(d.breakdownTitle, 16, 16, 14, 14, 14, 18);
        s.row(c("Period", S_HEADER), c("Total Bookings", S_HEADER), c("Completed", S_HEADER),
                c("Pending", S_HEADER), c("Cancelled", S_HEADER), c("Revenue", S_HEADER));
        int first = s.rows.size() + 1;
        for (BookingReportModel b : d.breakdown) {
            s.row(c(prettyPeriod(b.getPeriod()), S_DEFAULT),
                    c(b.getTotalBookings(), S_INT),
                    c(b.getCompletedBookings(), S_INT),
                    c(b.getPendingBookings(), S_INT),
                    c(b.getCancelledBookings(), S_INT),
                    c((double) b.getTotalIncome(), S_CURRENCY));
        }
        int last = s.rows.size();
        s.row(c("TOTAL", S_BOLD),
                c("=SUM(B" + first + ":B" + last + ")", S_BOLD_INT),
                c("=SUM(C" + first + ":C" + last + ")", S_BOLD_INT),
                c("=SUM(D" + first + ":D" + last + ")", S_BOLD_INT),
                c("=SUM(E" + first + ":E" + last + ")", S_BOLD_INT),
                c("=SUM(F" + first + ":F" + last + ")", S_BOLD_CURRENCY));
        return s;
    }

    // =====================================================================
    //  DETAILED BOOKINGS REPORT  (one row per booking)
    // =====================================================================

    public static byte[] buildBookingsXlsx(BookingsReportData d) throws IOException {
        List<Sheet> sheets = new ArrayList<>();
        sheets.add(bookingsSummarySheet(d));
        sheets.add(bookingsListSheet(d));
        sheets.add(bookingsByServiceSheet(d));
        return writeWorkbook(sheets);
    }

    private static Sheet bookingsSummarySheet(BookingsReportData d) {
        double revenue = d.completedRevenue();
        int completed = d.count("done");

        Sheet s = new Sheet("Summary", 38, 34);
        s.row(c(d.title, S_TITLE));
        s.row(c("Shop", S_BOLD), c(d.shopName, S_DEFAULT));
        s.row(c("Contact", S_BOLD), c(d.contact, S_DEFAULT));
        s.row(c("Location", S_BOLD), c(d.location, S_DEFAULT));
        s.row(c("Period", S_BOLD), c(d.periodLabel, S_DEFAULT));
        s.row(c("Date Range", S_BOLD), c(d.rangeLabel, S_DEFAULT));
        s.row(c("Service", S_BOLD), c(d.serviceLabel, S_DEFAULT));
        s.row(c("Status Filter", S_BOLD), c(d.statusLabel, S_DEFAULT));
        s.row(c("Generated", S_BOLD), c(new SimpleDateFormat("MMM dd, yyyy hh:mm a",
                Locale.getDefault()).format(new Date()), S_DEFAULT));
        s.row();
        s.row(c("Metric", S_HEADER), c("Value", S_HEADER));
        s.row(c("Total Bookings", S_BOLD), c(d.rows.size(), S_BOLD_INT));
        s.row(c("Completed", S_DEFAULT), c(completed, S_INT));
        s.row(c("Pending", S_DEFAULT), c(d.count("pending"), S_INT));
        s.row(c("Confirmed", S_DEFAULT), c(d.count("confirmed"), S_INT));
        s.row(c("In Progress", S_DEFAULT), c(d.count("inProgress"), S_INT));
        s.row(c("Cancelled", S_DEFAULT), c(d.count("cancelled"), S_INT));
        s.row(c("Rejected", S_DEFAULT), c(d.count("rejected"), S_INT));
        s.row();
        s.row(c("Revenue (completed bookings only)", S_BOLD), c(revenue, S_BOLD_CURRENCY));
        s.row(c("Total value of all listed bookings", S_DEFAULT), c(d.totalValue(), S_CURRENCY));
        s.row(c("Avg. revenue per completed booking", S_DEFAULT),
                c(completed > 0 ? revenue / completed : 0d, S_CURRENCY));
        return s;
    }

    private static Sheet bookingsListSheet(BookingsReportData d) {
        Sheet s = new Sheet("Bookings", 5, 22, 12, 10, 22, 16, 24, 18, 12, 14, 16, 10, 16, 28, 14, 14, 14);
        s.freezeHeader = true;
        s.row(c("#", S_HEADER), c("Booking ID", S_HEADER), c("Date", S_HEADER), c("Time", S_HEADER),
                c("Customer", S_HEADER), c("Contact", S_HEADER), c("Service", S_HEADER),
                c("Vehicle", S_HEADER), c("Vehicle Size", S_HEADER), c("Status", S_HEADER),
                c("Payment Method", S_HEADER), c("Paid?", S_HEADER), c("Billing No.", S_HEADER),
                c("Add-ons", S_HEADER), c("Base Price", S_HEADER), c("Add-ons Total", S_HEADER),
                c("Total", S_HEADER));

        int i = 1;
        for (BookingsReportData.Row r : d.rows) {
            s.row(c(i++, S_INT), c(r.bookingId, S_DEFAULT), c(r.date, S_DEFAULT), c(r.time, S_DEFAULT),
                    c(r.customer, S_DEFAULT), c(r.contact, S_DEFAULT), c(r.serviceName, S_DEFAULT),
                    c(r.vehicle, S_DEFAULT), c(r.vehicleSize, S_DEFAULT), c(r.status, S_DEFAULT),
                    c(r.paymentMethod, S_DEFAULT), c(r.paid, S_DEFAULT), c(r.billingNumber, S_DEFAULT),
                    c(r.addOnsDescription, S_DEFAULT), c(r.basePrice, S_CURRENCY),
                    c(r.addOnsTotal, S_CURRENCY), c(r.total, S_CURRENCY));
        }
        int last = s.rows.size();
        s.filterRef = "A1:Q" + last;

        s.row(); // blank row keeps the total outside the filter range

        // SUBTOTAL(109) respects the filter dropdowns: totals update when the user filters in Excel
        Cell[] tot = new Cell[17];
        for (int k = 0; k < 17; k++) tot[k] = c(null, S_DEFAULT);
        tot[13] = c("TOTAL (visible rows)", S_BOLD);
        tot[14] = c("=SUBTOTAL(109,O2:O" + last + ")", S_BOLD_CURRENCY);
        tot[15] = c("=SUBTOTAL(109,P2:P" + last + ")", S_BOLD_CURRENCY);
        tot[16] = c("=SUBTOTAL(109,Q2:Q" + last + ")", S_BOLD_CURRENCY);
        s.row(tot);
        return s;
    }

    private static Sheet bookingsByServiceSheet(BookingsReportData d) {
        // [bookings, completed, cancelled+rejected, revenue(done), totalValue]
        LinkedHashMap<String, double[]> agg = new LinkedHashMap<>();
        for (BookingsReportData.Row r : d.rows) {
            String key = (r.serviceName == null || r.serviceName.isEmpty()) ? "(Unknown)" : r.serviceName;
            double[] a = agg.get(key);
            if (a == null) {
                a = new double[5];
                agg.put(key, a);
            }
            a[0]++;
            if ("done".equalsIgnoreCase(r.statusRaw)) {
                a[1]++;
                a[3] += r.total;
            }
            if ("cancelled".equalsIgnoreCase(r.statusRaw) || "rejected".equalsIgnoreCase(r.statusRaw)) a[2]++;
            a[4] += r.total;
        }

        List<Map.Entry<String, double[]>> list = new ArrayList<>(agg.entrySet());
        Collections.sort(list, (x, y) -> Double.compare(y.getValue()[0], x.getValue()[0]));

        Sheet s = new Sheet("By Service", 34, 12, 12, 18, 20, 20);
        s.row(c("Service", S_HEADER), c("Bookings", S_HEADER), c("Completed", S_HEADER),
                c("Cancelled/Rejected", S_HEADER), c("Revenue (completed)", S_HEADER),
                c("Total Value", S_HEADER));
        int first = s.rows.size() + 1;
        for (Map.Entry<String, double[]> e : list) {
            double[] a = e.getValue();
            s.row(c(e.getKey(), S_DEFAULT), c((int) a[0], S_INT), c((int) a[1], S_INT),
                    c((int) a[2], S_INT), c(a[3], S_CURRENCY), c(a[4], S_CURRENCY));
        }
        int last = s.rows.size();
        if (last >= first) {
            s.row(c("TOTAL", S_BOLD),
                    c("=SUM(B" + first + ":B" + last + ")", S_BOLD_INT),
                    c("=SUM(C" + first + ":C" + last + ")", S_BOLD_INT),
                    c("=SUM(D" + first + ":D" + last + ")", S_BOLD_INT),
                    c("=SUM(E" + first + ":E" + last + ")", S_BOLD_CURRENCY),
                    c("=SUM(F" + first + ":F" + last + ")", S_BOLD_CURRENCY));
        }
        return s;
    }

    public static byte[] buildBookingsCsv(BookingsReportData d) {
        StringBuilder sb = new StringBuilder("\uFEFF");
        line(sb, d.title);
        line(sb, "Shop", d.shopName);
        line(sb, "Contact", d.contact);
        line(sb, "Location", d.location);
        line(sb, "Period", d.periodLabel);
        line(sb, "Date Range", d.rangeLabel);
        line(sb, "Service", d.serviceLabel);
        line(sb, "Status Filter", d.statusLabel);
        line(sb, "Total Bookings", String.valueOf(d.rows.size()));
        line(sb, "Revenue (completed only, PHP)", String.format(Locale.US, "%.2f", d.completedRevenue()));
        sb.append("\r\n");

        line(sb, "#", "Booking ID", "Date", "Time", "Customer", "Contact", "Service", "Vehicle",
                "Vehicle Size", "Status", "Payment Method", "Paid?", "Billing No.", "Add-ons",
                "Base Price", "Add-ons Total", "Total");
        int i = 1;
        for (BookingsReportData.Row r : d.rows) {
            line(sb, String.valueOf(i++), r.bookingId, r.date, r.time, r.customer, r.contact,
                    r.serviceName, r.vehicle, r.vehicleSize, r.status, r.paymentMethod, r.paid,
                    r.billingNumber, r.addOnsDescription,
                    String.format(Locale.US, "%.2f", r.basePrice),
                    String.format(Locale.US, "%.2f", r.addOnsTotal),
                    String.format(Locale.US, "%.2f", r.total));
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static String prettyPeriod(String p) {
        if (p == null) return "";
        try {
            if (p.matches("\\d{4}-\\d{2}")) {
                YearMonth ym = YearMonth.parse(p);
                return ym.getMonth().getDisplayName(TextStyle.SHORT, Locale.getDefault()) + " " + ym.getYear();
            }
        } catch (Exception ignored) {
        }
        return p;
    }

    // =====================================================================
    //  XLSX INTERNALS
    // =====================================================================

    private static class Cell {
        final Object value;
        final int style;

        Cell(Object v, int s) {
            value = v;
            style = s;
        }
    }

    private static Cell c(Object v, int style) {
        return new Cell(v, style);
    }

    private static class Sheet {
        final String name;
        final double[] widths;
        final List<List<Cell>> rows = new ArrayList<>();
        boolean freezeHeader = false;   // freeze first row
        String filterRef = null;        // e.g. "A1:Q40" -> adds Excel filter dropdowns

        Sheet(String name, double... widths) {
            this.name = name;
            this.widths = widths;
        }

        void row(Cell... cells) {
            List<Cell> r = new ArrayList<>();
            for (Cell cell : cells) r.add(cell);
            rows.add(r);
        }
    }

    private static void put(ZipOutputStream zip, String name, String content) throws IOException {
        zip.putNextEntry(new ZipEntry(name));
        zip.write(content.getBytes(StandardCharsets.UTF_8));
        zip.closeEntry();
    }

    private static String sheetXml(Sheet sheet) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
        sb.append("<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">");
        if (sheet.freezeHeader) {
            sb.append("<sheetViews><sheetView workbookViewId=\"0\"><pane ySplit=\"1\" topLeftCell=\"A2\" activePane=\"bottomLeft\" state=\"frozen\"/></sheetView></sheetViews>");
        }
        sb.append("<cols>");
        for (int i = 0; i < sheet.widths.length; i++) {
            sb.append("<col min=\"").append(i + 1).append("\" max=\"").append(i + 1)
                    .append("\" width=\"").append(sheet.widths[i]).append("\" customWidth=\"1\"/>");
        }
        sb.append("</cols><sheetData>");

        for (int r = 0; r < sheet.rows.size(); r++) {
            List<Cell> row = sheet.rows.get(r);
            sb.append("<row r=\"").append(r + 1).append("\">");
            for (int col = 0; col < row.size(); col++) {
                Cell cell = row.get(col);
                if (cell.value == null) continue;
                String ref = colLetter(col) + (r + 1);
                if (cell.value instanceof Number) {
                    sb.append("<c r=\"").append(ref).append("\" s=\"").append(cell.style).append("\"><v>")
                            .append(BigDecimal.valueOf(((Number) cell.value).doubleValue()).toPlainString())
                            .append("</v></c>");
                } else {
                    String text = String.valueOf(cell.value);
                    if (text.startsWith("=")) {
                        sb.append("<c r=\"").append(ref).append("\" s=\"").append(cell.style).append("\"><f>")
                                .append(esc(text.substring(1))).append("</f></c>");
                    } else {
                        sb.append("<c r=\"").append(ref).append("\" s=\"").append(cell.style)
                                .append("\" t=\"inlineStr\"><is><t xml:space=\"preserve\">")
                                .append(esc(text)).append("</t></is></c>");
                    }
                }
            }
            sb.append("</row>");
        }
        sb.append("</sheetData>");
        if (sheet.filterRef != null) {
            sb.append("<autoFilter ref=\"").append(sheet.filterRef).append("\"/>");
        }
        sb.append("</worksheet>");
        return sb.toString();
    }

    private static String colLetter(int index) {
        StringBuilder sb = new StringBuilder();
        index++;
        while (index > 0) {
            int rem = (index - 1) % 26;
            sb.insert(0, (char) ('A' + rem));
            index = (index - 1) / 26;
        }
        return sb.toString();
    }

    private static String esc(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&apos;");
    }

    private static String contentTypes(int sheetCount) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
        sb.append("<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">");
        sb.append("<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>");
        sb.append("<Default Extension=\"xml\" ContentType=\"application/xml\"/>");
        sb.append("<Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/>");
        sb.append("<Override PartName=\"/xl/styles.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml\"/>");
        for (int i = 1; i <= sheetCount; i++) {
            sb.append("<Override PartName=\"/xl/worksheets/sheet").append(i)
                    .append(".xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>");
        }
        sb.append("</Types>");
        return sb.toString();
    }

    private static String workbook(List<Sheet> sheets) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
        sb.append("<workbook xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" ")
                .append("xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\"><sheets>");
        for (int i = 0; i < sheets.size(); i++) {
            sb.append("<sheet name=\"").append(esc(sheets.get(i).name)).append("\" sheetId=\"").append(i + 1)
                    .append("\" r:id=\"rId").append(i + 1).append("\"/>");
        }
        sb.append("</sheets></workbook>");
        return sb.toString();
    }

    private static String workbookRels(int sheetCount) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
        sb.append("<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">");
        for (int i = 1; i <= sheetCount; i++) {
            sb.append("<Relationship Id=\"rId").append(i)
                    .append("\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" Target=\"worksheets/sheet")
                    .append(i).append(".xml\"/>");
        }
        sb.append("<Relationship Id=\"rId").append(sheetCount + 1)
                .append("\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles\" Target=\"styles.xml\"/>");
        sb.append("</Relationships>");
        return sb.toString();
    }

    private static String styles() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<styleSheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">"
                + "<numFmts count=\"2\">"
                + "<numFmt numFmtId=\"164\" formatCode=\"&quot;\u20B1&quot;#,##0.00\"/>"
                + "<numFmt numFmtId=\"165\" formatCode=\"0.0%\"/>"
                + "</numFmts>"
                + "<fonts count=\"4\">"
                + "<font><sz val=\"11\"/><name val=\"Calibri\"/></font>"
                + "<font><b/><sz val=\"11\"/><color rgb=\"FFFFFFFF\"/><name val=\"Calibri\"/></font>"
                + "<font><b/><sz val=\"16\"/><color rgb=\"FFEA5050\"/><name val=\"Calibri\"/></font>"
                + "<font><b/><sz val=\"11\"/><name val=\"Calibri\"/></font>"
                + "</fonts>"
                + "<fills count=\"3\">"
                + "<fill><patternFill patternType=\"none\"/></fill>"
                + "<fill><patternFill patternType=\"gray125\"/></fill>"
                + "<fill><patternFill patternType=\"solid\"><fgColor rgb=\"FFEA5050\"/><bgColor indexed=\"64\"/></patternFill></fill>"
                + "</fills>"
                + "<borders count=\"1\"><border><left/><right/><top/><bottom/><diagonal/></border></borders>"
                + "<cellStyleXfs count=\"1\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\"/></cellStyleXfs>"
                + "<cellXfs count=\"9\">"
                + "<xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\" xfId=\"0\"/>"
                + "<xf numFmtId=\"0\" fontId=\"1\" fillId=\"2\" borderId=\"0\" xfId=\"0\" applyFont=\"1\" applyFill=\"1\" applyAlignment=\"1\"><alignment horizontal=\"center\"/></xf>"
                + "<xf numFmtId=\"0\" fontId=\"2\" fillId=\"0\" borderId=\"0\" xfId=\"0\" applyFont=\"1\"/>"
                + "<xf numFmtId=\"164\" fontId=\"0\" fillId=\"0\" borderId=\"0\" xfId=\"0\" applyNumberFormat=\"1\"/>"
                + "<xf numFmtId=\"3\" fontId=\"0\" fillId=\"0\" borderId=\"0\" xfId=\"0\" applyNumberFormat=\"1\"/>"
                + "<xf numFmtId=\"0\" fontId=\"3\" fillId=\"0\" borderId=\"0\" xfId=\"0\" applyFont=\"1\"/>"
                + "<xf numFmtId=\"165\" fontId=\"0\" fillId=\"0\" borderId=\"0\" xfId=\"0\" applyNumberFormat=\"1\"/>"
                + "<xf numFmtId=\"164\" fontId=\"3\" fillId=\"0\" borderId=\"0\" xfId=\"0\" applyNumberFormat=\"1\" applyFont=\"1\"/>"
                + "<xf numFmtId=\"3\" fontId=\"3\" fillId=\"0\" borderId=\"0\" xfId=\"0\" applyNumberFormat=\"1\" applyFont=\"1\"/>"
                + "</cellXfs>"
                + "<cellStyles count=\"1\"><cellStyle name=\"Normal\" xfId=\"0\" builtinId=\"0\"/></cellStyles>"
                + "</styleSheet>";
    }

    // =====================================================================
    //  CSV HELPERS
    // =====================================================================

    private static void line(StringBuilder sb, String... cols) {
        for (int i = 0; i < cols.length; i++) {
            if (i > 0) sb.append(',');
            sb.append(csv(cols[i]));
        }
        sb.append("\r\n");
    }

    private static String csv(String v) {
        if (v == null) return "";
        if (v.contains(",") || v.contains("\"") || v.contains("\n")) {
            return "\"" + v.replace("\"", "\"\"") + "\"";
        }
        return v;
    }
}