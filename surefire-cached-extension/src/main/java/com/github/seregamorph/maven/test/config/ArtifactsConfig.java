package com.github.seregamorph.maven.test.config;

import java.util.List;

/**
 * @author Sergey Chernov
 */
public class ArtifactsConfig {

    private List<String> includes;

    public ArtifactsConfig setIncludes(List<String> includes) {
        this.includes = includes;
        return this;
    }

    public List<String> getIncludes() {
        return includes;
    }

    @Override
    public String toString() {
        return "ArtifactsConfig{" +
            "includes=" + includes +
            '}';
    }
}
