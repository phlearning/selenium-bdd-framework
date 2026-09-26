package io.github.phlearning.bdd.pages;

import io.github.phlearning.bdd.config.Config;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class LoginPage extends BasePage {

    private static final By USERNAME = By.id("user-name");
    private static final By PASSWORD = By.id("password");
    private static final By LOGIN_BUTTON = By.id("login-button");
    private static final By ERROR = By.cssSelector("[data-test='error']");

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    public LoginPage open() {
        open(Config.get().get("base.url"));
        visible(USERNAME);
        return this;
    }

    public void loginAs(String username, String password) {
        type(USERNAME, username);
        typeSecret(PASSWORD, password);
        click(LOGIN_BUTTON);
    }

    public String errorMessage() {
        return textOf(ERROR);
    }
}
