package com.github.seregamorph.maven.test.extension;

import com.github.seregamorph.maven.test.storage.CacheStorage;
import com.github.seregamorph.maven.test.storage.FileCacheStorage;
import com.github.seregamorph.maven.test.storage.HttpCacheStorage;
import java.io.File;
import java.net.URI;

public class CacheStorageFactory {

    public static CacheStorage createCacheStorage(String cacheStorageUrl) {
        //noinspection HttpUrlsUsage
        if (cacheStorageUrl.startsWith("http://") || cacheStorageUrl.startsWith("https://")) {
            return new HttpCacheStorage(URI.create(cacheStorageUrl));
        }

        return new FileCacheStorage(new File(cacheStorageUrl));
    }

    private CacheStorageFactory() {
    }
}
