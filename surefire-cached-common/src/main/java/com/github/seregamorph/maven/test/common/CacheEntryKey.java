package com.github.seregamorph.maven.test.common;

import com.github.seregamorph.maven.test.util.ValidatorUtils;
import java.util.List;

/**
 * @author Sergey Chernov
 */
public record CacheEntryKey(String pluginName, GroupArtifactId groupArtifactId, String hash) {

    private static final List<String> PLUGIN_NAMES = List.of("surefire-cached", "failsafe-cached");

    public CacheEntryKey {
        if (!PLUGIN_NAMES.contains(pluginName)) {
            throw new IllegalArgumentException("Unexpected pluginName [" + pluginName
                + "], allowed only " + PLUGIN_NAMES);
        }
        ValidatorUtils.validateFileName(hash);
    }

    @Override
    public String toString() {
        return pluginName + '/' + groupArtifactId.groupId() + '/' + groupArtifactId.artifactId() + '/' + hash;
    }
}
