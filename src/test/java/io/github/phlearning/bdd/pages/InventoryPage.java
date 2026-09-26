package io.github.phlearning.bdd.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class InventoryPage extends BasePage {

    private static final By TITLE = By.cssSelector("[data-test='title']");
    private static final By ITEMS = By.cssSelector("[data-test='inventory-item']");

    public InventoryPage(WebDriver driver) {
        super(driver);
    }

    public String title() {
        wait.until(ExpectedConditions.urlContains("/inventory"));
        return textOf(TITLE);
    }

    public int itemCount() {
        visible(ITEMS);
        return driver.findElements(ITEMS).size();
    }
}
