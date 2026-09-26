package io.github.phlearning.bdd.pages.internet;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class FileDownloadPage extends TheInternetPage {

    public FileDownloadPage(WebDriver driver) {
        super(driver);
    }

    public FileDownloadPage open() {
        openPath("/download");
        return this;
    }

    public void download(String fileName) {
        click(By.linkText(fileName));
    }
}
