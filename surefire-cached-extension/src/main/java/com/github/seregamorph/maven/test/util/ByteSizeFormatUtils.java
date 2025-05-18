package com.github.seregamorph.maven.test.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Utility class for formatting byte sizes into human-readable strings.
 *
 * @author Sergey Chernov
 */
public final class ByteSizeFormatUtils {

    private static final String[] UNITS = {"B", "KB", "MB", "GB"};
    private static final BigDecimal UNIT_SIZE = BigDecimal.valueOf(1024);

    /**
     * Formats a byte size into a human-readable string with the appropriate unit (B, KB, MB, GB).
     * The value is rounded to 2 decimal places.
     *
     * @param bytes the number of bytes to format
     * @return a human-readable string representation of the byte size
     */
    public static String formatByteSize(long bytes) {
        if (bytes < 0) {
            return "-" + formatByteSize(-bytes);
        }
        if (bytes < 1024) {
            return bytes + " " + UNITS[0];
        }

        // Find the appropriate unit by dividing by 1024 repeatedly
        int unitIndex = 0;
        BigDecimal size = BigDecimal.valueOf(bytes);
        while (size.compareTo(UNIT_SIZE) >= 0 && unitIndex < UNITS.length - 1) {
            size = size.divide(UNIT_SIZE, 2, RoundingMode.HALF_UP);
            unitIndex++;
        }

        return size + " " + UNITS[unitIndex];
    }

    private ByteSizeFormatUtils() {
    }
}
