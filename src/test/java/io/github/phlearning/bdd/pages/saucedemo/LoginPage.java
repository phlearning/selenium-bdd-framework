package io.github.phlearning.bdd.pages.saucedemo;

import io.github.phlearning.bdd.config.Config;
import io.github.phlearning.bdd.pages.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;

public class LoginPage extends BasePage {

    private static final By USERNAME = By.id("user-name");
    private static final By PASSWORD = By.id("password");
    private static final By LOGIN_BUTTON = By.id("login-button");
    private static final By ERROR = By.cssSelector("[data-test='error']");
    private static final String SESSION_COOKIE = "session-username";

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    public LoginPage open() {
        open(Config.get().get("saucedemo.url"));
        visible(USERNAME);
        return this;
    }

    public void loginAs(String username, String password) {
        type(USERNAME, username);
        typeSecret(PASSWORD, password);
        click(LOGIN_BUTTON);
    }

    /**
     * Logs in without going through the form, the way an API login would: saucedemo keeps its
     * session in the {@value #SESSION_COOKIE} cookie, so setting it is enough. For scenarios whose
     * subject is not the login itself; the login form keeps its own scenarios.
     */
    public void loginBypassingForm(String username) {
        String baseUrl = Config.get().get("saucedemo.url");
        open(baseUrl); // cookies can only be set on the current domain
        driver.manage().addCookie(new Cookie(SESSION_COOKIE, username));
        log.debug("Session cookie set for '{}'", username);
        open(baseUrl + "/inventory.html");
    }

    public String errorMessage() {
        return textOf(ERROR);
    }
}
