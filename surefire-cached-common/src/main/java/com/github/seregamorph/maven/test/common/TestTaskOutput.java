package com.github.seregamorph.maven.test.common;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;

/**
 * @author Sergey Chernov
 */
public record TestTaskOutput(
    Instant startTime,
    Instant endTime,
    /*
    Time between startTime and endTime of test task
    */
    BigDecimal totalTimeSeconds,
    int totalClasses,
    /*
     Time in seconds from TEST-*.xml reports.
     TODO check why it does not match real test time.
     */
    BigDecimal totalTestTimeSeconds,
    int totalTests,
    int totalErrors,
    int totalFailures,
    // alias -> packed target name
    Map<String, String> files
) {
    public static TestTaskOutput empty() {
        return new TestTaskOutput(
            Instant.now(), Instant.now(), BigDecimal.ZERO,
            0, BigDecimal.ZERO,
            0, 0, 0, Collections.emptyMap());
    }
}
