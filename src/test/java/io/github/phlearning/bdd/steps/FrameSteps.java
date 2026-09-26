package io.github.phlearning.bdd.steps;

import io.cucumber.java.fr.Alors;
import io.cucumber.java.fr.Soit;
import io.github.phlearning.bdd.driver.DriverManager;
import io.github.phlearning.bdd.pages.internet.NestedFramesPage;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

public class FrameSteps {

    private final DriverManager driverManager;

    public FrameSteps(DriverManager driverManager) {
        this.driverManager = driverManager;
    }

    private NestedFramesPage page() {
        return new NestedFramesPage(driverManager.getDriver());
    }

    @Soit("je suis sur la page des cadres imbriqués")
    public void jeSuisSurLaPageDesCadresImbriques() {
        page().open();
    }

    /** {@code chemin} lists frame names from the outermost one, separated by {@code >}. */
    @Alors("le cadre {string} contient le texte {string}")
    public void leCadreContientLeTexte(String chemin, String texte) {
        var framePath = Arrays.stream(chemin.split(">")).map(String::trim).toList();
        assertThat(page().textOfFrame(framePath)).isEqualTo(texte);
    }

    @Alors("le document principal contient {int} cadres")
    public void leDocumentPrincipalContientCadres(int count) {
        assertThat(page().topLevelFrameCount()).isEqualTo(count);
    }
}
