package com.github.seregamorph.maven.test.storage;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class CacheServiceMetrics {

    private final AtomicInteger readOperations = new AtomicInteger();
    private final AtomicLong readNanos = new AtomicLong();

    private final AtomicInteger writeOperations = new AtomicInteger();
    private final AtomicLong writeNanos = new AtomicLong();

    public void addReadOperation(long nanos) {
        readOperations.incrementAndGet();
        readNanos.addAndGet(nanos);
    }

    public void addWriteOperation(long nanos) {
        writeOperations.incrementAndGet();
        writeNanos.addAndGet(nanos);
    }

    public int getReadOperations() {
        return readOperations.get();
    }

    public long getReadMillis() {
        return TimeUnit.NANOSECONDS.toMillis(readNanos.get());
    }

    public int getWriteOperations() {
        return writeOperations.get();
    }

    public long getWriteMillis() {
        return TimeUnit.NANOSECONDS.toMillis(writeNanos.get());
    }
}
