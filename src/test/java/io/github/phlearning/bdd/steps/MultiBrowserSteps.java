package io.github.phlearning.bdd.steps;

import io.cucumber.java.fr.Alors;
import io.cucumber.java.fr.Quand;
import io.cucumber.java.fr.Soit;
import io.github.phlearning.bdd.config.Config;
import io.github.phlearning.bdd.driver.DriverManager;
import io.github.phlearning.bdd.pages.saucedemo.InventoryPage;
import io.github.phlearning.bdd.pages.saucedemo.LoginPage;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Steps that name the browser they act in. Each one makes that browser the active
 * session first, so the following unnamed steps keep acting in it.
 */
public class MultiBrowserSteps {

    private final DriverManager driverManager;
    private final Config config = Config.get();

    public MultiBrowserSteps(DriverManager driverManager) {
        this.driverManager = driverManager;
    }

    private InventoryPage inventoryIn(String browser) {
        return new InventoryPage(driverManager.use(browser).driver());
    }

    @Soit("l'utilisateur standard est connecté dans le navigateur {string}")
    public void lUtilisateurStandardEstConnecteDansLeNavigateur(String browser) {
        LoginPage login = new LoginPage(driverManager.use(browser).driver()).open();
        login.loginAs(config.get("sauce.username"), config.get("sauce.password"));
        assertThat(inventoryIn(browser).title()).isEqualTo("Products");
    }

    @Quand("dans le navigateur {string}, j'ajoute le produit {string} au panier")
    public void dansLeNavigateurJAjouteLeProduitAuPanier(String browser, String product) {
        inventoryIn(browser).addToCart(product);
    }

    @Alors("dans le navigateur {string}, le panier contient {int} article(s)")
    public void dansLeNavigateurLePanierContient(String browser, int count) {
        assertThat(inventoryIn(browser).cartCount()).isEqualTo(count);
    }

    @Alors("dans le navigateur {string}, le panier est vide")
    public void dansLeNavigateurLePanierEstVide(String browser) {
        assertThat(inventoryIn(browser).cartCount()).isZero();
    }
}
