package io.github.phlearning.bdd.steps;

import io.cucumber.java.fr.Et;
import io.cucumber.java.fr.Quand;
import io.github.phlearning.bdd.driver.DriverManager;
import org.openqa.selenium.JavascriptExecutor;

/** Steps of the demo scenarios (features/demo), which illustrate failures in the report. */
public class DemoSteps {

    private final DriverManager driverManager;

    public DemoSteps(DriverManager driverManager) {
        this.driverManager = driverManager;
    }

    /** Fails on the first pass only: the rerun passes, so the scenario is reported as flaky. */
    @Et("le réseau est instable au premier passage")
    public void leReseauEstInstableAuPremierPassage() {
        if (!Boolean.getBoolean("rerun.pass")) {
            throw new AssertionError("Délai dépassé en attendant la réponse du serveur (simulé)");
        }
    }

    /** A small style regression, as a faulty CSS change would introduce. */
    @Quand("le bouton de connexion change de couleur")
    public void leBoutonDeConnexionChangeDeCouleur() {
        ((JavascriptExecutor) driverManager.getDriver())
                .executeScript("document.querySelector('#login-button').style.backgroundColor = '#e67e22';");
    }
}
