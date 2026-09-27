package io.github.phlearning.bdd.steps;

import io.cucumber.java.fr.Et;

/** Steps of the demo scenarios (features/demo), which illustrate failures in the report. */
public class DemoSteps {

    /** Fails on the first pass only: the rerun passes, so the scenario is reported as flaky. */
    @Et("le réseau est instable au premier passage")
    public void leReseauEstInstableAuPremierPassage() {
        if (!Boolean.getBoolean("rerun.pass")) {
            throw new AssertionError("Délai dépassé en attendant la réponse du serveur (simulé)");
        }
    }
}
