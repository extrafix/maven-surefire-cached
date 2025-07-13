package com.github.seregamorph.maven.test.extension;

import static com.github.seregamorph.maven.test.extension.TestTaskCacheHelper.isPrivate;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.junit.jupiter.api.Test;

class TestTaskCacheHelperTest {

    @Test
    public void shouldIncludeToCacheEntry() {
        var artifactHandler = mock(ArtifactHandler.class);
        when(artifactHandler.isAddedToClasspath()).thenReturn(true);
        var artifact = mock(Artifact.class);
        when(artifact.getArtifactHandler()).thenReturn(artifactHandler);

        when(artifact.getGroupId()).thenReturn("com.acme");
        when(artifact.getArtifactId()).thenReturn("lib");

        assertTrue(TestTaskCacheHelper.isIncludeToCacheEntry(List.of("com.acme:lib1", "com.acme:lib2"), artifact));
        assertTrue(TestTaskCacheHelper.isIncludeToCacheEntry(List.of("com.example:*"), artifact));

        assertFalse(TestTaskCacheHelper.isIncludeToCacheEntry(List.of("com.acme:lib1", "com.acme:lib"), artifact));
        assertFalse(TestTaskCacheHelper.isIncludeToCacheEntry(List.of("com.acme:*"), artifact));
    }

    @Test
    public void shouldFilterPrivate() {
        assertTrue(isPrivate("deploy.password"));
        assertTrue(isPrivate("env.NEXUS_PASSWORD"));
        assertTrue(isPrivate("env.AWS_SECRET_ACCESS_KEY"));
        // note: in Maven 3 "env." prefix is optional, in Maven 4 - mandatory
        assertTrue(isPrivate("AWS_SESSION_TOKEN"));

        assertFalse(isPrivate("project.version"));
        assertFalse(isPrivate("env.GITHUB_REF"));
    }
}
