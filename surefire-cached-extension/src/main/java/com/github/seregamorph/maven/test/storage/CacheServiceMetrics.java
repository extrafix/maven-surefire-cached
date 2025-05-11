package com.github.seregamorph.maven.test.storage;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class CacheServiceMetrics {

    private final AtomicInteger readHitOperations = new AtomicInteger();
    private final AtomicLong readHitNanos = new AtomicLong();
    private final AtomicLong readHitBytes = new AtomicLong();

    private final AtomicInteger readMissOperations = new AtomicInteger();
    private final AtomicLong readMissNanos = new AtomicLong();

    private final AtomicInteger writeOperations = new AtomicInteger();
    private final AtomicLong writeNanos = new AtomicLong();
    private final AtomicLong writeBytes = new AtomicLong();

    public void addReadHitOperation(long nanos, long bytes) {
        readHitOperations.incrementAndGet();
        readHitNanos.addAndGet(nanos);
        readHitBytes.addAndGet(bytes);
    }

    public void addReadMissOperation(long nanos) {
        readMissOperations.incrementAndGet();
        readMissNanos.addAndGet(nanos);
    }

    public void addWriteOperation(long nanos, long bytes) {
        writeOperations.incrementAndGet();
        writeNanos.addAndGet(nanos);
        writeBytes.addAndGet(bytes);
    }

    public int getReadHitOperations() {
        return readHitOperations.get();
    }

    public long getReadHitMillis() {
        return TimeUnit.NANOSECONDS.toMillis(readHitNanos.get());
    }

    public long getReadHitBytes() {
        return readHitBytes.get();
    }

    public int getReadMissOperations() {
        return readMissOperations.get();
    }

    public long getReadMissMillis() {
        return TimeUnit.NANOSECONDS.toMillis(readMissNanos.get());
    }

    public int getWriteOperations() {
        return writeOperations.get();
    }

    public long getWriteMillis() {
        return TimeUnit.NANOSECONDS.toMillis(writeNanos.get());
    }

    public long getWriteBytes() {
        return writeBytes.get();
    }
}
