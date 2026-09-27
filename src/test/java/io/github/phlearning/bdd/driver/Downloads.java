package io.github.phlearning.bdd.driver;

import org.openqa.selenium.HasDownloads;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.FluentWait;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

/**
 * Files downloaded by one browser, wherever it runs.
 * <ul>
 *     <li>local browser: saved straight into {@link #directory()}</li>
 *     <li>Grid browser: saved on the node, then copied into {@link #directory()} through
 *     the Grid's managed downloads API</li>
 * </ul>
 */
public class Downloads {

    private final WebDriver driver;
    private final Path directory;
    private final boolean grid;
    private final Duration timeout;

    public Downloads(WebDriver driver, Path directory, boolean grid, Duration timeout) {
        this.driver = driver;
        this.directory = directory;
        this.grid = grid;
        this.timeout = timeout;
    }

    public Path directory() {
        return directory;
    }

    /** Waits for {@code fileName} to be completely downloaded and returns it as a local file. */
    public Path waitFor(String fileName) {
        FluentWait<Downloads> wait = new FluentWait<>(this)
                .withTimeout(timeout)
                .pollingEvery(Duration.ofMillis(250))
                .withMessage("file '" + fileName + "' not downloaded");
        if (grid) {
            HasDownloads downloads = (HasDownloads) driver;
            wait.until(d -> downloads.getDownloadedFiles().stream()
                    .anyMatch(f -> f.getName().equals(fileName)));
            try {
                downloads.downloadFile(fileName, directory);
            } catch (IOException e) {
                throw new UncheckedIOException("Cannot fetch '" + fileName + "' from the Grid", e);
            }
            return directory.resolve(fileName);
        }
        Path file = directory.resolve(fileName);
        // Browsers write to a temporary file (.crdownload, .part) and rename it once complete.
        wait.until(d -> Files.exists(file)
                && !Files.exists(directory.resolve(fileName + ".crdownload"))
                && !Files.exists(directory.resolve(fileName + ".part")));
        return file;
    }
}
