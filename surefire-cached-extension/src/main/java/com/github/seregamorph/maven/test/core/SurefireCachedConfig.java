package com.github.seregamorph.maven.test.core;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration entity "surefire-cached.json".
 *
 * @author Sergey Chernov
 */
@JsonIgnoreProperties({
        "//" // for comments
})
public class SurefireCachedConfig {

    /**
     * List of "$groupId:$artifactId" for modules that should be excluded from cache key calculation
     */
    private final List<String> cacheExcludes = new ArrayList<>();

    public List<String> getCacheExcludes() {
        return cacheExcludes;
    }
}
