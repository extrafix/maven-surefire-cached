package com.github.seregamorph.maven.test.common;

import com.github.seregamorph.maven.test.util.ValidatorUtils;
import org.apache.maven.artifact.Artifact;

/**
 * @author Sergey Chernov
 */
public record GroupArtifactId(String groupId, String artifactId) {

    public GroupArtifactId {
        ValidatorUtils.validateFileName(groupId);
        ValidatorUtils.validateFileName(artifactId);
    }

    public static GroupArtifactId of(Artifact artifact) {
        return new GroupArtifactId(artifact.getGroupId(), artifact.getArtifactId());
    }

    public static GroupArtifactId fromString(String str) {
        var parts = str.split(":");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid format: [" + str + "]");
        }
        return new GroupArtifactId(parts[0], parts[1]);
    }

    @Override
    public String toString() {
        return groupId + ':' + artifactId;
    }
}
