package io.github.phlearning.bdd.pages.internet;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import java.nio.file.Path;

public class FileUploadPage extends TheInternetPage {

    private static final By FILE_INPUT = By.id("file-upload");
    private static final By SUBMIT = By.id("file-submit");
    private static final By UPLOADED_FILES = By.id("uploaded-files");

    public FileUploadPage(WebDriver driver) {
        super(driver);
    }

    public FileUploadPage open() {
        openPath("/upload");
        return this;
    }

    public void uploadFile(Path file) {
        upload(FILE_INPUT, file);
        click(SUBMIT);
    }

    public String uploadedFiles() {
        return textOf(UPLOADED_FILES);
    }
}
