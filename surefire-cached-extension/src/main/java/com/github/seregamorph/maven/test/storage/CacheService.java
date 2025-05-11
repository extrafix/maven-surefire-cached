package com.github.seregamorph.maven.test.storage;

import com.github.seregamorph.maven.test.common.CacheEntryKey;
import javax.annotation.Nullable;

public class CacheService {

    private final CacheStorage cacheStorage;
    private final CacheServiceMetrics metrics;

    public CacheService(CacheStorage cacheStorage, CacheServiceMetrics metrics) {
        this.cacheStorage = cacheStorage;
        this.metrics = metrics;
    }

    @Nullable
    public byte[] read(CacheEntryKey cacheEntryKey, String fileName) {
        long start = System.nanoTime();
        try {
            return cacheStorage.read(cacheEntryKey, fileName);
        } finally {
            metrics.addReadOperation(System.nanoTime() - start);
        }
    }

    public int write(CacheEntryKey cacheEntryKey, String fileName, byte[] value) {
        long start = System.nanoTime();
        try {
            return cacheStorage.write(cacheEntryKey, fileName, value);
        } finally {
            metrics.addWriteOperation(System.nanoTime() - start);
        }
    }
}
