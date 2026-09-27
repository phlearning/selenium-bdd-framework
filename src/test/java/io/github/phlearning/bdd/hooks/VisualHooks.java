package io.github.phlearning.bdd.hooks;

import io.cucumber.java.Before;
import io.github.phlearning.bdd.config.Config;
import io.github.phlearning.bdd.driver.DriverFactory;
import org.opentest4j.TestAbortedException;

public class VisualHooks {

    /**
     * Visual baselines are taken on the Selenium Grid: a local browser renders differently
     * (fonts, screen, version), so the comparison would only report noise. Skipped there.
     */
    @Before("@visuel")
    public void onlyOnTheGrid() {
        if (!DriverFactory.isGrid(Config.get())) {
            throw new TestAbortedException("Visual regression runs on the Selenium Grid only (-Dexecution=grid)");
        }
    }
}
