package io.github.phlearning.bdd.driver;

import io.github.phlearning.bdd.config.Config;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.time.Duration;

/**
 * Demo mode ({@code -Ddemo=true}): outlines each element before it is used and pauses after
 * each step, so that a person watching the browser (or its Grid video) can follow the
 * scenario. Every method is a no-op when the mode is off.
 */
public final class DemoMode {

    private static final DemoMode OFF = new DemoMode(Duration.ZERO);

    private final Duration delay;

    private DemoMode(Duration delay) {
        this.delay = delay;
    }

    public static DemoMode from(Config config) {
        return config.getBoolean("demo") ? new DemoMode(Duration.ofMillis(config.getInt("demo.delay"))) : OFF;
    }

    public boolean isOn() {
        return !delay.isZero();
    }

    public void highlight(WebDriver driver, WebElement element) {
        if (isOn()) {
            ((JavascriptExecutor) driver)
                    .executeScript(
                            "arguments[0].style.outline = '3px solid #e5383b';"
                                    + "arguments[0].style.outlineOffset = '2px';",
                            element);
            pause(driver);
        }
    }

    /**
     * Waits in the browser (not a thread sleep): drivers skip a W3C pause action sent alone,
     * so the pause is a {@code setTimeout} run by an asynchronous script.
     */
    public void pause(WebDriver driver) {
        if (isOn() && !alertOpen(driver)) {
            ((JavascriptExecutor) driver)
                    .executeAsyncScript("setTimeout(arguments[arguments.length - 1], arguments[0]);", delay.toMillis());
        }
    }

    /** A script would close an open JavaScript dialog (unhandled prompt behaviour): skip the pause. */
    private static boolean alertOpen(WebDriver driver) {
        try {
            driver.switchTo().alert();
            return true;
        } catch (NoAlertPresentException e) {
            return false;
        }
    }
}
