package com.github.seregamorph.maven.test.storage;

import com.github.seregamorph.maven.test.common.CacheEntryKey;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * In-memory cache LRU storage.
 *
 * @author Sergey Chernov
 */
public class MemoryStorage implements CacheStorage {

    private final Map<CacheEntryKey, Map<String, byte[]>> cache = new LinkedHashMap<>();

    private final int size;

    public MemoryStorage(int size) {
        this.size = size;
    }

    @Override
    @Nullable
    public byte[] read(CacheEntryKey cacheEntryKey, String fileName) {
        synchronized (cache) {
            var cacheEntry = cache.remove(cacheEntryKey);
            if (cacheEntry == null) {
                return null;
            }
            // LRU
            cache.put(cacheEntryKey, cacheEntry);
            synchronized (cacheEntry) {
                return cacheEntry.get(fileName);
            }
        }
    }

    @Override
    public int write(CacheEntryKey cacheEntryKey, String fileName, byte[] value) {
        int deleted = 0;
        synchronized (cache) {
            var cacheEntry = cache.remove(cacheEntryKey);
            if (cacheEntry == null) {
                if (cache.size() >= size) {
                    // LRU
                    var iterator = cache.entrySet().iterator();
                    var deletedEntry = iterator.next().getValue();
                    synchronized (deletedEntry) {
                        deleted = deletedEntry.size();
                    }
                    iterator.remove();
                }
                cacheEntry = new LinkedHashMap<>();
            }
            cache.put(cacheEntryKey, cacheEntry);
            synchronized (cacheEntry) {
                cacheEntry.put(fileName, value);
            }
        }
        return deleted;
    }
}
