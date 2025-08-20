package com.github.seregamorph.maven.test.extension;

import com.github.seregamorph.maven.test.common.GroupArtifactId;
import com.github.seregamorph.maven.test.core.TaskOutcome;
import java.math.BigDecimal;

/**
 * @author Sergey Chernov
 */
public final class ModuleTestResult {

    private final GroupArtifactId groupArtifactId;
    private final TaskOutcome result;
    private final BigDecimal totalTimeSeconds;
    private final int deletedCacheEntries;

    public ModuleTestResult(
        GroupArtifactId groupArtifactId,
        TaskOutcome result,
        BigDecimal totalTimeSeconds,
        int deletedCacheEntries
    ) {
        this.groupArtifactId = groupArtifactId;
        this.result = result;
        this.totalTimeSeconds = totalTimeSeconds;
        this.deletedCacheEntries = deletedCacheEntries;
    }

    public GroupArtifactId getGroupArtifactId() {
        return groupArtifactId;
    }

    public TaskOutcome getResult() {
        return result;
    }

    public BigDecimal getTotalTimeSeconds() {
        return totalTimeSeconds;
    }

    public int getDeletedCacheEntries() {
        return deletedCacheEntries;
    }

    @Override
    public String toString() {
        return "ModuleTestResult{" +
            "groupArtifactId=" + groupArtifactId +
            ", result=" + result +
            ", totalTimeSeconds=" + totalTimeSeconds +
            ", deletedCacheEntries=" + deletedCacheEntries +
            '}';
    }
}
