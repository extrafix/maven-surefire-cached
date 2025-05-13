package com.github.seregamorph.maven.test.core;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.Arrays;
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

    /**
     * Properties that should be included into cache key calculation.
     * <p>
     * The "env." prefix may be used to resolve environment variables.
     */
    private final List<String> inputProperties = new ArrayList<>(Arrays.asList(
        "java.specification.version"
    ));

    /**
     * Properties that should be excluded from cache key calculation, but included to "surefire-cached-input.json" /
     * "failsafe-cached-input.json" files.
     * <p>
     * The "env." prefix may be used to resolve environment variables.
     * <p>
     * The ignored input properties always include "timestamp".
     * <p>
     * E.g. "java.version", "os.arch", "os.name", "env.CI", "env.GITHUB_BASE_REF", "env.GITHUB_REF",
     * "env.GITHUB_RUN_ID", "env.GITHUB_JOB", "env.GITHUB_SHA"
     */
    private final List<String> inputIgnoredProperties = new ArrayList<>();

    private final TestPluginConfig surefire = new TestPluginConfig();
    private final TestPluginConfig failsafe = new TestPluginConfig();

    public List<String> getInputProperties() {
        return inputProperties;
    }

    public List<String> getInputIgnoredProperties() {
        return inputIgnoredProperties;
    }

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
