package com.github.seregamorph.maven.test.storage;

import com.github.seregamorph.maven.test.common.CacheEntryKey;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FailsafeCacheStorage implements CacheStorage, ReportingCacheStorage {

    private static final Logger logger = LoggerFactory.getLogger(FailsafeCacheStorage.class);

    private final AtomicInteger readFailures = new AtomicInteger(0);
    private final AtomicInteger skippedReads = new AtomicInteger(0);

    private final AtomicInteger writeFailures = new AtomicInteger(0);
    private final AtomicInteger skippedWrites = new AtomicInteger(0);

    private final CacheStorage delegate;
    private final int threshold;

    public FailsafeCacheStorage(CacheStorage delegate, int threshold) {
        if (threshold <= 0) {
            throw new IllegalArgumentException("Threshold must be greater than 0");
        }
        this.delegate = delegate;
        this.threshold = threshold;
    }

    @Nullable
    @Override
    public byte[] read(CacheEntryKey cacheEntryKey, String fileName) {
        if (readFailures.get() >= threshold) {
            skippedReads.incrementAndGet();
            logger.info("Skipping reading {} {} because of too many failures", cacheEntryKey, fileName);
            return null;
        }

        try {
            return delegate.read(cacheEntryKey, fileName);
        } catch (CacheStorageException e) {
            logger.warn("Failed to read cache entry {}", e.toString());
            readFailures.incrementAndGet();
            return null;
        }
    }

    @Override
    public int write(CacheEntryKey cacheEntryKey, String fileName, byte[] value) {
        if (writeFailures.get() >= threshold) {
            skippedWrites.incrementAndGet();
            logger.info("Skipping writing {} {} because of too many failures", cacheEntryKey, fileName);
            return 0;
        }

        try {
            return delegate.write(cacheEntryKey, fileName, value);
        } catch (CacheStorageException e) {
            logger.warn("Failed to write cache entry {}", e.toString());
            writeFailures.incrementAndGet();
            return 0;
        }
    }

    @Override
    public String getReadFailureReport() {
        int readFailureCount = readFailures.get();
        if (readFailureCount == 0) {
            return null;
        }
        return String.format("Read failures: %d, then skipped %d operations", readFailureCount, skippedReads.get());
    }

    @Override
    public String getWriteFailureReport() {
        int writeFailureCount = writeFailures.get();
        if (writeFailureCount == 0) {
            return null;
        }
        return String.format("Write failures: %d, then skipped %d operations", writeFailureCount, skippedWrites.get());
    }
}
