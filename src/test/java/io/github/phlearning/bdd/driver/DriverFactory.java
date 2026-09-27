package io.github.phlearning.bdd.driver;

import io.github.phlearning.bdd.config.Config;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.LocalFileDetector;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Builds a configured {@link WebDriver}, either on this machine ({@code execution=local}:
 * drivers and, when missing, browsers are resolved by Selenium Manager) or on a Selenium
 * Grid ({@code execution=grid}).
 */
public final class DriverFactory {

    private static final Logger LOG = LoggerFactory.getLogger(DriverFactory.class);

    private DriverFactory() {}

    /**
     * @param downloadDir where a local browser saves downloads; ignored on the Grid,
     *                    where downloads stay on the node and are fetched through the Grid API
     */
    public static WebDriver create(Config config, Path downloadDir) {
        BrowserType browser = BrowserType.from(config.get("browser"));
        boolean headless = config.getBoolean("headless");
        boolean grid = isGrid(config);
        LOG.info("Starting {} (headless={}, execution={})", browser, headless, grid ? "grid" : "local");

        // On the Grid the node picks its own download folder (managed downloads).
        Optional<Path> localDownloads = grid ? Optional.empty() : Optional.of(downloadDir);
        MutableCapabilities options = switch (browser) {
            case CHROME -> chromeOptions(headless, localDownloads);
            case FIREFOX -> firefoxOptions(headless, localDownloads);
        };

        WebDriver driver = grid ? remote(config, options, headless) : local(browser, options);

        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(config.getInt("timeout.page.load")));
        // Implicit waits stay at 0 on purpose: all synchronisation goes through explicit waits.
        driver.manage().timeouts().implicitlyWait(Duration.ZERO);
        driver.manage().window().setSize(new Dimension(config.getInt("window.width"), config.getInt("window.height")));
        return driver;
    }

    public static boolean isGrid(Config config) {
        return "grid".equalsIgnoreCase(config.get("execution"));
    }

    private static WebDriver local(BrowserType browser, MutableCapabilities options) {
        return switch (browser) {
            case CHROME -> new ChromeDriver((ChromeOptions) options);
            case FIREFOX -> new FirefoxDriver((FirefoxOptions) options);
        };
    }

    private static WebDriver remote(Config config, MutableCapabilities options, boolean headless) {
        // Files downloaded by the browser stay on the node: the Grid exposes them through its API.
        options.setCapability("se:downloadsEnabled", true);
        if (config.getBoolean("video")) {
            if (headless) {
                LOG.warn("video=true is ignored with headless browsers: nothing is displayed to record");
            } else {
                options.setCapability("se:recordVideo", true);
            }
        }
        RemoteWebDriver driver = new RemoteWebDriver(gridUrl(config), options);
        // Files to upload live on this machine: send them to the node before typing their path.
        driver.setFileDetector(new LocalFileDetector());
        LOG.info("Grid session {}", driver.getSessionId());
        return driver;
    }

    private static URL gridUrl(Config config) {
        try {
            return new URI(config.get("grid.url")).toURL();
        } catch (URISyntaxException | MalformedURLException e) {
            throw new IllegalArgumentException("Invalid grid.url: " + config.get("grid.url"), e);
        }
    }

    private static ChromeOptions chromeOptions(boolean headless, Optional<Path> downloadDir) {
        ChromeOptions options = new ChromeOptions();
        if (headless) {
            options.addArguments("--headless=new");
        }
        options.addArguments("--disable-dev-shm-usage", "--no-sandbox", "--disable-search-engine-choice-screen");
        Map<String, Object> prefs = new HashMap<>(Map.of(
                // Keep Chrome's password manager from opening "change your password" popups on demo sites.
                "credentials_enable_service", false,
                "profile.password_manager_enabled", false,
                "profile.password_manager_leak_detection", false));
        downloadDir.ifPresent(dir -> {
            // Save downloads without asking.
            prefs.put("download.default_directory", dir.toAbsolutePath().toString());
            prefs.put("download.prompt_for_download", false);
        });
        options.setExperimentalOption("prefs", prefs);
        return options;
    }

    private static FirefoxOptions firefoxOptions(boolean headless, Optional<Path> downloadDir) {
        FirefoxOptions options = new FirefoxOptions();
        if (headless) {
            options.addArguments("-headless");
        }
        downloadDir.ifPresent(dir -> {
            // Save downloads without asking.
            options.addPreference("browser.download.folderList", 2);
            options.addPreference("browser.download.dir", dir.toAbsolutePath().toString());
            options.addPreference("browser.download.useDownloadDir", true);
            options.addPreference("browser.helperApps.neverAsk.saveToDisk", "text/plain,application/octet-stream");
        });
        return options;
    }
}
