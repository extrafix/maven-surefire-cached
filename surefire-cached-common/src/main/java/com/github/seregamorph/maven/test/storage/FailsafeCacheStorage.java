package com.github.seregamorph.maven.test.storage;

import com.github.seregamorph.maven.test.common.CacheEntryKey;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FailsafeCacheStorage implements CacheStorage {

    private static final Logger logger = LoggerFactory.getLogger(FailsafeCacheStorage.class);

    private final AtomicInteger failures = new AtomicInteger(0);

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
        if (failures.get() > threshold) {
            logger.info("Skipping reading {} {} because of too many failures", cacheEntryKey, fileName);
            return null;
        }

        try {
            return delegate.read(cacheEntryKey, fileName);
        } catch (CacheStorageException e) {
            logger.warn("Failed to read {} {}", cacheEntryKey, fileName, e);
            failures.incrementAndGet();
            return null;
        }
    }

    @Override
    public int write(CacheEntryKey cacheEntryKey, String fileName, byte[] value) {
        if (failures.get() > threshold) {
            logger.info("Skipping writing {} {} because of too many failures", cacheEntryKey, fileName);
            return 0;
        }

        try {
            return delegate.write(cacheEntryKey, fileName, value);
        } catch (CacheStorageException e) {
            logger.warn("Failed to write {} {}", cacheEntryKey, fileName, e);
            failures.incrementAndGet();
            return 0;
        }
    }
}
