package io.github.phlearning.bdd.driver;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.util.Optional;

/**
 * One named browser of a scenario, with its tabs, windows and downloads, and what WebDriver
 * BiDi reports of it: JavaScript errors and failed requests.
 */
public record BrowserSession(
        String name,
        WebDriver driver,
        WindowManager windows,
        Downloads downloads,
        BrowserConsole console,
        BrowserNetwork network) {

    /** WebDriver session id, which also names the session's video on the Grid. */
    public Optional<String> sessionId() {
        return driver instanceof RemoteWebDriver remote && remote.getSessionId() != null
                ? Optional.of(remote.getSessionId().toString())
                : Optional.empty();
    }
}
