package io.github.phlearning.bdd.steps;

import io.cucumber.java.fr.Alors;
import io.github.phlearning.bdd.config.Config;
import io.github.phlearning.bdd.driver.DriverManager;
import io.github.phlearning.bdd.visual.VisualCheck;

public class VisualSteps {

    private final DriverManager driverManager;

    public VisualSteps(DriverManager driverManager) {
        this.driverManager = driverManager;
    }

    @Alors("l'apparence de la page est conforme à la référence {string}")
    public void lApparenceDeLaPageEstConformeALaReference(String baseline) {
        new VisualCheck(driverManager.getDriver(), Config.get()).matches(baseline);
    }
}
