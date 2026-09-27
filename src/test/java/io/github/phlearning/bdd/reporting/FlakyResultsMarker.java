package io.github.phlearning.bdd.reporting;

import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestPlan;
import org.openqa.selenium.json.Json;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 * After the rerun pass, flags as flaky every scenario that failed first and then passed.
 * <p>
 * Allure groups the attempts of a scenario by {@code historyId} but only shows it as flaky
 * when the result says so, and the Cucumber plugin rewrites the status details at the end
 * of each scenario. Patching the result files once all attempts are written is the only
 * reliable place to do it. Registered through {@code META-INF/services}; active only in the
 * rerun pass ({@code -Drerun.pass=true}, set by the pom).
 */
public class FlakyResultsMarker implements TestExecutionListener {

    private static final Logger LOG = LoggerFactory.getLogger(FlakyResultsMarker.class);
    private static final Set<String> FAILED_STATUSES = Set.of("failed", "broken");

    private final Json json = new Json();

    @Override
    public void testPlanExecutionFinished(TestPlan testPlan) {
        if (!Boolean.getBoolean("rerun.pass")) {
            return;
        }
        Path results = Path.of(System.getProperty("allure.results.directory", "target/allure-results"));
        if (Files.isDirectory(results)) {
            markFlaky(results);
        }
    }

    void markFlaky(Path results) {
        Map<String, List<Attempt>> attemptsByHistoryId = new HashMap<>();
        for (Path file : resultFiles(results)) {
            Map<String, Object> result = read(file);
            Object historyId = result.get("historyId");
            if (historyId != null) {
                attemptsByHistoryId
                        .computeIfAbsent(historyId.toString(), id -> new ArrayList<>())
                        .add(new Attempt(file, result));
            }
        }

        attemptsByHistoryId.values().stream()
                .filter(attempts -> attempts.stream().anyMatch(a -> FAILED_STATUSES.contains(a.status())))
                .flatMap(attempts -> attempts.stream().filter(a -> "passed".equals(a.status())))
                .forEach(this::flagFlaky);
    }

    @SuppressWarnings("unchecked")
    private void flagFlaky(Attempt attempt) {
        Map<String, Object> details =
                new LinkedHashMap<>((Map<String, Object>) attempt.result().getOrDefault("statusDetails", Map.of()));
        details.put("flaky", true);
        Map<String, Object> patched = new LinkedHashMap<>(attempt.result());
        patched.put("statusDetails", details);
        try {
            Files.writeString(attempt.file(), json.toJson(patched));
            LOG.warn("Flaky scenario (passed on rerun): {}", patched.get("name"));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static List<Path> resultFiles(Path results) {
        try (Stream<Path> files = Files.list(results)) {
            return files.filter(f -> f.getFileName().toString().endsWith("-result.json"))
                    .toList();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Map<String, Object> read(Path file) {
        try {
            return json.toType(Files.readString(file), Json.MAP_TYPE);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private record Attempt(Path file, Map<String, Object> result) {
        String status() {
            return String.valueOf(result.get("status"));
        }
    }
}
