package com.github.seregamorph.maven.test.storage;

import com.github.seregamorph.maven.test.common.CacheEntryKey;
import javax.annotation.Nullable;

/**
 * @author Sergey Chernov
 */
public interface CacheStorage {

    @Nullable
    byte[] read(CacheEntryKey cacheEntryKey, String fileName);

    int write(CacheEntryKey cacheEntryKey, String fileName, byte[] value);
}
