package io.github.phlearning.bdd.reporting;

import io.github.phlearning.bdd.config.Config;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.FluentWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Finds the video the Grid recorded for a session. Nodes write {@code <sessionId>.mp4}
 * (SE_VIDEO_FILE_NAME=auto) into {@code video.dir}, mounted from the host by docker compose.
 * The file only appears, and keeps growing, after the session is closed: it is returned
 * once its size stops changing.
 */
public class GridVideos {

    private static final Logger LOG = LoggerFactory.getLogger(GridVideos.class);

    private final Path directory;
    private final Duration timeout;

    public GridVideos(Config config) {
        this.directory = Path.of(config.get("video.dir"));
        this.timeout = Duration.ofSeconds(config.getInt("timeout.video"));
    }

    public Optional<Path> find(String sessionId) {
        long[] lastSize = {-1};
        try {
            return Optional.of(new FluentWait<>(sessionId)
                    .withTimeout(timeout)
                    .pollingEvery(Duration.ofSeconds(1))
                    .until(id -> {
                        Optional<Path> video = videoOf(id);
                        if (video.isEmpty()) {
                            return null;
                        }
                        long size = size(video.get());
                        boolean stable = size > 0 && size == lastSize[0];
                        lastSize[0] = size;
                        return stable ? video.get() : null;
                    }));
        } catch (TimeoutException e) {
            LOG.warn("No video found for session {} in {} after {}s", sessionId, directory, timeout.toSeconds());
            return Optional.empty();
        }
    }

    private Optional<Path> videoOf(String sessionId) {
        if (!Files.isDirectory(directory)) {
            return Optional.empty();
        }
        try (Stream<Path> files = Files.list(directory)) {
            return files.filter(f -> f.getFileName().toString().contains(sessionId))
                    .filter(f -> f.getFileName().toString().endsWith(".mp4"))
                    .findFirst();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static long size(Path file) {
        try {
            return Files.size(file);
        } catch (IOException e) {
            return -1;
        }
    }
}
