package com.github.seregamorph.maven.test.extension;

import static com.github.seregamorph.maven.test.util.ReflectionUtils.call;

import com.github.seregamorph.maven.test.common.GroupArtifactId;
import com.github.seregamorph.maven.test.core.FileHashCache;
import com.github.seregamorph.maven.test.core.SurefireCachedConfig;
import com.github.seregamorph.maven.test.core.TestTaskInput;
import com.github.seregamorph.maven.test.storage.CacheService;
import com.github.seregamorph.maven.test.storage.CacheServiceMetrics;
import com.github.seregamorph.maven.test.util.HashUtils;
import com.github.seregamorph.maven.test.util.MavenPropertyUtils;
import java.io.File;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import javax.inject.Singleton;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.Mojo;
import org.apache.maven.project.MavenProject;

/**
 * @author Sergey Chernov
 */
@Singleton
public class TestTaskCacheHelper {

    private static final String PROP_CACHE_STORAGE_URL = "cacheStorageUrl";

    private FileHashCache fileHashCache;
    private Set<GroupArtifactId> modules;
    private CacheServiceMetrics metrics;
    private CacheService cacheService;

    public void init(MavenSession session) {
        fileHashCache = new FileHashCache();
        modules = session.getAllProjects().stream()
            .map(p -> new GroupArtifactId(p.getGroupId(), p.getArtifactId()))
            .collect(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(Object::toString))));

        this.metrics = new CacheServiceMetrics();
        this.cacheService = getCacheService(session, metrics);
    }

    public void destroy() {
        fileHashCache = null;
        modules = null;
        cacheService = null;
    }

    public CacheService getCacheService() {
        if (cacheService == null) {
            throw new IllegalStateException("cacheStorage is not initialized");
        }
        return cacheService;
    }

    public CacheServiceMetrics getMetrics() {
        if (metrics == null) {
            throw new IllegalStateException("metrics is not initialized");
        }
        return metrics;
    }

    private static CacheService getCacheService(MavenSession session, CacheServiceMetrics metrics) {
        String cacheStorageUrl = session.getUserProperties().getProperty(PROP_CACHE_STORAGE_URL);
        if (cacheStorageUrl == null) {
            cacheStorageUrl = System.getProperty("user.home") + "/.m2/test-cache";
        }
        var cacheStorage = CacheStorageFactory.createCacheStorage(cacheStorageUrl);
        return new CacheService(cacheStorage, metrics);
    }

    public TestTaskInput getTestTaskInput(
        MavenSession session,
        MavenProject project,
        Mojo delegate,
        SurefireCachedConfig surefireCachedConfig,
        SurefireCachedConfig.TestPluginConfig testPluginConfig
    ) {
        var activeProfiles = session.getRequest().getActiveProfiles();

        var testTaskInput = new TestTaskInput();

        testTaskInput.addIgnoredProperty("timestamp", Instant.now().toString());
        for (var ignoredProperty : surefireCachedConfig.getInputIgnoredProperties()) {
            var value = MavenPropertyUtils.getProperty(session, project, ignoredProperty);
            if (value != null) {
                testTaskInput.addIgnoredProperty(ignoredProperty, value);
            }
        }

        for (var property : surefireCachedConfig.getInputProperties()) {
            var value = MavenPropertyUtils.getProperty(session, project, property);
            if (value != null) {
                testTaskInput.addProperty(property, value);
            }
        }

        var pluginArtifacts = project.getPluginArtifacts();
        for (var pluginArtifact : pluginArtifacts) {
            var file = pluginArtifact.getFile();
            // now the file is always null, enhance the caching key on demand
            var hash = file == null ? null : fileHashCache.getClasspathElementHash(file);
            var groupArtifactId = groupArtifactId(pluginArtifact);
            testTaskInput.addPluginArtifactHash(groupArtifactId, pluginArtifact.getClassifier(),
                pluginArtifact.getVersion(), hash);
        }

        // todo add java version
        // todo system properties

        testTaskInput.setModuleName(project.getGroupId() + ":" + project.getArtifactId());
        var testClasspath = getTestClasspath(project);
        var cacheExcludes = testPluginConfig.getCacheExcludes().stream()
            .map(GroupArtifactId::fromString)
            .collect(Collectors.toSet());
        for (var artifact : testClasspath.artifacts()) {
            if (isIncludeToCacheEntry(artifact, cacheExcludes)) {
                // Can be a jar file (when "install" command is executed) or
                // a classes directory (when "test" command is executed).
                // The trick is we calculate hash of files which is the same in both cases (jar manifest is ignored)
                var file = artifact.getFile();
                var hash = fileHashCache.getClasspathElementHash(file);
                var groupArtifactId = groupArtifactId(artifact);
                var suffix = file.isDirectory() ? "@dir" : "@" + artifact.getType();
                if (modules.contains(groupArtifactId)) {
                    testTaskInput.addModuleArtifactHash(groupArtifactId + suffix, hash);
                } else {
                    testTaskInput.addLibraryArtifactHash(groupArtifactId, artifact.getClassifier(),
                            artifact.getVersion(), hash);
                }
            }
        }
        if (testClasspath.classesDir().exists()) {
            testTaskInput.setClassesHashes(HashUtils.hashDirectory(testClasspath.classesDir()));
        }
        if (testClasspath.testClassesDir().exists()) {
            testTaskInput.setTestClassesHashes(HashUtils.hashDirectory(testClasspath.testClassesDir()));
        }
        testTaskInput.setActiveProfiles(activeProfiles);
        testTaskInput.setArgLine(call(delegate, String.class, "getArgLine"));
        testTaskInput.setTest(call(delegate, String.class, "getTest"));
        testTaskInput.setArtifactConfigs(testPluginConfig.getArtifacts());
        testTaskInput.setExcludes(call(delegate, List.class, "getExcludes"));
        return testTaskInput;
    }

    private static boolean isIncludeToCacheEntry(Artifact artifact, Set<GroupArtifactId> cacheExcludes) {
        return artifact.getArtifactHandler().isAddedToClasspath()
            && !cacheExcludes.contains(groupArtifactId(artifact));
    }

    private static TestClasspath getTestClasspath(MavenProject project) {
        var artifacts = project.getArtifacts();
        var classesDir = new File(project.getBuild().getOutputDirectory());
        var testClassesDir = new File(project.getBuild().getTestOutputDirectory());
        return new TestClasspath(artifacts, classesDir, testClassesDir);
    }

    private static GroupArtifactId groupArtifactId(Artifact artifact) {
        return new GroupArtifactId(artifact.getGroupId(), artifact.getArtifactId());
    }

    private record TestClasspath(Set<Artifact> artifacts, File classesDir, File testClassesDir) {
    }
}
