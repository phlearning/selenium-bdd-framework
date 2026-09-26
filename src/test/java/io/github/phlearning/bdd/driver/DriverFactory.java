package io.github.phlearning.bdd.driver;

import io.github.phlearning.bdd.config.Config;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Map;

/**
 * Builds a configured {@link WebDriver}. Drivers and, when missing, browsers are
 * resolved by Selenium Manager, so nothing has to be installed by hand.
 */
public final class DriverFactory {

    private static final Logger LOG = LoggerFactory.getLogger(DriverFactory.class);

    private DriverFactory() {
    }

    public static WebDriver create(Config config) {
        BrowserType browser = BrowserType.from(config.get("browser"));
        boolean headless = config.getBoolean("headless");
        LOG.info("Starting {} (headless={})", browser, headless);

        WebDriver driver = switch (browser) {
            case CHROME -> new ChromeDriver(chromeOptions(headless));
            case FIREFOX -> new FirefoxDriver(firefoxOptions(headless));
        };

        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(config.getInt("timeout.page.load")));
        // Implicit waits stay at 0 on purpose: all synchronisation goes through explicit waits.
        driver.manage().timeouts().implicitlyWait(Duration.ZERO);
        driver.manage().window().setSize(new Dimension(config.getInt("window.width"), config.getInt("window.height")));
        return driver;
    }

    private static ChromeOptions chromeOptions(boolean headless) {
        ChromeOptions options = new ChromeOptions();
        if (headless) {
            options.addArguments("--headless=new");
        }
        options.addArguments("--disable-dev-shm-usage", "--no-sandbox", "--disable-search-engine-choice-screen");
        // Keep Chrome's password manager from opening "change your password" popups on demo sites.
        options.setExperimentalOption("prefs", Map.of(
                "credentials_enable_service", false,
                "profile.password_manager_enabled", false,
                "profile.password_manager_leak_detection", false));
        return options;
    }

    private static FirefoxOptions firefoxOptions(boolean headless) {
        FirefoxOptions options = new FirefoxOptions();
        if (headless) {
            options.addArguments("-headless");
        }
        return options;
    }
}
