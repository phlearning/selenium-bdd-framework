package io.github.phlearning.bdd.steps;

import io.cucumber.java.fr.Alors;
import io.cucumber.java.fr.Quand;
import io.cucumber.java.fr.Soit;
import io.github.phlearning.bdd.driver.DriverManager;
import io.github.phlearning.bdd.driver.WindowManager;
import io.github.phlearning.bdd.pages.internet.MultipleWindowsPage;
import io.github.phlearning.bdd.pages.internet.TheInternetPage;

import static org.assertj.core.api.Assertions.assertThat;

public class WindowSteps {

    private final DriverManager driverManager;

    public WindowSteps(DriverManager driverManager) {
        this.driverManager = driverManager;
    }

    private WindowManager windows() {
        return driverManager.session().windows();
    }

    @Soit("je suis sur la page des fenêtres multiples")
    public void jeSuisSurLaPageDesFenetresMultiples() {
        new MultipleWindowsPage(driverManager.getDriver()).open();
    }

    @Quand("je clique sur le lien qui ouvre la fenêtre {string}")
    public void jeCliqueSurLeLienQuiOuvreLaFenetre(String alias) {
        MultipleWindowsPage page = new MultipleWindowsPage(driverManager.getDriver());
        windows().openedBy(alias, page::clickNewWindowLink);
    }

    @Quand("j'ouvre un nouvel onglet {string} sur la page {string}")
    public void jOuvreUnNouvelOnglet(String alias, String path) {
        windows().openTab(alias, TheInternetPage.url(path));
    }

    @Quand("j'ouvre une nouvelle fenêtre {string} sur la page {string}")
    public void jOuvreUneNouvelleFenetre(String alias, String path) {
        windows().openWindow(alias, TheInternetPage.url(path));
    }

    @Quand("je bascule sur la fenêtre {string}")
    public void jeBasculeSurLaFenetre(String alias) {
        windows().switchTo(alias);
    }

    @Quand("je bascule sur la fenêtre intitulée {string}")
    public void jeBasculeSurLaFenetreIntitulee(String title) {
        windows().switchToTitle(title);
    }

    @Quand("je ferme la fenêtre {string}")
    public void jeFermeLaFenetre(String alias) {
        windows().close(alias);
    }

    @Alors("{int} fenêtres sont ouvertes")
    public void fenetresSontOuvertes(int count) {
        assertThat(windows().count()).isEqualTo(count);
    }

    @Alors("la fenêtre {string} est active")
    public void laFenetreEstActive(String alias) {
        assertThat(windows().currentAlias()).contains(alias);
    }

    @Alors("le titre de l'onglet est {string}")
    public void leTitreDeLOngletEst(String title) {
        assertThat(driverManager.getDriver().getTitle()).isEqualTo(title);
    }

    @Alors("la page affiche le titre {string}")
    public void laPageAfficheLeTitre(String heading) {
        assertThat(new TheInternetPage(driverManager.getDriver()).heading()).isEqualTo(heading);
    }
}
