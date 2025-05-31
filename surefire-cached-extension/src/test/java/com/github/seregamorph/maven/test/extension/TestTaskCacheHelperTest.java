package com.github.seregamorph.maven.test.extension;

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
}
