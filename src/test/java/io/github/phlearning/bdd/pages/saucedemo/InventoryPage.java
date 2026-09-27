package io.github.phlearning.bdd.pages.saucedemo;

import io.github.phlearning.bdd.pages.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class InventoryPage extends BasePage {

    private static final By TITLE = By.cssSelector("[data-test='title']");
    private static final By ITEMS = By.cssSelector("[data-test='inventory-item']");
    private static final By CART_BADGE = By.cssSelector("[data-test='shopping-cart-badge']");

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

    public void addToCart(String productName) {
        click(By.xpath(
                "//*[@data-test='inventory-item'][.//*[@data-test='inventory-item-name' and normalize-space()='%s']]//button"
                        .formatted(productName)));
    }

    /** Product pictures actually displayed: loaded, with a real size. */
    public int loadedImageCount() {
        visible(ITEMS);
        Object count = ((JavascriptExecutor) driver)
                .executeScript("return [...document.querySelectorAll('img.inventory_item_img')]"
                        + ".filter(img => img.complete && img.naturalWidth > 0).length;");
        return ((Number) count).intValue();
    }

    /** Number shown on the cart icon; the badge is absent when the cart is empty. */
    public int cartCount() {
        return isDisplayed(CART_BADGE) ? Integer.parseInt(textOf(CART_BADGE)) : 0;
    }
}
