package com.github.seregamorph.maven.test.util;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author Sergey Chernov
 */
public final class ResponseBodyUtils {

    @Nonnull
    public static String responseBodyForLog(@Nullable String responseBody) {
        if (responseBody == null) {
            return "[empty]";
        }

        return responseBody.substring(0, Math.min(responseBody.length(), 1000));
    }

    private ResponseBodyUtils() {
    }
}
