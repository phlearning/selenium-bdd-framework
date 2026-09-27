package io.github.phlearning.bdd.hooks;

import io.cucumber.java.After;
import io.cucumber.java.Scenario;
import io.github.phlearning.bdd.config.Config;
import io.github.phlearning.bdd.driver.BrowserSession;
import io.github.phlearning.bdd.driver.DriverFactory;
import io.github.phlearning.bdd.driver.DriverManager;
import io.github.phlearning.bdd.reporting.GridVideos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public class DriverHooks {

    private static final Logger LOG = LoggerFactory.getLogger(DriverHooks.class);

    private final DriverManager driverManager;
    private final Config config = Config.get();

    public DriverHooks(DriverManager driverManager) {
        this.driverManager = driverManager;
    }

    /**
     * Lowest order runs last: other {@code @After} hooks (screenshots, logs) still see an open
     * browser. Grid videos are only finalised once the browser has quit, so they are attached here.
     */
    @After(order = 0)
    public void quitBrowsers(Scenario scenario) {
        // Session ids name the videos, and quitting a RemoteWebDriver forgets its id: read them first.
        Map<String, String> sessionIdsByBrowser = new LinkedHashMap<>();
        for (BrowserSession session : driverManager.startedSessions()) {
            session.sessionId().ifPresent(id -> sessionIdsByBrowser.put(session.name(), id));
        }
        driverManager.quit();
        if (scenario.isFailed() && DriverFactory.isGrid(config) && config.getBoolean("video")) {
            attachVideos(scenario, sessionIdsByBrowser);
        }
    }

    private void attachVideos(Scenario scenario, Map<String, String> sessionIdsByBrowser) {
        GridVideos videos = new GridVideos(config);
        boolean several = sessionIdsByBrowser.size() > 1;
        sessionIdsByBrowser.forEach((browser, sessionId) -> videos.find(sessionId)
                .ifPresent(file -> {
                    LOG.info("Attaching video {}", file);
                    scenario.attach(read(file), "video/mp4", "Video" + (several ? " (" + browser + ")" : ""));
                }));
    }

    private static byte[] read(Path file) {
        try {
            return Files.readAllBytes(file);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
