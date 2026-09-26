package io.github.phlearning.bdd.driver;

import io.github.phlearning.bdd.config.Config;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Owns the browsers of one scenario. PicoContainer creates one instance per scenario,
 * so parallel scenarios never share a driver.
 * <p>
 * A scenario usually drives a single browser, {@link #DEFAULT_SESSION}. It can drive
 * several at once (two users interacting, an admin and a customer...) by switching the
 * active session with {@link #use(String)}; page objects always work on the active one.
 * Browsers are started on first use: scenarios that never touch the UI never pay for one.
 */
public class DriverManager {

    public static final String DEFAULT_SESSION = "principal";

    private static final Logger LOG = LoggerFactory.getLogger(DriverManager.class);

    private final Config config = Config.get();
    private final Map<String, BrowserSession> sessions = new LinkedHashMap<>();
    private String active = DEFAULT_SESSION;

    /** Driver of the active session, started if needed. */
    public WebDriver getDriver() {
        return session().driver();
    }

    /** The active session, started if needed. */
    public BrowserSession session() {
        return sessions.computeIfAbsent(active, this::start);
    }

    /** Makes {@code name} the active session, starting its browser if needed. */
    public BrowserSession use(String name) {
        if (!name.equals(active)) {
            LOG.info("Active browser: '{}'", name);
        }
        active = name;
        return session();
    }

    /** Sessions whose browser has been started, without starting any. */
    public Collection<BrowserSession> startedSessions() {
        return List.copyOf(sessions.values());
    }

    public void quit() {
        for (BrowserSession session : sessions.values()) {
            try {
                session.driver().quit();
            } catch (RuntimeException e) {
                LOG.warn("Could not quit browser '{}': {}", session.name(), e.getMessage());
            }
        }
        sessions.clear();
        active = DEFAULT_SESSION;
    }

    private BrowserSession start(String name) {
        LOG.info("Starting browser '{}'", name);
        WebDriver driver = DriverFactory.create(config);
        Duration timeout = Duration.ofSeconds(config.getInt("timeout.explicit"));
        return new BrowserSession(name, driver, new WindowManager(driver, timeout));
    }
}
