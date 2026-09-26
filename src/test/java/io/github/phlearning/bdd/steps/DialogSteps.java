package io.github.phlearning.bdd.steps;

import io.cucumber.java.fr.Alors;
import io.cucumber.java.fr.Quand;
import io.cucumber.java.fr.Soit;
import io.github.phlearning.bdd.driver.DriverManager;
import io.github.phlearning.bdd.pages.internet.JavaScriptAlertsPage;

import static org.assertj.core.api.Assertions.assertThat;

public class DialogSteps {

    private final DriverManager driverManager;

    public DialogSteps(DriverManager driverManager) {
        this.driverManager = driverManager;
    }

    private JavaScriptAlertsPage page() {
        return new JavaScriptAlertsPage(driverManager.getDriver());
    }

    @Soit("je suis sur la page des alertes JavaScript")
    public void jeSuisSurLaPageDesAlertesJavaScript() {
        page().open();
    }

    @Quand("je clique sur le bouton {string}")
    public void jeCliqueSurLeBouton(String label) {
        page().clickButton(label);
    }

    @Alors("une boîte de dialogue affiche {string}")
    public void uneBoiteDeDialogueAffiche(String text) {
        assertThat(page().dialogText()).isEqualTo(text);
    }

    @Quand("j'accepte la boîte de dialogue")
    public void jAccepteLaBoiteDeDialogue() {
        page().acceptDialog();
    }

    @Quand("je refuse la boîte de dialogue")
    public void jeRefuseLaBoiteDeDialogue() {
        page().dismissDialog();
    }

    @Quand("je réponds {string} à la boîte de dialogue")
    public void jeReponds(String answer) {
        page().answerDialog(answer);
    }

    @Alors("le résultat affiché est {string}")
    public void leResultatAfficheEst(String result) {
        assertThat(page().result()).isEqualTo(result);
    }
}
