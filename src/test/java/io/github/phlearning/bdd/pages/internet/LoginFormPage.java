package io.github.phlearning.bdd.pages.internet;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/** the-internet login form: labelled fields, used by the accessibility and visual scenarios. */
public class LoginFormPage extends TheInternetPage {

    private static final By USERNAME = By.id("username");

    public LoginFormPage(WebDriver driver) {
        super(driver);
    }

    public void open() {
        openPath("/login");
        visible(USERNAME);
    }
}
