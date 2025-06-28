package com.github.seregamorph.maven.test.extension;

import javax.annotation.Nullable;

/**
 * @author Sergey Chernov
 */
public interface PropertySource {

    @Nullable
    String getProperty(String name);
}
