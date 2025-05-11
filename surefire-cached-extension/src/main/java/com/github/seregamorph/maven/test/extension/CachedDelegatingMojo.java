package com.github.seregamorph.maven.test.extension;

import com.github.seregamorph.maven.test.core.CachedTestWrapper;
import com.github.seregamorph.maven.test.core.TestTaskCacheHelper;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

/**
 * @author Sergey Chernov
 */
public class CachedDelegatingMojo extends AbstractMojo {

    private final TestTaskCacheHelper testTaskCacheHelper;
    private final MavenSession session;
    private final MavenProject project;
    private final Mojo delegate;
    private final String pluginName;

    public CachedDelegatingMojo(
        TestTaskCacheHelper testTaskCacheHelper,
        MavenSession session,
        MavenProject project,
        Mojo delegate,
        String pluginName
    ) {
        this.testTaskCacheHelper = testTaskCacheHelper;
        this.session = session;
        this.project = project;
        this.delegate = delegate;
        this.pluginName = pluginName;
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        var cachedTestWrapper = new CachedTestWrapper(testTaskCacheHelper, session, project, delegate, pluginName);
        cachedTestWrapper.execute();
    }
}
