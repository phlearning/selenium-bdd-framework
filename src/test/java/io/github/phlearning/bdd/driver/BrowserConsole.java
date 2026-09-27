package io.github.phlearning.bdd.driver;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.bidi.HasBiDi;
import org.openqa.selenium.bidi.log.LogLevel;
import org.openqa.selenium.bidi.module.LogInspector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * JavaScript errors of one browser, collected through WebDriver BiDi from the moment the
 * browser starts, in every tab and window: uncaught exceptions and {@code console.error} calls.
 * <p>
 * Events arrive asynchronously: a check right after an action should wait for them
 * (see {@code BrowserSteps}).
 */
public final class BrowserConsole {

    private static final Logger LOG = LoggerFactory.getLogger(BrowserConsole.class);

    private final boolean enabled;
    private final List<String> ignored;
    private final List<String> errors = new CopyOnWriteArrayList<>();

    private BrowserConsole(boolean enabled, List<String> ignored) {
        this.enabled = enabled;
        this.ignored = ignored;
    }

    /**
     * Starts listening, or returns a disabled console when the driver has no BiDi connection.
     *
     * @param ignored errors containing one of these texts are known noise (third-party
     *                scripts...) and are not recorded
     */
    static BrowserConsole listen(WebDriver driver, List<String> ignored) {
        if (!(driver instanceof HasBiDi)) {
            return new BrowserConsole(false, ignored);
        }
        BrowserConsole console = new BrowserConsole(true, ignored);
        // The inspector lives as long as the browser: quitting closes its connection.
        LogInspector inspector = new LogInspector(driver);
        inspector.onJavaScriptException(entry -> console.add("Exception : " + entry.getText()));
        inspector.onConsoleEntry(entry -> {
            if (entry.getLevel() == LogLevel.ERROR) {
                console.add("console.error : " + entry.getText());
            }
        });
        return console;
    }

    private void add(String error) {
        if (ignored.stream().anyMatch(error::contains)) {
            LOG.debug("Browser console, ignored: {}", error);
            return;
        }
        LOG.debug("Browser console: {}", error);
        errors.add(error);
    }

    public boolean enabled() {
        return enabled;
    }

    /** Errors received so far, oldest first. */
    public List<String> errors() {
        return List.copyOf(errors);
    }
}
