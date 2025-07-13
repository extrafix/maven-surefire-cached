package com.github.seregamorph.maven.test.util;

import javax.annotation.Nullable;

/**
 * @author Sergey Chernov
 */
public interface PropertySource {

    @Nullable
    String getProperty(String propertyName);

    default String getProperty(String propertyName, String defaultValue) {
        String value = getProperty(propertyName);
        return value == null ? defaultValue : value;
    }
}
