package io.github.phlearning.bdd.hooks;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.github.phlearning.bdd.driver.BrowserSession;
import io.github.phlearning.bdd.driver.DriverManager;
import io.github.phlearning.bdd.logging.ScenarioLogAppender;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.nio.charset.StandardCharsets;

/**
 * Evidence collection around each scenario. {@code @After} hooks run from the highest
 * order to the lowest, so the evidence is gathered before {@link DriverHooks} closes the browser.
 */
public class ScenarioHooks {

    private static final Logger LOG = LoggerFactory.getLogger(ScenarioHooks.class);

    private final DriverManager driverManager;

    public ScenarioHooks(DriverManager driverManager) {
        this.driverManager = driverManager;
    }

    @Before(order = 0)
    public void startScenario(Scenario scenario) {
        MDC.put("scenario", scenario.getName());
        ScenarioLogAppender.startCapture();
        LOG.info("Scenario started {} {}", scenario.getUri(), scenario.getSourceTagNames());
    }

    @After(order = 200)
    public void captureFailureEvidence(Scenario scenario) {
        if (!scenario.isFailed()) {
            return;
        }
        var sessions = driverManager.startedSessions();
        // With a single browser the attachments keep short names; with several, each is suffixed with its browser.
        boolean several = sessions.size() > 1;
        for (BrowserSession session : sessions) {
            attachBrowserState(scenario, session.driver(), several ? " (" + session.name() + ")" : "");
        }
    }

    @After(order = 100)
    public void attachLogs(Scenario scenario) {
        LOG.info("Scenario finished with status {}", scenario.getStatus());
        String logs = ScenarioLogAppender.stopCapture();
        if (!logs.isEmpty()) {
            scenario.attach(logs.getBytes(StandardCharsets.UTF_8), "text/plain", "Logs");
        }
        MDC.remove("scenario");
    }

    private static void attachBrowserState(Scenario scenario, WebDriver driver, String suffix) {
        // Each piece is collected independently: a crashed browser may still give some of them.
        try {
            scenario.attach(driver.getCurrentUrl().getBytes(StandardCharsets.UTF_8), "text/uri-list", "URL" + suffix);
        } catch (WebDriverException e) {
            LOG.warn("Could not read the current URL: {}", e.getMessage());
        }
        try {
            byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            scenario.attach(screenshot, "image/png", "Screenshot" + suffix);
        } catch (WebDriverException e) {
            LOG.warn("Could not take a screenshot: {}", e.getMessage());
        }
        try {
            scenario.attach(
                    driver.getPageSource().getBytes(StandardCharsets.UTF_8), "text/html", "Page source" + suffix);
        } catch (WebDriverException e) {
            LOG.warn("Could not read the page source: {}", e.getMessage());
        }
    }
}
