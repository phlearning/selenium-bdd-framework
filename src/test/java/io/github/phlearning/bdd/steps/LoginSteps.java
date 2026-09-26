package io.github.phlearning.bdd.steps;

import io.cucumber.java.fr.Alors;
import io.cucumber.java.fr.Quand;
import io.cucumber.java.fr.Soit;
import io.github.phlearning.bdd.config.Config;
import io.github.phlearning.bdd.driver.DriverManager;
import io.github.phlearning.bdd.pages.saucedemo.InventoryPage;
import io.github.phlearning.bdd.pages.saucedemo.LoginPage;

import static org.assertj.core.api.Assertions.assertThat;

public class LoginSteps {

    private final DriverManager driverManager;
    private final Config config = Config.get();

    public LoginSteps(DriverManager driverManager) {
        this.driverManager = driverManager;
    }

    private LoginPage loginPage() {
        return new LoginPage(driverManager.getDriver());
    }

    private InventoryPage inventoryPage() {
        return new InventoryPage(driverManager.getDriver());
    }

    @Soit("je suis sur la page de connexion")
    public void jeSuisSurLaPageDeConnexion() {
        loginPage().open();
    }

    @Quand("je me connecte avec l'utilisateur standard")
    public void jeMeConnecteAvecLUtilisateurStandard() {
        loginPage().loginAs(config.get("sauce.username"), config.get("sauce.password"));
    }

    @Quand("je me connecte avec l'identifiant {string} et le mot de passe {string}")
    public void jeMeConnecteAvec(String username, String password) {
        loginPage().loginAs(username, password);
    }

    @Alors("la page {string} est affichée")
    public void laPageEstAffichee(String title) {
        assertThat(inventoryPage().title()).isEqualTo(title);
    }

    @Alors("le catalogue contient {int} produits")
    public void leCatalogueContientProduits(int count) {
        assertThat(inventoryPage().itemCount()).isEqualTo(count);
    }

    @Alors("le message d'erreur {string} est affiché")
    public void leMessageDErreurEstAffiche(String message) {
        assertThat(loginPage().errorMessage()).isEqualTo(message);
    }
}
