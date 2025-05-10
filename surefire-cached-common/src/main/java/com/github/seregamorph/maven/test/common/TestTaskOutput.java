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
    Time between begin and end of test task
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
    // unpacked source name -> packed target name
    Map<String, String> files
) {
    public static TestTaskOutput empty() {
        return new TestTaskOutput(
            Instant.now(), Instant.now(), BigDecimal.ZERO,
            0, BigDecimal.ZERO,
            0, 0, 0, Collections.emptyMap());
    }

    public static final String PROP_SUFFIX_TEST_CACHED_RESULT = "_test-cached-result";
    public static final String PROP_SUFFIX_TEST_CACHED_TIME = "_test-cached-time";
    public static final String PROP_SUFFIX_TEST_DELETED_ENTRIES = "_test-deleted-entries";

    public static final String PLUGIN_SUREFIRE_CACHED = "surefire-cached";
    public static final String PLUGIN_FAILSAFE_CACHED = "failsafe-cached";
}
