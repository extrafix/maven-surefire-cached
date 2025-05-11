package com.github.seregamorph.maven.test.extension;

import static com.github.seregamorph.maven.test.util.ReflectionUtils.call;

import com.github.seregamorph.maven.test.common.GroupArtifactId;
import com.github.seregamorph.maven.test.core.FileHashCache;
import com.github.seregamorph.maven.test.core.SurefireCachedConfig;
import com.github.seregamorph.maven.test.core.TestTaskInput;
import com.github.seregamorph.maven.test.storage.CacheService;
import com.github.seregamorph.maven.test.util.HashUtils;
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
    private CacheService cacheService;

    public void init(MavenSession session) {
        fileHashCache = new FileHashCache();
        modules = session.getAllProjects().stream()
            .map(p -> new GroupArtifactId(p.getGroupId(), p.getArtifactId()))
            .collect(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(Object::toString))));

        this.cacheService = getCacheService(session);
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

    private static CacheService getCacheService(MavenSession session) {
        String cacheStorageUrl = session.getUserProperties().getProperty(PROP_CACHE_STORAGE_URL);
        if (cacheStorageUrl == null) {
            cacheStorageUrl = System.getProperty("user.home") + "/.m2/test-cache";
        }
        var cacheStorage = CacheStorageFactory.createCacheStorage(cacheStorageUrl);
        return new CacheService(cacheStorage);
    }

    public TestTaskInput getTestTaskInput(
        List<String> activeProfiles,
        MavenProject project,
        Mojo delegate,
        SurefireCachedConfig.TestPluginConfig testPluginConfig
    ) {
        var testTaskInput = new TestTaskInput();
        testTaskInput.addIgnoredProperty("timestamp", Instant.now().toString());
        // todo git commit hash

        var pluginArtifacts = project.getPluginArtifacts();
        for (var pluginArtifact : pluginArtifacts) {
            var file = pluginArtifact.getFile();
            // now the file is always null, enhance the caching key on demand
            var hash = file == null ? null : fileHashCache.getClasspathElementHash(file);
            var groupArtifactId = GroupArtifactId.of(pluginArtifact);
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
                var groupArtifactId = GroupArtifactId.of(artifact);
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
            && !cacheExcludes.contains(GroupArtifactId.of(artifact));
    }

    private static TestClasspath getTestClasspath(MavenProject project) {
        var artifacts = project.getArtifacts();
        var classesDir = new File(project.getBuild().getOutputDirectory());
        var testClassesDir = new File(project.getBuild().getTestOutputDirectory());
        return new TestClasspath(artifacts, classesDir, testClassesDir);
    }

    private record TestClasspath(Set<Artifact> artifacts, File classesDir, File testClassesDir) {
    }
}
