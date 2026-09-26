package io.github.phlearning.bdd.pages.internet;

import io.github.phlearning.bdd.config.Config;
import io.github.phlearning.bdd.pages.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/** Any page of the-internet: every example page has a single {@code <h3>} heading. */
public class TheInternetPage extends BasePage {

    private static final By HEADING = By.tagName("h3");

    public TheInternetPage(WebDriver driver) {
        super(driver);
    }

    protected void openPath(String path) {
        open(url(path));
    }

    public static String url(String path) {
        return Config.get().get("the-internet.url") + path;
    }

    public String heading() {
        return textOf(HEADING);
    }
}
