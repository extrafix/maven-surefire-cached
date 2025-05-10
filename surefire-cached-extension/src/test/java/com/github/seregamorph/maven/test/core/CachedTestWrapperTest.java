package com.github.seregamorph.maven.test.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.seregamorph.maven.test.common.TestTaskOutput;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Build;
import org.apache.maven.plugin.Mojo;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class CachedTestWrapperTest {

    @Test
    public void shouldReadConfig() {
        var testPomResource = getResourceURI("test-pom.xml");
        var testPomFile = new File(testPomResource.getPath());
        var basedir = testPomFile.getParentFile();

        var session = Mockito.mock(MavenSession.class);
        var build = new Build();
        build.setDirectory(new File(basedir, "target").getAbsolutePath());
        var project = new MavenProject();
        project.setFile(testPomFile);
        project.setBuild(build);
        var delegate = Mockito.mock(TestSurefireMojo.class);
        var testTaskCacheHelper = new TestTaskCacheHelper();
        var cachedTestWrapper = new CachedTestWrapper(session, project, delegate, testTaskCacheHelper,
            "http://localhost:8080", TestTaskOutput.PLUGIN_SUREFIRE_CACHED);

        var config = cachedTestWrapper.loadSurefireCachedConfig();
        assertEquals(List.of("com.acme:core"), config.getCacheExcludes());
    }

    private static URI getResourceURI(String name) {
        try {
            URL resource = CachedTestWrapperTest.class.getClassLoader().getResource(name);
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
