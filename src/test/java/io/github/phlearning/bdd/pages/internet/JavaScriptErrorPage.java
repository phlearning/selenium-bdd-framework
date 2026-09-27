package io.github.phlearning.bdd.pages.internet;

import org.openqa.selenium.WebDriver;

/** A page whose onload handler throws a JavaScript error. */
public class JavaScriptErrorPage extends TheInternetPage {

    public JavaScriptErrorPage(WebDriver driver) {
        super(driver);
    }

    public void open() {
        openPath("/javascript_error");
    }
}
