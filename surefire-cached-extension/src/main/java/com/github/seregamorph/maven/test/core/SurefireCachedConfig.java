package com.github.seregamorph.maven.test.core;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.Map;

/**
 * Configuration entity "surefire-cached.json".
 *
 * @author Sergey Chernov
 */
@JsonIgnoreProperties({
    "//" // for comments
})
public class SurefireCachedConfig {

    static final TestPluginConfig DEFAULT_CONFIG = new TestPluginConfig()
        .setInputProperties(List.of("java.specification.version"))
        .setInputIgnoredProperties(List.of())
        .setCacheExcludes(List.of())
        .setArtifacts(Map.of());

    private TestPluginConfig common;
    private TestPluginConfig surefire;
    private TestPluginConfig failsafe;

    public TestPluginConfig getCommon() {
        return common;
    }

    public TestPluginConfig getSurefire() {
        return surefire;
    }

    public TestPluginConfig getFailsafe() {
        return failsafe;
    }

    public static class TestPluginConfig {

        /**
         * Properties that should be included into cache key calculation.
         * <p>
         * The "env." prefix may be used to resolve environment variables.
         * <p>
         * Default is "java.specification.version"
         */
        private List<String> inputProperties;

        /**
         * Properties that should be excluded from cache key calculation, but included to "surefire-cached-input.json" /
         * "failsafe-cached-input.json" files.
         * <p>
         * The "env." prefix may be used to resolve environment variables.
         * <p>
         * The ignored input properties always include "timestamp".
         * <p>
         * E.g. ["java.version", "os.arch", "os.name", "env.CI", "env.GITHUB_BASE_REF", "env.GITHUB_REF",
         * "env.GITHUB_RUN_ID", "env.GITHUB_JOB", "env.GITHUB_SHA"]
         */
        private List<String> inputIgnoredProperties;

        /**
         * List of "$groupId:$artifactId" for modules that should be excluded from cache key calculation
         */
        private List<String> cacheExcludes;

        private Map<String, ArtifactsConfig> artifacts;

        public TestPluginConfig setInputProperties(List<String> inputProperties) {
            this.inputProperties = inputProperties;
            return this;
        }

        public TestPluginConfig setInputIgnoredProperties(List<String> inputIgnoredProperties) {
            this.inputIgnoredProperties = inputIgnoredProperties;
            return this;
        }

        public TestPluginConfig setCacheExcludes(List<String> cacheExcludes) {
            this.cacheExcludes = cacheExcludes;
            return this;
        }

        public TestPluginConfig setArtifacts(Map<String, ArtifactsConfig> artifacts) {
            this.artifacts = artifacts;
            return this;
        }

        public List<String> getInputProperties() {
            return inputProperties;
        }

        public List<String> getInputIgnoredProperties() {
            return inputIgnoredProperties;
        }

        public List<String> getCacheExcludes() {
            return cacheExcludes;
        }

        public Map<String, ArtifactsConfig> getArtifacts() {
            return artifacts;
        }
    }

    public static class ArtifactsConfig {

        private List<String> includes;

        public ArtifactsConfig setIncludes(List<String> includes) {
            this.includes = includes;
            return this;
        }

        public List<String> getIncludes() {
            return includes;
        }
    }
}
