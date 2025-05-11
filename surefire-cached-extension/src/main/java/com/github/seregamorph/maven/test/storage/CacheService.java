package com.github.seregamorph.maven.test.storage;

import com.github.seregamorph.maven.test.common.CacheEntryKey;
import javax.annotation.Nullable;

public class CacheService {

    private final CacheStorage cacheStorage;

    public CacheService(CacheStorage cacheStorage) {
        this.cacheStorage = cacheStorage;
    }

    @Nullable
    public byte[] read(CacheEntryKey cacheEntryKey, String fileName) {
        return cacheStorage.read(cacheEntryKey, fileName);
    }

    public int write(CacheEntryKey cacheEntryKey, String fileName, byte[] value) {
        return cacheStorage.write(cacheEntryKey, fileName, value);
    }
}
