package io.github.phlearning.bdd.pages.internet;

import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class JavaScriptAlertsPage extends TheInternetPage {

    private static final By RESULT = By.id("result");

    public JavaScriptAlertsPage(WebDriver driver) {
        super(driver);
    }

    public JavaScriptAlertsPage open() {
        openPath("/javascript_alerts");
        return this;
    }

    public void clickButton(String label) {
        click(By.xpath("//button[normalize-space()='%s']".formatted(label)));
    }

    public String dialogText() {
        return dialog().getText();
    }

    public void acceptDialog() {
        dialog().accept();
    }

    public void dismissDialog() {
        dialog().dismiss();
    }

    public void answerDialog(String text) {
        Alert prompt = dialog();
        prompt.sendKeys(text);
        prompt.accept();
    }

    public String result() {
        return textOf(RESULT);
    }
}
