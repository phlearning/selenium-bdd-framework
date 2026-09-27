package io.github.phlearning.bdd.visual;

import io.github.phlearning.bdd.config.Config;
import io.qameta.allure.Allure;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import javax.imageio.ImageIO;

/**
 * Visual regression: compares a screenshot with a reference image (baseline) kept in the
 * repository, one per browser: {@code <visual.baselines.dir>/<browser>/<name>.png}.
 * <p>
 * Baselines are taken on the Selenium Grid, whose browsers, fonts and screen size are the
 * same everywhere. {@code -Dvisual.update=true} (re)writes the baselines instead of comparing.
 * On a difference, the report shows the reference, the screenshot and the differences
 * (Allure "screen diff").
 */
public final class VisualCheck {

    private static final Logger LOG = LoggerFactory.getLogger(VisualCheck.class);

    private final WebDriver driver;
    private final Path baselines;
    private final Path newScreenshots;
    private final boolean update;
    private final int tolerance;
    private final double maxDifferingRatio;

    public VisualCheck(WebDriver driver, Config config) {
        this.driver = driver;
        this.baselines = Path.of(config.get("visual.baselines.dir"), config.get("browser"));
        this.newScreenshots = Path.of(config.get("visual.actual.dir"), config.get("browser"));
        this.update = config.getBoolean("visual.update");
        this.tolerance = config.getInt("visual.tolerance");
        this.maxDifferingRatio = Double.parseDouble(config.get("visual.max.differing.ratio"));
    }

    /**
     * Compares the visible part of the page with the baseline {@code name}.
     *
     * @throws AssertionError when the page looks different, or when there is no baseline yet
     */
    public void matches(String name) {
        byte[] png = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
        Path baseline = baselines.resolve(name + ".png");
        if (update) {
            write(baseline, png);
            LOG.warn("Visual baseline written: {}", baseline);
            return;
        }
        if (!Files.exists(baseline)) {
            Path actual = write(newScreenshots.resolve(name + ".png"), png);
            Allure.addAttachment("Screenshot (no baseline)", "image/png", new ByteArrayInputStream(png), ".png");
            throw new AssertionError("No visual baseline " + baseline + ": check the screenshot " + actual
                    + ", then create the baseline with -Dvisual.update=true");
        }
        byte[] expectedPng = read(baseline);
        ImageComparison.Result result = ImageComparison.compare(image(expectedPng), image(png), tolerance);
        if (!result.sameSize()) {
            attachDiff(expectedPng, png, png);
            throw new AssertionError("Page size differs from the visual baseline " + baseline);
        }
        LOG.info(
                "Visual check '{}': {} differing pixels ({})",
                name,
                result.differingPixels(),
                "%.4f %%".formatted(result.differingRatio() * 100));
        if (result.differingRatio() > maxDifferingRatio) {
            write(newScreenshots.resolve(name + ".png"), png);
            attachDiff(expectedPng, png, encode(result.diff()));
            throw new AssertionError(
                    "Page looks different from the visual baseline %s: %.3f %% of pixels differ (max %.3f %%)"
                            .formatted(baseline, result.differingRatio() * 100, maxDifferingRatio * 100));
        }
    }

    /** Allure screen diff: reference, screenshot and differences side by side in the report. */
    private static void attachDiff(byte[] expected, byte[] actual, byte[] diff) {
        Allure.label("testType", "screenshotDiff");
        String json = "{\"expected\":\"%s\",\"actual\":\"%s\",\"diff\":\"%s\"}"
                .formatted(dataUrl(expected), dataUrl(actual), dataUrl(diff));
        Allure.addAttachment("Visual differences", "application/vnd.allure.image.diff", json, ".imagediff");
    }

    private static String dataUrl(byte[] png) {
        return "data:image/png;base64," + Base64.getEncoder().encodeToString(png);
    }

    private static BufferedImage image(byte[] png) {
        try {
            return ImageIO.read(new ByteArrayInputStream(png));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static byte[] encode(BufferedImage image) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static byte[] read(Path file) {
        try {
            return Files.readAllBytes(file);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static Path write(Path file, byte[] content) {
        try {
            Files.createDirectories(file.getParent());
            return Files.write(file, content);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
