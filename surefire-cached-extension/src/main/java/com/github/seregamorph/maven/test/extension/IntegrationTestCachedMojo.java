package com.github.seregamorph.maven.test.extension;

import com.github.seregamorph.maven.test.common.PluginName;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.Mojo;
import org.apache.maven.project.MavenProject;

/**
 * Delegates to org.apache.maven.plugin.failsafe.IntegrationTestMojo (failsafe:integration-test)
 *
 * @author Sergey Chernov
 */
class IntegrationTestCachedMojo extends AbstractCachedSurefireMojo {

    IntegrationTestCachedMojo(
        TestTaskCacheHelper testTaskCacheHelper,
        MavenSession session,
        MavenProject project,
        Mojo delegate
    ) {
        super(testTaskCacheHelper, session, project, delegate, PluginName.FAILSAFE_CACHED);
    }
}
