package io.github.phlearning.bdd.pages.internet;

import org.openqa.selenium.WebDriver;

/** A page with two images whose files do not exist on the server. */
public class BrokenImagesPage extends TheInternetPage {

    public BrokenImagesPage(WebDriver driver) {
        super(driver);
    }

    public void open() {
        openPath("/broken_images");
        heading();
    }
}
