package io.github.phlearning.bdd.driver;

import io.github.phlearning.bdd.config.Config;
import org.openqa.selenium.WebDriver;

import java.util.Optional;

/**
 * Owns the browser of one scenario. PicoContainer creates one instance per scenario,
 * so parallel scenarios never share a driver. The browser is only started on first
 * use: scenarios that never touch the UI never pay for a browser start.
 */
public class DriverManager {

    private final Config config = Config.get();
    private WebDriver driver;

    public WebDriver getDriver() {
        if (driver == null) {
            driver = DriverFactory.create(config);
        }
        return driver;
    }

    /** The driver if it has been started, without starting one. */
    public Optional<WebDriver> current() {
        return Optional.ofNullable(driver);
    }

    public void quit() {
        if (driver != null) {
            try {
                driver.quit();
            } finally {
                driver = null;
            }
        }
    }
}
