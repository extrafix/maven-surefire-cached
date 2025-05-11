package com.github.seregamorph.maven.test.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link ByteSizeFormatUtils}.
 */
public class ByteSizeFormatUtilsTest {

    @Test
    public void testFormatByteSizeZero() {
        assertEquals("0 B", ByteSizeFormatUtils.formatByteSize(0));
    }

    @Test
    public void testFormatByteSizeNegative() {
        assertEquals("-1.00 KB", ByteSizeFormatUtils.formatByteSize(-1024));
        assertEquals("-1 B", ByteSizeFormatUtils.formatByteSize(-1));
    }

    @Test
    public void testFormatByteSizeBytes() {
        assertEquals("1 B", ByteSizeFormatUtils.formatByteSize(1));
        assertEquals("999 B", ByteSizeFormatUtils.formatByteSize(999));
        assertEquals("1023 B", ByteSizeFormatUtils.formatByteSize(1023));
    }

    @Test
    public void testFormatByteSizeKilobytes() {
        assertEquals("1.00 KB", ByteSizeFormatUtils.formatByteSize(1024));
        assertEquals("1.50 KB", ByteSizeFormatUtils.formatByteSize(1536));
        assertEquals("999.99 KB", ByteSizeFormatUtils.formatByteSize(1024 * 1000 - 10));
    }

    @Test
    public void testFormatByteSizeMegabytes() {
        assertEquals("1.00 MB", ByteSizeFormatUtils.formatByteSize(1024 * 1024));
        assertEquals("1.50 MB", ByteSizeFormatUtils.formatByteSize(1024 * 1024 + 1024 * 512));
        assertEquals("10.00 MB", ByteSizeFormatUtils.formatByteSize(1024 * 1024 * 10));
    }

    @Test
    public void testFormatByteSizeGigabytes() {
        assertEquals("1.00 GB", ByteSizeFormatUtils.formatByteSize(1024L * 1024 * 1024));
        assertEquals("2.50 GB", ByteSizeFormatUtils.formatByteSize(1024L * 1024 * 1024 * 2 + 1024L * 1024 * 512));
    }
}
