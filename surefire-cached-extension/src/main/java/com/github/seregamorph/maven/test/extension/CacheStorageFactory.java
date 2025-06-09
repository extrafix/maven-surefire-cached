package com.github.seregamorph.maven.test.extension;

import com.github.seregamorph.maven.test.storage.CacheStorage;
import com.github.seregamorph.maven.test.storage.FileCacheStorage;
import com.github.seregamorph.maven.test.storage.HttpCacheStorage;
import com.github.seregamorph.maven.test.storage.HttpCacheStorageConfig;
import java.io.File;
import java.net.URI;
import org.apache.maven.execution.MavenSession;

/**
 * @author Sergey Chernov
 */
class CacheStorageFactory {

    private static final String PROP_CACHE_STORAGE_URL = "cacheStorageUrl";

    static CacheStorage createCacheStorage(MavenSession session) {
        String cacheStorageUrl = session.getUserProperties().getProperty(PROP_CACHE_STORAGE_URL);
        if (cacheStorageUrl == null) {
            cacheStorageUrl = System.getProperty("user.home") + "/.m2/test-cache";
        }
        //noinspection HttpUrlsUsage
        if (cacheStorageUrl.startsWith("http://") || cacheStorageUrl.startsWith("https://")) {
            // todo configuration
            var httpCacheStorageConfig = new HttpCacheStorageConfig(URI.create(cacheStorageUrl));
            return new HttpCacheStorage(httpCacheStorageConfig);
        }

        return new FileCacheStorage(new File(cacheStorageUrl));
    }

    private CacheStorageFactory() {
    }
}
