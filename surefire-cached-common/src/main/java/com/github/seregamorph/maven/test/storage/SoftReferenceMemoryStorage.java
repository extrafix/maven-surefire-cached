package com.github.seregamorph.maven.test.storage;

import com.github.seregamorph.maven.test.common.CacheEntryKey;
import java.lang.ref.SoftReference;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * In-memory cache LRU storage with SoftReference.
 *
 * @author Sergey Chernov
 */
public class SoftReferenceMemoryStorage implements CacheStorage {

    private final Map<CacheEntryKey, SoftReference<Map<String, byte[]>>> cache = new LinkedHashMap<>();

    private final int size;

    public SoftReferenceMemoryStorage(int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("size must be > 0");
        }
        this.size = size;
    }

    @Override
    @Nullable
    public byte[] read(CacheEntryKey cacheEntryKey, String fileName) {
        synchronized (cache) {
            var softReferenceCacheEntry = cache.remove(cacheEntryKey);
            if (softReferenceCacheEntry == null) {
                return null;
            }
            var cacheEntry = softReferenceCacheEntry.get();
            if (cacheEntry == null) {
                return null;
            }
            // LRU
            cache.put(cacheEntryKey, softReferenceCacheEntry);
            return cacheEntry.get(fileName);
        }
    }

    @Override
    public int write(CacheEntryKey cacheEntryKey, String fileName, byte[] value) {
        int deleted = 0;
        synchronized (cache) {
            var softReferenceCacheEntry = cache.remove(cacheEntryKey);
            var cacheEntry = softReferenceCacheEntry == null ? null : softReferenceCacheEntry.get();
            if (cacheEntry == null) {
                if (cache.size() >= size) {
                    // LRU
                    var iterator = cache.entrySet().iterator();
                    var deletedReference = iterator.next().getValue();
                    synchronized (deletedReference) {
                        var deletedEntry = deletedReference.get();
                        deleted = deletedEntry == null ? 0 : deletedEntry.size();
                    }
                    iterator.remove();
                }
                cacheEntry = new LinkedHashMap<>();
            }
            cache.put(cacheEntryKey, new SoftReference<>(cacheEntry));
            cacheEntry.put(fileName, value);
        }
        return deleted;
    }
}
