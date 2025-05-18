package com.github.seregamorph.maven.test.extension;

import static com.github.seregamorph.maven.test.util.ByteSizeFormatUtils.formatByteSize;
import static com.github.seregamorph.maven.test.util.TimeFormatUtils.formatTime;
import static com.github.seregamorph.maven.test.util.TimeFormatUtils.toSeconds;

import com.github.seregamorph.maven.test.common.PluginName;
import com.github.seregamorph.maven.test.core.TaskOutcome;
import com.github.seregamorph.maven.test.storage.CacheServiceMetrics;
import com.github.seregamorph.maven.test.util.JsonSerializers;
import com.github.seregamorph.maven.test.util.MoreFileUtils;
import java.io.File;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.maven.AbstractMavenLifecycleParticipant;
import org.apache.maven.SessionScoped;
import org.apache.maven.execution.MavenSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
Hint: monitor values Dashboard
* Stored entries
* Evicted entries
* Cache hits
* Cache misses
* Cache hit rate
* Data Received
* Data Sent
 */

/**
 * @author Sergey Chernov
 */
@SessionScoped
@Named
public class CachedTestLifecycleParticipant extends AbstractMavenLifecycleParticipant {

    private static final Logger logger = LoggerFactory.getLogger(CachedTestLifecycleParticipant.class);

    private record AggResult(int totalModules, BigDecimal totalTimeSec) {
        private static final AggResult EMPTY = new AggResult(0, BigDecimal.ZERO);

        AggResult add(BigDecimal time) {
            return new AggResult(totalModules + 1, totalTimeSec.add(time));
        }
    }

    private final TestTaskCacheHelper testTaskCacheHelper;

    @Inject
    public CachedTestLifecycleParticipant(TestTaskCacheHelper testTaskCacheHelper) {
        this.testTaskCacheHelper = testTaskCacheHelper;
    }

    @Override
    public void afterProjectsRead(MavenSession session) {
        this.testTaskCacheHelper.init(session);
    }

    @Override
    public void afterSessionEnd(MavenSession session) {
        var cacheReport = testTaskCacheHelper.getCacheReport();
        var pluginResults = new TreeMap<String, Map<TaskOutcome, AggResult>>();
        for (var pluginName : List.of(PluginName.SUREFIRE_CACHED, PluginName.FAILSAFE_CACHED)) {
            var pluginResult = new TreeMap<TaskOutcome, AggResult>();
            int deleted = 0;
            var executionResults = cacheReport.getExecutionResults(pluginName);
            for (var executionResult : executionResults) {
                pluginResult.compute(executionResult.result(),
                    (k, v) -> (v == null ? AggResult.EMPTY : v).add(executionResult.totalTimeSeconds()));
                deleted += executionResult.deletedCacheEntries();
            }
            if (!pluginResult.isEmpty()) {
                logger.info("Total test cached results ({}):", pluginName);
                pluginResult.forEach((k, v) -> {
                    var suffix = k.suffix();
                    logger.info("{} ({} modules): {}{}", k, v.totalModules,
                        formatTime(v.totalTimeSec), suffix == null ? "" : " " + suffix);
                });
                if (deleted > 0) {
                    logger.info("Total deleted cache entries: {}", deleted);
                }
                pluginResults.put(pluginName.name(), pluginResult);
            }
        }
        logStorageMetrics(testTaskCacheHelper.getMetrics());
        saveJsonReport(session, pluginResults);
        this.testTaskCacheHelper.destroy();
    }

    private void saveJsonReport(MavenSession session, TreeMap<String, Map<TaskOutcome, AggResult>> pluginResults) {
        var jsonCacheReport = new JsonCacheReport(pluginResults, testTaskCacheHelper.getMetrics());
        var dir = new File(session.getExecutionRootDirectory(), "target");
        dir.mkdir();
        MoreFileUtils.write(new File(dir, "surefire-cache-report.json"), JsonSerializers.serialize(jsonCacheReport));
    }

    private record JsonCacheReport(
        Map<String, Map<TaskOutcome, AggResult>> pluginResults,
        CacheServiceMetrics cacheServiceMetrics) {
    }

    private static void logStorageMetrics(CacheServiceMetrics metrics) {
        int readHitOps = metrics.getReadHitOperations();
        long readHitMillis = metrics.getReadHitMillis();
        long readHitBytes = metrics.getReadHitBytes();
        if (readHitOps > 0) {
            logger.info("Cache hit read operations: {}, time: {}, size: {}",
                readHitOps, formatTime(toSeconds(readHitMillis)), formatByteSize(readHitBytes));
        }

        int readMissOps = metrics.getReadMissOperations();
        long readMissMillis = metrics.getReadMissMillis();
        if (readMissOps > 0) {
            logger.info("Cache miss read operations: {}, time: {}",
                readMissOps, formatTime(toSeconds(readMissMillis)));
        }

        int writeOps = metrics.getWriteOperations();
        long writeMillis = metrics.getWriteMillis();
        long writeBytes = metrics.getWriteBytes();
        if (writeOps > 0) {
            logger.info("Cache write operations: {}, time: {}, size: {}",
                writeOps, formatTime(toSeconds(writeMillis)), formatByteSize(writeBytes));
        }

        int readFailures = metrics.getReadFailures();
        if (readFailures != 0) {
            logger.warn("Read failures: {}, then skipped {} operations", readFailures, metrics.getReadSkipped());
        }
        int writeFailures = metrics.getWriteFailures();
        if (writeFailures != 0) {
            logger.warn("Write failures: {}, then skipped {} operations", writeFailures, metrics.getWriteSkipped());
        }
    }
}
