package com.github.seregamorph.maven.test.extension;

import static com.github.seregamorph.maven.test.extension.TestTaskCacheHelper.filterPrivate;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
        assertEquals("******", filterPrivate("deploy.password", "abcdef0123456789"));
        assertEquals("******", filterPrivate("env.NEXUS_PASSWORD", "abcdef0123456789"));
        assertEquals("******", filterPrivate("env.AWS_SECRET_ACCESS_KEY", "abcdef0123456789"));
        // note: in Maven 3 "env." prefix is optional, in Maven 4 - mandatory
        assertEquals("******", filterPrivate("AWS_SESSION_TOKEN", "abcdef0123456789"));
        assertEquals("1.2", filterPrivate("project.version", "1.2"));
        assertEquals("refs/pull/1234/merge", filterPrivate("env.GITHUB_REF", "refs/pull/1234/merge"));
    }
}
