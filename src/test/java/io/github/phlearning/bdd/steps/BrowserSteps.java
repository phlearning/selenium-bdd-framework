package io.github.phlearning.bdd.steps;

import io.cucumber.java.fr.Alors;
import io.cucumber.java.fr.Quand;
import io.cucumber.java.fr.Soit;
import io.github.phlearning.bdd.config.Config;
import io.github.phlearning.bdd.driver.BrowserConsole;
import io.github.phlearning.bdd.driver.BrowserNetwork;
import io.github.phlearning.bdd.driver.BrowserNetwork.FailedRequest;
import io.github.phlearning.bdd.driver.DriverManager;
import io.github.phlearning.bdd.pages.internet.BrokenImagesPage;
import io.github.phlearning.bdd.pages.internet.JavaScriptErrorPage;
import io.github.phlearning.bdd.pages.saucedemo.InventoryPage;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** What happens inside the browser, seen through WebDriver BiDi: JavaScript errors and network. */
public class BrowserSteps {

    private final DriverManager driverManager;

    public BrowserSteps(DriverManager driverManager) {
        this.driverManager = driverManager;
    }

    private BrowserConsole console() {
        BrowserConsole console = driverManager.session().console();
        assertThat(console.enabled()).as("WebDriver BiDi is needed (bidi=true)").isTrue();
        return console;
    }

    private BrowserNetwork network() {
        return driverManager.session().network();
    }

    /** BiDi events arrive asynchronously: wait for them, within the explicit timeout. */
    private WebDriverWait waitFor(String what) {
        WebDriverWait wait = new WebDriverWait(
                driverManager.getDriver(), Duration.ofSeconds(Config.get().getInt("timeout.explicit")));
        wait.withMessage(what);
        return wait;
    }

    @Soit("les requêtes vers {string} échouent")
    public void lesRequetesVersEchouent(String urlPart) {
        network().block(urlPart);
    }

    @Quand("j'ouvre une page qui déclenche une erreur JavaScript au chargement")
    public void jOuvreUnePageQuiDeclencheUneErreurJavaScript() {
        new JavaScriptErrorPage(driverManager.getDriver()).open();
    }

    @Quand("j'ouvre la page des images cassées")
    public void jOuvreLaPageDesImagesCassees() {
        new BrokenImagesPage(driverManager.getDriver()).open();
    }

    @Alors("la console du navigateur ne contient aucune erreur")
    public void laConsoleNeContientAucuneErreur() {
        BrowserConsole console = console();
        // Page fully loaded: errors raised while loading have been reported.
        waitFor("page loaded")
                .until(d -> "complete".equals(((JavascriptExecutor) d).executeScript("return document.readyState")));
        assertThat(console.errors()).as("JavaScript errors").isEmpty();
    }

    @Alors("la console du navigateur signale {int} erreur(s)")
    public void laConsoleSignaleErreurs(int count) {
        BrowserConsole console = console();
        waitFor(count + " JavaScript error(s)").until(d -> console.errors().size() >= count);
        assertThat(console.errors()).as("JavaScript errors").hasSize(count);
    }

    /** Distinct images: a browser may request a missing image more than once. */
    @Alors("{int} images de la page renvoient une erreur {int}")
    public void imagesDeLaPageRenvoientUneErreur(int count, int status) {
        waitFor(count + " images answered " + status)
                .until(d -> failedImages(status).size() >= count);
        assertThat(failedImages(status)).hasSize(count);
    }

    private List<String> failedImages(int status) {
        return network().failures().stream()
                .filter(failure -> failure.status() == status)
                .map(FailedRequest::url)
                .filter(url -> url.matches(".*\\.(jpg|jpeg|png|gif|svg)(\\?.*)?$"))
                .distinct()
                .toList();
    }

    @Alors("aucune image de produit n'est affichée")
    public void aucuneImageDeProduitNEstAffichee() {
        assertThat(new InventoryPage(driverManager.getDriver()).loadedImageCount())
                .isZero();
    }
}
