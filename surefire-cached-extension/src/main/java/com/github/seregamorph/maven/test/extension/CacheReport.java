package com.github.seregamorph.maven.test.extension;

import static java.util.Collections.emptyList;

import com.github.seregamorph.maven.test.common.GroupArtifactId;
import com.github.seregamorph.maven.test.common.PluginName;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

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

    public List<ModuleTestResult> getExecutionResults(PluginName pluginName) {
        synchronized (executionResults) {
            return executionResults.values().stream()
                .flatMap(m -> m.getOrDefault(pluginName, emptyList()).stream())
                .collect(Collectors.toList());
        }
    }

}
