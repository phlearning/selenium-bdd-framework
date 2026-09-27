package io.github.phlearning.bdd.driver;

import io.github.phlearning.bdd.config.Config;
import org.openqa.selenium.NoSuchWindowException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WindowType;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Tabs and windows of one browser, addressed by alias instead of raw window handles.
 * <p>
 * The window that was open when the browser started is registered as {@link #MAIN}.
 * Tabs and windows opened by the test are aliased when opened; windows opened by the
 * application (a link with {@code target="_blank"}, a popup) are caught with
 * {@link #openedBy(String, Runnable)}, which waits for the new handle to appear.
 */
public class WindowManager {

    public static final String MAIN = "principale";

    private static final Logger LOG = LoggerFactory.getLogger(WindowManager.class);

    private final WebDriver driver;
    private final WebDriverWait wait;
    private final Map<String, String> handlesByAlias = new LinkedHashMap<>();
    private final DemoMode demo = DemoMode.from(Config.get());

    public WindowManager(WebDriver driver, Duration timeout) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, timeout);
        handlesByAlias.put(MAIN, driver.getWindowHandle());
    }

    /**
     * Runs an action that makes the application open a new tab or window, waits for it,
     * switches to it and registers it under {@code alias}.
     */
    public void openedBy(String alias, Runnable action) {
        Set<String> before = new HashSet<>(driver.getWindowHandles());
        action.run();
        String handle = wait.withMessage("no new window opened for '" + alias + "'")
                .until(d -> {
                    Set<String> now = new HashSet<>(d.getWindowHandles());
                    now.removeAll(before);
                    return now.isEmpty() ? null : now.iterator().next();
                });
        register(alias, handle);
        driver.switchTo().window(handle);
        LOG.info("Window '{}' opened by the application, switched to it", alias);
        demo.pause(driver);
    }

    public void openTab(String alias, String url) {
        open(alias, url, WindowType.TAB);
    }

    public void openWindow(String alias, String url) {
        open(alias, url, WindowType.WINDOW);
    }

    private void open(String alias, String url, WindowType type) {
        driver.switchTo().newWindow(type);
        register(alias, driver.getWindowHandle());
        LOG.info("New {} '{}' opened on {}", type.name().toLowerCase(), alias, url);
        driver.get(url);
        demo.pause(driver);
    }

    public void switchTo(String alias) {
        driver.switchTo().window(handleOf(alias));
        LOG.info("Switched to window '{}'", alias);
        demo.pause(driver);
    }

    /** Switches to the first window whose document title equals {@code title}, waiting for it to exist. */
    public void switchToTitle(String title) {
        String current = driver.getWindowHandle();
        wait.withMessage("no window titled '" + title + "'").until(d -> {
            for (String handle : d.getWindowHandles()) {
                if (title.equals(d.switchTo().window(handle).getTitle())) {
                    return true;
                }
            }
            d.switchTo().window(current);
            return false;
        });
        LOG.info("Switched to window titled '{}'", title);
        demo.pause(driver);
    }

    /** Closes the window {@code alias} and switches back to {@link #MAIN}. */
    public void close(String alias) {
        switchTo(alias);
        driver.close();
        handlesByAlias.remove(alias);
        LOG.info("Window '{}' closed", alias);
        switchTo(MAIN);
    }

    public int count() {
        return driver.getWindowHandles().size();
    }

    /** Alias of the active window, if it has one. */
    public Optional<String> currentAlias() {
        String handle = driver.getWindowHandle();
        return handlesByAlias.entrySet().stream()
                .filter(entry -> entry.getValue().equals(handle))
                .map(Map.Entry::getKey)
                .findFirst();
    }

    private void register(String alias, String handle) {
        if (handlesByAlias.containsKey(alias)) {
            throw new IllegalArgumentException("Window alias already in use: " + alias);
        }
        handlesByAlias.put(alias, handle);
    }

    private String handleOf(String alias) {
        String handle = handlesByAlias.get(alias);
        if (handle == null) {
            throw new NoSuchWindowException(
                    "Unknown window alias '%s', known: %s".formatted(alias, handlesByAlias.keySet()));
        }
        return handle;
    }
}
