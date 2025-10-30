package com.github.seregamorph.maven.test.extension;

import com.github.seregamorph.maven.test.common.PluginName;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.Mojo;
import org.apache.maven.project.MavenProject;

/**
 * Delegates to org.apache.maven.plugin.surefire.SurefireMojo (surefire:test)
 * (org.apache.maven.plugin.surefire.SurefirePlugin for older maven-surefire-plugin versions)
 *
 * @author Sergey Chernov
 */
class SurefireCachedMojo extends AbstractCachedSurefireMojo {

    SurefireCachedMojo(
        TestTaskCacheHelper testTaskCacheHelper,
        MavenSession session,
        MavenProject project,
        Mojo delegate
    ) {
        super(testTaskCacheHelper, session, project, delegate, PluginName.SUREFIRE_CACHED);
    }
}
