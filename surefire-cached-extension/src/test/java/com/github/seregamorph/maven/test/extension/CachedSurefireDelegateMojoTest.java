package com.github.seregamorph.maven.test.extension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.github.seregamorph.maven.test.common.PluginName;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Properties;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Build;
import org.apache.maven.plugin.Mojo;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.Test;

public class CachedSurefireDelegateMojoTest {

    @Test
    public void shouldReadConfig() {
        var testPomResource = getResourceURI("test-pom.xml");
        var testPomFile = new File(testPomResource.getPath());
        var basedir = testPomFile.getParentFile();

        var build = new Build();
        build.setDirectory(new File(basedir, "target").getAbsolutePath());
        var project = new MavenProject();
        project.setFile(testPomFile);
        project.setBuild(build);

        var userProperties = new Properties();
        var session = mock(MavenSession.class);
        when(session.getUserProperties()).thenReturn(userProperties);
        when(session.getAllProjects()).thenReturn(List.of(project));
        var delegate = mock(TestSurefireMojo.class);
        var testTaskCacheHelper = new TestTaskCacheHelper();
        testTaskCacheHelper.init(session);
        var pluginName = PluginName.SUREFIRE_CACHED;
        var cachedDelegateMojo = new CachedSurefireDelegateMojo(testTaskCacheHelper,
            testTaskCacheHelper.getCacheService(), testTaskCacheHelper.getCacheReport(),
            session, project, delegate, pluginName);

        var config = cachedDelegateMojo.loadTestPluginConfig(pluginName);
        assertEquals(List.of("com.acme:core"), config.getCacheExcludes());
        assertEquals(List.of("surefire-reports/TEST-*.xml"), config.getArtifacts().get("surefire-reports").getIncludes());
        assertEquals(List.of("jacoco-*.exec"), config.getArtifacts().get("jacoco").getIncludes());
    }

    private static URI getResourceURI(String name) {
        try {
            URL resource = CachedSurefireDelegateMojoTest.class.getClassLoader().getResource(name);
            if (resource == null) {
                throw new RuntimeException("Resource not found: " + name);
            }
            return resource.toURI();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private interface TestSurefireMojo extends Mojo {

        File getReportsDirectory();
    }
}
