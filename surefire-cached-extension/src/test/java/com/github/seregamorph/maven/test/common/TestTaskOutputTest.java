package com.github.seregamorph.maven.test.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.github.seregamorph.maven.test.util.JsonSerializers;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.Test;

class TestTaskOutputTest {

    @Test
    public void shouldSerializeAndDeserialize() {
        var testTestOutput = new TestTaskOutput(
            Instant.now().minus(Duration.ofSeconds(10)),
            Instant.now(),
            new BigDecimal("1.1"),
            10,
            new BigDecimal("1.0"),
            100,
            0,
            0,
            0,
            0,
            Map.of(
                "jacoco",
                new OutputArtifact(
                    "jacoco.exec",
                    1,
                    100,
                    10
                )
            )
        );
        var content = JsonSerializers.serialize(testTestOutput);
        var restored = JsonSerializers.deserialize(content, TestTaskOutput.class, "surefire-cached-output.json");
        assertEquals(testTestOutput.getStartTime(), restored.getStartTime());
        assertEquals(testTestOutput.getEndTime(), restored.getEndTime());
        assertEquals(testTestOutput.getTotalTimeSeconds(), restored.getTotalTimeSeconds());
        assertEquals(testTestOutput.getTotalClasses(), restored.getTotalClasses());
        assertEquals(testTestOutput.getTotalTestTimeSeconds(), restored.getTotalTestTimeSeconds());
        assertEquals(testTestOutput.getTotalTests(), restored.getTotalTests());
        assertEquals(testTestOutput.getTotalErrors(), restored.getTotalErrors());
        assertEquals(testTestOutput.getTotalFailures(), restored.getTotalFailures());
        assertEquals(testTestOutput.getArtifacts().get("jacoco").getFileName(),
            restored.getArtifacts().get("jacoco").getFileName());
        assertEquals(testTestOutput.getArtifacts().get("jacoco").getFiles(),
            restored.getArtifacts().get("jacoco").getFiles());
        assertEquals(testTestOutput.getArtifacts().get("jacoco").getPackedSize(),
            restored.getArtifacts().get("jacoco").getPackedSize());
        assertEquals(testTestOutput.getArtifacts().get("jacoco").getUnpackedSize(),
            restored.getArtifacts().get("jacoco").getUnpackedSize());
    }

    @Test
    public void shouldDeserializePrevVersion() throws IOException {
        var content = Files.readAllBytes(getResourceFile("surefire-cached-output.json").toPath());
        var restored = JsonSerializers.deserialize(content, TestTaskOutput.class, "surefire-cached-output.json");
        assertEquals(Instant.parse("2025-06-30T17:10:34.721504Z"), restored.getStartTime());
        assertEquals(Instant.parse("2025-06-30T17:10:44.722564Z"), restored.getEndTime());
        assertEquals(new BigDecimal("1.1"), restored.getTotalTimeSeconds());
        assertEquals(10, restored.getTotalClasses());
        assertEquals(new BigDecimal("1.0"), restored.getTotalTestTimeSeconds());
        assertEquals(100, restored.getTotalTests());
        assertEquals(0, restored.getTotalErrors());
        assertEquals(0, restored.getTotalFailures());
        assertEquals("jacoco.exec",
            restored.getArtifacts().get("jacoco").getFileName());
        assertEquals(1,
            restored.getArtifacts().get("jacoco").getFiles());
        assertEquals(10L,
            restored.getArtifacts().get("jacoco").getPackedSize());
        assertEquals(100L,
            restored.getArtifacts().get("jacoco").getUnpackedSize());
    }

    private static File getResourceFile(String name) {
        var resource = TestTaskOutputTest.class.getClassLoader().getResource(name);
        assertNotNull(resource, "Resource not found: " + name);
        return new File(resource.getFile());
    }
}
