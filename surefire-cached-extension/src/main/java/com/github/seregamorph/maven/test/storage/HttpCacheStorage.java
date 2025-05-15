package com.github.seregamorph.maven.test.storage;

import com.github.seregamorph.maven.test.common.CacheEntryKey;
import com.github.seregamorph.maven.test.util.ResponseBodyUtils;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Sergey Chernov
 */
public class HttpCacheStorage implements CacheStorage {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpCacheStorage.class);

    private static final MediaType TYPE = MediaType.get("application/octet-stream");

    private final URI baseUrl;

    private final OkHttpClient client;

    public HttpCacheStorage(URI baseUrl) {
        this.baseUrl = baseUrl;

        // todo configurable
        this.client = createHttpClient();
    }

    private static OkHttpClient createHttpClient() {
        return new OkHttpClient.Builder()
            .connectTimeout(5L, TimeUnit.SECONDS)
            .readTimeout(10L, TimeUnit.SECONDS)
            .writeTimeout(10L, TimeUnit.SECONDS)
            .build();
    }

    @Nullable
    @Override
    public byte[] read(CacheEntryKey cacheEntryKey, String fileName) {
        var url = getEntryUri(cacheEntryKey, fileName);
        try {
            Request request = new Request.Builder()
                .get()
                .url(url)
                .build();
            LOGGER.info("Fetching from cache: {}", url);
            try (Response response = client.newCall(request).execute()) {
                if (response.code() == 404) {
                    return null;
                }
                ResponseBody responseBody = response.body();
                if (responseBody == null) {
                    throw new IOException("No response body with response code: " + response.code());
                }
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected response code: " + response.code()
                        + "\n" + ResponseBodyUtils.responseBodyForLog(responseBody.string()));
                }
                return responseBody.bytes();
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Error while fetching from cache " + url, e);
        }
    }

    @Override
    public int write(CacheEntryKey cacheEntryKey, String fileName, byte[] value) {
        var url = getEntryUri(cacheEntryKey, fileName);
        try {
            var requestBody = RequestBody.create(value, TYPE);
            Request request = new Request.Builder()
                .put(requestBody)
                .url(url)
                .build();
            LOGGER.info("Pushing to cache: {}", url);
            try (Response response = client.newCall(request).execute()) {
                ResponseBody responseBody = response.body();
                if (responseBody == null) {
                    throw new IOException("No response body with response code: " + response.code());
                }
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected response code: " + response.code()
                        + "\n" + ResponseBodyUtils.responseBodyForLog(responseBody.string()));
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Error while pushing to cache " + url, e);
        }

        return 0;
    }

    private String getEntryUri(CacheEntryKey cacheEntryKey, String fileName) {
        return baseUrl + "/" + cacheEntryKey + "/" + fileName;
    }
}
