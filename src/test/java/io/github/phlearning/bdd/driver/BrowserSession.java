package io.github.phlearning.bdd.driver;

import org.openqa.selenium.WebDriver;

/** One named browser of a scenario, with its tabs and windows. */
public record BrowserSession(String name, WebDriver driver, WindowManager windows) {
}
