package com.github.seregamorph.maven.test.core;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Configuration entity "surefire-cached.json".
 *
 * @author Sergey Chernov
 */
@JsonIgnoreProperties({
        "//" // for comments
})
public class SurefireCachedConfig {

    private final TestPluginConfig surefire = new TestPluginConfig();
    private final TestPluginConfig failsafe = new TestPluginConfig();

    public TestPluginConfig getSurefire() {
        return surefire;
    }

    public TestPluginConfig getFailsafe() {
        return failsafe;
    }

    public static class TestPluginConfig {

        /**
         * List of "$groupId:$artifactId" for modules that should be excluded from cache key calculation
         */
        private final List<String> cacheExcludes = new ArrayList<>();

        private final Map<String, ArtifactsConfig> artifacts = new TreeMap<>();

        public List<String> getCacheExcludes() {
            return cacheExcludes;
        }

        public Map<String, ArtifactsConfig> getArtifacts() {
            return artifacts;
        }
    }

    public static class ArtifactsConfig {

        private final List<String> includes = new ArrayList<>();

        public List<String> getIncludes() {
            return includes;
        }
    }
}
