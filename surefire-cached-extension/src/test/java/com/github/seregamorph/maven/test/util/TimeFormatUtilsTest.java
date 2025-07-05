package com.github.seregamorph.maven.test.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

/**
 * @author Sergey Chernov
 */
class TimeFormatUtilsTest {

    @Test
    void testFormatTimeWhenSecondsAreLessThanOne() {
        BigDecimal seconds = new BigDecimal("0.432");
        String result = TimeFormatUtils.formatTime(seconds);
        assertEquals("0.432s", result);
    }

    @Test
    void testFormatTimeWhenSecondsOnly() {
        BigDecimal seconds = new BigDecimal("45");
        String result = TimeFormatUtils.formatTime(seconds);
        assertEquals("45s", result);
    }

    @Test
    void testFormatTimeWhenMinutesAndSeconds() {
        BigDecimal seconds = new BigDecimal("125");
        String result = TimeFormatUtils.formatTime(seconds);
        assertEquals("2m5s", result);
    }

    @Test
    void testFormatTimeWhenHoursMinutesAndSeconds() {
        BigDecimal seconds = new BigDecimal("3665");
        String result = TimeFormatUtils.formatTime(seconds);
        assertEquals("1h1m5s", result);
    }

    @Test
    void testFormatTimeWhenDaysHoursMinutesAndSeconds() {
        BigDecimal seconds = new BigDecimal("90061");
        String result = TimeFormatUtils.formatTime(seconds);
        assertEquals("1d1h1m1s", result);
    }

    @Test
    void testFormatTimeWhenOnlyDays() {
        BigDecimal seconds = new BigDecimal("172800");
        String result = TimeFormatUtils.formatTime(seconds);
        assertEquals("2d0h0m0s", result);
    }

    @Test
    void testFormatTimeWhenComplexTime() {
        BigDecimal seconds = new BigDecimal(864000 + 36000 + 600 + 10);
        String result = TimeFormatUtils.formatTime(seconds);
        assertEquals("10d10h10m10s", result);
    }
}
