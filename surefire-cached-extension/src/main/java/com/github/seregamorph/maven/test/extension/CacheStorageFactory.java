package com.github.seregamorph.maven.test.extension;

import com.github.seregamorph.maven.test.storage.CacheStorage;
import com.github.seregamorph.maven.test.storage.FileCacheStorage;
import com.github.seregamorph.maven.test.storage.HttpCacheStorage;
import java.io.File;
import java.net.URI;

public class CacheStorageFactory {

    static CacheStorage createCacheStorage(String cacheStorage) {
        //noinspection HttpUrlsUsage
        if (cacheStorage.startsWith("http://") || cacheStorage.startsWith("https://")) {
            return new HttpCacheStorage(URI.create(cacheStorage));
        }

        return new FileCacheStorage(new File(cacheStorage));
    }

    private CacheStorageFactory() {
    }
}
