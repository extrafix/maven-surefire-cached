package com.github.seregamorph.maven.test.common;

import com.github.seregamorph.maven.test.util.ValidatorUtils;

/**
 * @author Sergey Chernov
 */
public record CacheEntryKey(PluginName pluginName, GroupArtifactId groupArtifactId, String hash) {

    public CacheEntryKey {
        ValidatorUtils.validateFileName(hash);
    }

    @Override
    public String toString() {
        return pluginName + "/" + groupArtifactId.groupId() + '/' + groupArtifactId.artifactId() + '/' + hash;
    }
}
