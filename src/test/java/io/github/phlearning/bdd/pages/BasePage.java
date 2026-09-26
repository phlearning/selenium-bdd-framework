package io.github.phlearning.bdd.pages;

import io.github.phlearning.bdd.config.Config;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.ElementNotInteractableException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;

/**
 * Common interactions for page objects. Every interaction waits explicitly for the
 * element to be ready and is retried, within the explicit timeout, when the DOM moves
 * under it (element re-rendered, covered by an overlay, not yet interactable).
 * Page objects therefore never deal with timing themselves.
 */
public abstract class BasePage {

    /** Transient states worth retrying: the element exists but the DOM is still settling. */
    private static final List<Class<? extends Throwable>> TRANSIENT = List.of(
            NoSuchElementException.class,
            StaleElementReferenceException.class,
            ElementClickInterceptedException.class,
            ElementNotInteractableException.class);

    protected final Logger log = LoggerFactory.getLogger(getClass());
    protected final WebDriver driver;
    protected final WebDriverWait wait;

    protected BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(Config.get().getInt("timeout.explicit")));
        this.wait.ignoreAll(TRANSIENT);
    }

    protected WebElement visible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected void click(By locator) {
        log.debug("Click {}", locator);
        wait.until(d -> {
            ExpectedConditions.elementToBeClickable(locator).apply(d).click();
            return true;
        });
    }

    protected void type(By locator, String text) {
        log.debug("Type '{}' into {}", text, locator);
        wait.until(d -> {
            WebElement element = d.findElement(locator);
            element.clear();
            if (!text.isEmpty()) {
                element.sendKeys(text);
            }
            return true;
        });
    }

    /** Same as {@link #type} but the value never reaches the logs. */
    protected void typeSecret(By locator, String secret) {
        log.debug("Type ******** into {}", locator);
        wait.until(d -> {
            WebElement element = d.findElement(locator);
            element.clear();
            if (!secret.isEmpty()) {
                element.sendKeys(secret);
            }
            return true;
        });
    }

    protected String textOf(By locator) {
        return wait.until(d -> {
            WebElement element = d.findElement(locator);
            return element.isDisplayed() ? element.getText() : null;
        });
    }

    protected boolean isDisplayed(By locator) {
        return driver.findElements(locator).stream().anyMatch(WebElement::isDisplayed);
    }

    protected void open(String url) {
        log.debug("Open {}", url);
        driver.get(url);
    }
}
