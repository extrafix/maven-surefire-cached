package com.github.seregamorph.maven.test.extension;

import com.github.seregamorph.maven.test.common.GroupArtifactId;
import com.github.seregamorph.maven.test.common.PluginName;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
        ModuleTestResult moduleTestResult
    ) {
        synchronized (executionResults) {
            executionResults.computeIfAbsent(groupArtifactId, k -> new TreeMap<>())
                .computeIfAbsent(pluginName, k -> new ArrayList<>())
                .add(moduleTestResult);
        }
    }

    public Map<GroupArtifactId, List<ModuleTestResult>> getExecutionResults(PluginName pluginName) {
        Map<GroupArtifactId, List<ModuleTestResult>> result = new TreeMap<>();
        synchronized (executionResults) {
            executionResults.forEach((groupArtifactId, pluginResults) -> {
                List<ModuleTestResult> moduleTestResults = pluginResults.get(pluginName);
                if (moduleTestResults != null) {
                    result.put(groupArtifactId, moduleTestResults);
                }
            });
        }
        return result;
    }
}
