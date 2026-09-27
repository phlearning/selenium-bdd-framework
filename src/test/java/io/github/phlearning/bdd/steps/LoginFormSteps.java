package io.github.phlearning.bdd.steps;

import io.cucumber.java.fr.Soit;
import io.github.phlearning.bdd.driver.DriverManager;
import io.github.phlearning.bdd.pages.internet.LoginFormPage;

public class LoginFormSteps {

    private final DriverManager driverManager;

    public LoginFormSteps(DriverManager driverManager) {
        this.driverManager = driverManager;
    }

    @Soit("je suis sur le formulaire de connexion de the-internet")
    public void jeSuisSurLeFormulaireDeConnexionDeTheInternet() {
        new LoginFormPage(driverManager.getDriver()).open();
    }
}
