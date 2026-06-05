package compiler.common.report;

import java.util.ArrayList;
import java.util.List;

/** Fixed-width column alignment for experiment report tables. */
public final class ReportTableFormatter {
    private static final int COLUMN_GAP = 2;

    private ReportTableFormatter() {
    }

    public static List<String> format(String[] headers, List<String[]> rows) {
        if (headers == null || headers.length == 0) {
            return List.of();
        }
        int[] widths = new int[headers.length];
        for (int i = 0; i < headers.length; i++) {
            widths[i] = headers[i].length();
        }
        for (String[] row : rows) {
            for (int i = 0; i < headers.length; i++) {
                String cell = i < row.length ? row[i] : "";
                widths[i] = Math.max(widths[i], cell.length());
            }
        }

        List<String> lines = new ArrayList<>(rows.size() + 1);
        lines.add(formatRow(headers, widths));
        for (String[] row : rows) {
            lines.add(formatRow(row, headers.length, widths));
        }
        return List.copyOf(lines);
    }

    private static String formatRow(String[] cells, int[] widths) {
        return formatRow(cells, cells.length, widths);
    }

    private static String formatRow(String[] cells, int columnCount, int[] widths) {
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < columnCount; i++) {
            if (i > 0) {
                line.append(" ".repeat(COLUMN_GAP));
            }
            String cell = i < cells.length && cells[i] != null ? cells[i] : "";
            line.append(padRight(cell, widths[i]));
        }
        return line.toString();
    }

    private static String padRight(String text, int width) {
        if (text.length() >= width) {
            return text;
        }
        return text + " ".repeat(width - text.length());
    }
}
