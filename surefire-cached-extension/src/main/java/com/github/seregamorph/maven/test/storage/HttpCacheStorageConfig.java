package com.github.seregamorph.maven.test.storage;

import java.net.URI;
import java.time.Duration;
import javax.annotation.Nullable;

/**
 * @author Sergey Chernov
 */
public final class HttpCacheStorageConfig {

    private final URI baseUrl;
    private final boolean checkServerVersion;
    private final Duration connectTimeout;
    private final Duration readTimeout;
    private final Duration writeTimeout;
    @Nullable
    private final String cacheHashPrefix;

    public HttpCacheStorageConfig(
        URI baseUrl,
        boolean checkServerVersion,
        Duration connectTimeout,
        Duration readTimeout,
        Duration writeTimeout,
        @Nullable String cacheHashPrefix
    ) {
        this.baseUrl = baseUrl;
        this.checkServerVersion = checkServerVersion;
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
        this.writeTimeout = writeTimeout;
        this.cacheHashPrefix = cacheHashPrefix;
    }

    public URI baseUrl() {
        return baseUrl;
    }

    public boolean checkServerVersion() {
        return checkServerVersion;
    }

    public Duration connectTimeout() {
        return connectTimeout;
    }

    public Duration readTimeout() {
        return readTimeout;
    }

    public Duration writeTimeout() {
        return writeTimeout;
    }

    public String cacheHashPrefix() {
        return cacheHashPrefix;
    }

    @Override
    public String toString() {
        return "HttpCacheStorageConfig{" +
            "baseUrl=" + baseUrl +
            ", checkServerVersion=" + checkServerVersion +
            ", connectTimeout=" + connectTimeout +
            ", readTimeout=" + readTimeout +
            ", writeTimeout=" + writeTimeout +
            ", cacheHashPrefix='" + cacheHashPrefix + '\'' +
            '}';
    }
}
