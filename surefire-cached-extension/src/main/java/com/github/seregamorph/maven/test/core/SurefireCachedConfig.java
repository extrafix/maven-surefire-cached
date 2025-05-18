package com.github.seregamorph.maven.test.core;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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

    private TestPluginConfig common;
    private TestPluginConfig surefire;
    private TestPluginConfig failsafe;

    public SurefireCachedConfig setCommon(TestPluginConfig common) {
        this.common = common;
        return this;
    }

    public SurefireCachedConfig setSurefire(TestPluginConfig surefire) {
        this.surefire = surefire;
        return this;
    }

    public SurefireCachedConfig setFailsafe(TestPluginConfig failsafe) {
        this.failsafe = failsafe;
        return this;
    }

    public TestPluginConfig getCommon() {
        return common;
    }

    public TestPluginConfig getSurefire() {
        return surefire;
    }

    public TestPluginConfig getFailsafe() {
        return failsafe;
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
