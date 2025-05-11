package com.github.seregamorph.maven.test.extension;

import static com.github.seregamorph.maven.test.common.TestTaskOutput.PROP_SUFFIX_TEST_CACHED_RESULT;
import static com.github.seregamorph.maven.test.common.TestTaskOutput.PROP_SUFFIX_TEST_CACHED_TIME;
import static com.github.seregamorph.maven.test.common.TestTaskOutput.PROP_SUFFIX_TEST_DELETED_ENTRIES;
import static com.github.seregamorph.maven.test.util.ReflectionUtils.call;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.github.seregamorph.maven.test.common.CacheEntryKey;
import com.github.seregamorph.maven.test.common.GroupArtifactId;
import com.github.seregamorph.maven.test.common.TaskOutcome;
import com.github.seregamorph.maven.test.common.TestTaskOutput;
import com.github.seregamorph.maven.test.core.JsonSerializers;
import com.github.seregamorph.maven.test.core.SurefireCachedConfig;
import com.github.seregamorph.maven.test.core.TestSuiteReport;
import com.github.seregamorph.maven.test.core.TestTaskInput;
import com.github.seregamorph.maven.test.storage.CacheService;
import com.github.seregamorph.maven.test.util.MoreFileUtils;
import com.github.seregamorph.maven.test.util.ZipUtils;
import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.annotation.Nullable;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

/**
 * @author Sergey Chernov
 */
public class CachedSurefireDelegateMojo extends AbstractMojo {

    private static final String CONFIG_FILE_NAME = "surefire-cached.json";

    private final TestTaskCacheHelper testTaskCacheHelper;
    private final CacheService cacheService;
    private final MavenSession session;
    private final MavenProject project;
    private final Mojo delegate;
    private final String pluginName;

    private final Log log;
    private final File projectBuildDirectory;
    private final File reportsDirectory;

    public CachedSurefireDelegateMojo(
            TestTaskCacheHelper testTaskCacheHelper,
            MavenSession session,
            MavenProject project,
            Mojo delegate,
            String pluginName
    ) {
        this.testTaskCacheHelper = testTaskCacheHelper;
        this.cacheService = testTaskCacheHelper.getCacheService();
        this.session = session;
        this.project = project;
        this.delegate = delegate;
        this.pluginName = pluginName;

        this.log = delegate.getLog();
        // "target" subdir of basedir
        this.projectBuildDirectory = new File(project.getBuild().getDirectory());
        // "target/surefire-reports" or "target/failsafe-reports"
        this.reportsDirectory = call(delegate, File.class, "getReportsDirectory");
    }

    private void setCachedExecution(TaskOutcome result, TestTaskOutput testTaskOutput) {
        project.getProperties().put(pluginName + PROP_SUFFIX_TEST_CACHED_RESULT, result.name());
        project.getProperties().put(pluginName + PROP_SUFFIX_TEST_CACHED_TIME,
                testTaskOutput.totalTimeSeconds().toString());

        var message = result.message(testTaskOutput);
        log.info("Cached execution "
                + project.getGroupId() + ":" + project.getArtifactId()
                + " " + result + (message == null ? "" : " " + message));
    }

    private void setCachedDeletion(int deleted) {
        if (deleted > 0) {
            project.getProperties()
                    .put(pluginName + PROP_SUFFIX_TEST_DELETED_ENTRIES, Integer.toString(deleted));
        }
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (call(this.delegate, Boolean.class, "isSkip")
                || call(this.delegate, Boolean.class, "isSkipTests")
                || call(this.delegate, Boolean.class, "isSkipExec")
                || "pom".equals(project.getPackaging())
                || !projectBuildDirectory.exists()) {
            delegate.execute();
            return;
        }

        var startTime = Instant.now();

        MoreFileUtils.delete(reportsDirectory);

        var skipCache = isEmptyOrTrue(session.getSystemProperties().getProperty("skipCache"));
        if (skipCache) {
            delegate.execute();
            var testTaskOutput = getTaskOutput(null, startTime, Instant.now());
            setCachedExecution(TaskOutcome.SKIPPED_CACHE, testTaskOutput);
            return;
        }

        SurefireCachedConfig surefireCachedConfig = loadSurefireCachedConfig();
        var testPluginConfig = getTestPluginConfig(surefireCachedConfig);

        var taskInputFile = new File(projectBuildDirectory, getTaskInputFileName());
        var taskOutputFile = new File(projectBuildDirectory, getTaskOutputFileName());
        MoreFileUtils.delete(taskInputFile);
        MoreFileUtils.delete(taskOutputFile);

        var activeProfiles = session.getRequest().getActiveProfiles();
        var testTaskInput = testTaskCacheHelper.getTestTaskInput(activeProfiles, project, this.delegate, testPluginConfig);
        var testTaskInputBytes = JsonSerializers.serialize(testTaskInput);
        log.debug(new String(testTaskInputBytes, UTF_8));
        MoreFileUtils.write(taskInputFile, testTaskInputBytes);
        var cacheEntryKey = getLayoutKey(testTaskInput);

        // TODO calculate cache operation time (hash, load, store, etc.)
        var entryCalculatedTime = Instant.now();
        log.debug("Cache entry calculated in " + Duration.between(startTime, entryCalculatedTime));

        var testTaskOutputBytes = cacheService.read(cacheEntryKey, getTaskOutputFileName());
        if (testTaskOutputBytes == null) {
            log.info("Cache miss " + cacheEntryKey);
            boolean success = false;
            try {
                delegate.execute();
                success = true;
            } finally {
                var testTaskOutput = getTaskOutput(testPluginConfig, startTime, Instant.now());
                MoreFileUtils.write(taskOutputFile, JsonSerializers.serialize(testTaskOutput));
                if (testTaskOutput.totalErrors() > 0 || testTaskOutput.totalFailures() > 0) {
                    log.warn("Tests failed, not storing to cache. See " + reportsDirectory);
                    setCachedExecution(TaskOutcome.FAILED, testTaskOutput);
                } else if (success) {
                    log.info("Storing artifacts to cache from " + projectBuildDirectory);
                    var deleted = storeCache(testPluginConfig, cacheEntryKey, testTaskInput, testTaskOutput);
                    var result = testTaskOutput.totalTests() == 0 ? TaskOutcome.EMPTY : TaskOutcome.SUCCESS;
                    setCachedExecution(result, testTaskOutput);
                    setCachedDeletion(deleted);
                }
            }
        } else {
            MoreFileUtils.write(taskOutputFile, testTaskOutputBytes);
            log.info("Cache hit " + cacheEntryKey);
            var testTaskOutput = JsonSerializers.deserialize(testTaskOutputBytes, TestTaskOutput.class,
                    getTaskOutputFileName());
            log.info("Restoring artifacts from cache to " + projectBuildDirectory);
            restoreCache(cacheEntryKey, testTaskOutput);
            setCachedExecution(TaskOutcome.FROM_CACHE, testTaskOutput);
        }
    }

    SurefireCachedConfig loadSurefireCachedConfig() {
        MavenProject currentProject = this.project;
        do {
            for (String fileName : List.of(CONFIG_FILE_NAME, ".mvn/" + CONFIG_FILE_NAME)) {
                File surefireCachedConfigFile = new File(currentProject.getBasedir(), fileName);
                if (surefireCachedConfigFile.exists()) {
                    return JsonSerializers.deserialize(
                            MoreFileUtils.read(surefireCachedConfigFile), SurefireCachedConfig.class,
                            surefireCachedConfigFile.toString());
                }
            }
            currentProject = currentProject.getParent();
        } while (currentProject != null && currentProject.getBasedir() != null);

        throw new IllegalStateException("Unable to find surefire cached config file in "
                + new File(this.project.getBasedir(), CONFIG_FILE_NAME) + " or parent Maven project");
    }

    private int storeCache(
            SurefireCachedConfig.TestPluginConfig testPluginConfig,
            CacheEntryKey cacheEntryKey,
            TestTaskInput testTaskInput,
            TestTaskOutput testTaskOutput
    ) {
        int deleted = cacheService.write(cacheEntryKey, getTaskInputFileName(),
                JsonSerializers.serialize(testTaskInput));
        for (Map.Entry<String, SurefireCachedConfig.ArtifactsConfig> entry : testPluginConfig.getArtifacts().entrySet()) {
            var alias = entry.getKey();
            var artifactsConfig = entry.getValue();
            var artifactPackName = getArtifactPackName(alias);
            var packFile = new File(projectBuildDirectory, artifactPackName);
            MoreFileUtils.delete(packFile);
            ZipUtils.packDirectory(projectBuildDirectory, artifactsConfig.getIncludes(), packFile);
            deleted += cacheService.write(cacheEntryKey, artifactPackName, MoreFileUtils.read(packFile));
        }
        var testTaskOutputBytes = JsonSerializers.serialize(testTaskOutput);
        deleted += cacheService.write(cacheEntryKey, getTaskOutputFileName(), testTaskOutputBytes);
        return deleted;
    }

    private void restoreCache(CacheEntryKey cacheEntryKey, TestTaskOutput testTaskOutput) {
        testTaskOutput.files().forEach((alias, packedName) -> {
            var packedContent = cacheService.read(cacheEntryKey, packedName);
            if (packedContent == null) {
                throw new IllegalStateException("Cache file not found " + cacheEntryKey + " " + packedName);
            }

            var packFile = new File(projectBuildDirectory, packedName);
            MoreFileUtils.write(packFile, packedContent);
            ZipUtils.unpackDirectory(packFile, projectBuildDirectory);
            MoreFileUtils.delete(packFile);
        });
    }

    private CacheEntryKey getLayoutKey(TestTaskInput testTaskInput) {
        return new CacheEntryKey(
                pluginName,
                new GroupArtifactId(project.getGroupId(), project.getArtifactId()),
                testTaskInput.hash());
    }

    private TestTaskOutput getTaskOutput(
            @Nullable SurefireCachedConfig.TestPluginConfig testPluginConfig,
            Instant startTime,
            Instant endTime
    ) {
        var testReports = reportsDirectory.listFiles((dir, name) ->
                name.startsWith("TEST-") && name.endsWith(".xml"));

        if (testReports == null) {
            return TestTaskOutput.empty();
        }

        int totalClasses = 0;
        BigDecimal totalTestTimeSeconds = BigDecimal.ZERO;
        int totalTests = 0;
        int totalErrors = 0;
        int totalFailures = 0;
        for (var testReport : testReports) {
            var testSuiteSummary = TestSuiteReport.fromFile(testReport);
            totalClasses++;
            totalTestTimeSeconds = totalTestTimeSeconds.add(testSuiteSummary.timeSeconds());
            totalTests += testSuiteSummary.tests();
            totalErrors += testSuiteSummary.errors();
            totalFailures += testSuiteSummary.failures();
        }

        var files = new TreeMap<String, String>();
        if (testPluginConfig != null) {
            testPluginConfig.getArtifacts().keySet().forEach(alias -> {
                files.put(alias, getArtifactPackName(alias));
            });
        }

        return new TestTaskOutput(startTime, endTime, getTotalTimeSeconds(startTime, endTime),
                totalClasses, totalTestTimeSeconds, totalTests, totalErrors, totalFailures, files);
    }

    private static String getArtifactPackName(String alias) {
        return alias + ".tar.gz";
    }

    private SurefireCachedConfig.TestPluginConfig getTestPluginConfig(SurefireCachedConfig surefireCachedConfig) {
        if (TestTaskOutput.PLUGIN_SUREFIRE_CACHED.equals(pluginName)) {
            return surefireCachedConfig.getSurefire();
        } else if (TestTaskOutput.PLUGIN_FAILSAFE_CACHED.equals(pluginName)) {
            return surefireCachedConfig.getFailsafe();
        } else {
            throw new IllegalStateException("Unknown plugin " + pluginName);
        }
    }

    private static BigDecimal getTotalTimeSeconds(Instant startTime, Instant endTime) {
        var duration = Duration.between(startTime, endTime);
        long durationMillis = duration.toMillis();
        return BigDecimal.valueOf(durationMillis).divide(BigDecimal.valueOf(1000L), 3, RoundingMode.HALF_UP);
    }

    private String getTaskInputFileName() {
        return pluginName + "-input.json";
    }

    private String getTaskOutputFileName() {
        return pluginName + "-output.json";
    }

    private static boolean isEmptyOrTrue(String value) {
        return "".equals(value) || "true".equals(value);
    }
}
