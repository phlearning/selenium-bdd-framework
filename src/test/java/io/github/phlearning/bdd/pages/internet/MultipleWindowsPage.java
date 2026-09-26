package io.github.phlearning.bdd.pages.internet;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class MultipleWindowsPage extends TheInternetPage {

    private static final By NEW_WINDOW_LINK = By.linkText("Click Here");

    public MultipleWindowsPage(WebDriver driver) {
        super(driver);
    }

    public MultipleWindowsPage open() {
        openPath("/windows");
        return this;
    }

    /** Opens /windows/new in a new window ({@code target="_blank"}); the driver stays on this one. */
    public void clickNewWindowLink() {
        click(NEW_WINDOW_LINK);
    }
}
