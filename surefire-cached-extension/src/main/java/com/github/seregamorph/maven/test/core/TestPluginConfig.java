package com.github.seregamorph.maven.test.core;

import java.util.List;
import java.util.Map;

public class TestPluginConfig {

    static final TestPluginConfig DEFAULT_CONFIG = new TestPluginConfig()
        .setInputProperties(List.of("java.specification.version"))
        .setInputIgnoredProperties(List.of())
        .setCacheExcludes(List.of())
        .setArtifacts(Map.of());
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

    private Map<String, SurefireCachedConfig.ArtifactsConfig> artifacts;

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

    public TestPluginConfig setArtifacts(Map<String, SurefireCachedConfig.ArtifactsConfig> artifacts) {
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

    public Map<String, SurefireCachedConfig.ArtifactsConfig> getArtifacts() {
        return artifacts;
    }
}
