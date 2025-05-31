package com.github.seregamorph.maven.test.extension;

import com.github.seregamorph.maven.test.common.GroupArtifactId;
import com.github.seregamorph.maven.test.common.PluginName;
import com.github.seregamorph.maven.test.core.TaskOutcome;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author Sergey Chernov
 */
public class CacheReport {

    /**
     * There can be more than one execution per module and plugin.
     */
    private final SortedMap<GroupArtifactId, SortedMap<PluginName, List<ModuleTestResult>>> executionResults;

    public CacheReport() {
        executionResults = new TreeMap<>();
    }

    public void addExecutionResult(
            GroupArtifactId groupArtifactId,
            PluginName pluginName,
            TaskOutcome result,
            BigDecimal totalTimeSeconds,
            int deletedCacheEntries
    ) {
        synchronized (executionResults) {
            executionResults.computeIfAbsent(groupArtifactId, k -> new TreeMap<>())
                    .computeIfAbsent(pluginName, k -> new ArrayList<>())
                    .add(new ModuleTestResult(result, totalTimeSeconds, deletedCacheEntries));
        }
    }

    public List<ModuleTestResult> getExecutionResults(PluginName pluginName) {
        synchronized (executionResults) {
            return executionResults.values().stream()
                    .flatMap(m -> m.getOrDefault(pluginName, List.of()).stream())
                    .toList();
        }
    }

    public record ModuleTestResult(TaskOutcome result, BigDecimal totalTimeSeconds, int deletedCacheEntries) {

        @Override
        public String toString() {
            return "ModuleTestResult{" +
                    "result=" + result +
                    ", totalTimeSeconds=" + totalTimeSeconds +
                    '}';
        }
    }
}
