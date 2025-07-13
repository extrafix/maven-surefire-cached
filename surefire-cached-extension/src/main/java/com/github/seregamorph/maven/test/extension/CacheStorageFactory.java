package com.github.seregamorph.maven.test.extension;

import static com.github.seregamorph.maven.test.util.MavenPropertyUtils.isTrue;

import com.github.seregamorph.maven.test.storage.CacheStorage;
import com.github.seregamorph.maven.test.storage.FileCacheStorage;
import com.github.seregamorph.maven.test.storage.HttpCacheStorage;
import com.github.seregamorph.maven.test.storage.HttpCacheStorageConfig;
import com.github.seregamorph.maven.test.util.MavenPropertyUtils;
import java.io.File;
import java.net.URI;
import java.time.Duration;
import javax.annotation.Nullable;
import org.apache.maven.execution.MavenSession;

/**
 * @author Sergey Chernov
 */
class CacheStorageFactory {

    private static final String PROP_CACHE_STORAGE_URL = "cacheStorageUrl";

    private final MavenSession session;

    CacheStorageFactory(MavenSession session) {
        this.session = session;
    }

    CacheStorage createCacheStorage() {
        String cacheStorageUrl = getProperty(PROP_CACHE_STORAGE_URL, null);
        if (cacheStorageUrl == null) {
            String userHome = System.getProperty("user.home");
            if (userHome == null) {
                throw new IllegalStateException("Could not resolve default cacheStorageUrl, user.home is not defined.\n"
                    + "Please provide -D" + PROP_CACHE_STORAGE_URL + "= with directory or HTTP/HTTPS url of the cache");
            }
            cacheStorageUrl = userHome + "/.m2/test-cache";
        }
        //noinspection HttpUrlsUsage
        if (cacheStorageUrl.startsWith("http://") || cacheStorageUrl.startsWith("https://")) {
            return createHttpCacheStorage(cacheStorageUrl);
        }

        return new FileCacheStorage(new File(cacheStorageUrl));
    }

    private HttpCacheStorage createHttpCacheStorage(String cacheStorageUrl) {
        boolean checkServerVersion = isTrue(getProperty("cacheCheckServerVersion", "true"));
        Duration connectTimeout = Duration.ofSeconds(Integer.parseInt(
            getProperty("cacheConnectTimeoutSec", "5")));
        Duration readTimeout = Duration.ofSeconds(Integer.parseInt(
            getProperty("cacheReadTimeoutSec", "10")));
        Duration writeTimeout = Duration.ofSeconds(Integer.parseInt(
            getProperty("cacheWriteTimeoutSec", "10")));
        @Nullable String cacheHashPrefix = getProperty("cacheHashPrefix", null);
        HttpCacheStorageConfig httpCacheStorageConfig = new HttpCacheStorageConfig(
            URI.create(cacheStorageUrl), checkServerVersion,
            connectTimeout, readTimeout, writeTimeout,
            cacheHashPrefix);
        return new HttpCacheStorage(httpCacheStorageConfig);
    }

    private String getProperty(String propertyName, String defaultValue) {
        String value = MavenPropertyUtils.getProperty(session, propertyName);
        return value == null ? defaultValue : value;
    }
}
