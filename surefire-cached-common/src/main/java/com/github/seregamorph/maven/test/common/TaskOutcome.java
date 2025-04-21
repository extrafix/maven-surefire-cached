package com.github.seregamorph.maven.test.common;

import javax.annotation.Nullable;

/**
 * @author Sergey Chernov
 */
public enum TaskOutcome {

    SKIPPED_CACHE(null),
    SUCCESS("serial time") {
        @Override
        public String message(TestTaskOutput testTaskOutput) {
            return "(" + testTaskOutput.totalTests() + " tests"
                + ", " + testTaskOutput.totalTime() + "s)";
        }
    },
    FAILED("serial time") {
        @Override
        public String message(TestTaskOutput testTaskOutput) {
            return "(errors " + testTaskOutput.totalErrors()
                + ", failures " + testTaskOutput.totalFailures() + ")";
        }
    },
    EMPTY(null),
    FROM_CACHE("serial time saved") {
        @Override
        public String message(TestTaskOutput testTaskOutput) {
            return "(saved " + testTaskOutput.totalTime() + "s)";
        }
    };

    private final String suffix;

    TaskOutcome(String suffix) {
        this.suffix = suffix;
    }

    @Nullable
    public String suffix() {
        return suffix;
    }

    @Nullable
    public String message(TestTaskOutput testTaskOutput) {
        return null;
    }
}
