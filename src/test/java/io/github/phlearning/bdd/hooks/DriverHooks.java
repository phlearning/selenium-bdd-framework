package io.github.phlearning.bdd.hooks;

import io.cucumber.java.After;
import io.github.phlearning.bdd.driver.DriverManager;

public class DriverHooks {

    private final DriverManager driverManager;

    public DriverHooks(DriverManager driverManager) {
        this.driverManager = driverManager;
    }

    // Lowest order runs last: other @After hooks (screenshots, logs) still see an open browser.
    @After(order = 0)
    public void quitBrowser() {
        driverManager.quit();
    }
}
