package com.github.seregamorph.maven.test.storage;

import java.net.URI;
import java.time.Duration;

/**
 * @author Sergey Chernov
 */
public record HttpCacheStorageConfig(
    URI baseUrl,
    Duration connectTimeout,
    Duration readTimeout,
    Duration writeTimeout
) {
    public HttpCacheStorageConfig(URI baseUrl) {
        this(baseUrl, Duration.ofSeconds(5L), Duration.ofSeconds(10L), Duration.ofSeconds(10L));
    }
}
