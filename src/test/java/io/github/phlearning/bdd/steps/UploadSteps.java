package io.github.phlearning.bdd.steps;

import io.cucumber.java.fr.Alors;
import io.cucumber.java.fr.Quand;
import io.cucumber.java.fr.Soit;
import io.github.phlearning.bdd.driver.DriverManager;
import io.github.phlearning.bdd.pages.internet.FileUploadPage;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

public class UploadSteps {

    private static final String UPLOAD_DIR = "testdata/upload/";

    private final DriverManager driverManager;

    public UploadSteps(DriverManager driverManager) {
        this.driverManager = driverManager;
    }

    private FileUploadPage page() {
        return new FileUploadPage(driverManager.getDriver());
    }

    @Soit("je suis sur la page de téléversement")
    public void jeSuisSurLaPageDeTeleversement() {
        page().open();
    }

    @Quand("je téléverse le fichier {string}")
    public void jeTeleverseLeFichier(String fileName) {
        page().uploadFile(testFile(fileName));
    }

    @Alors("le fichier {string} figure parmi les fichiers reçus")
    public void leFichierFigureParmiLesFichiersRecus(String fileName) {
        assertThat(page().uploadedFiles()).contains(fileName);
    }

    private static Path testFile(String fileName) {
        URL resource = UploadSteps.class.getClassLoader().getResource(UPLOAD_DIR + fileName);
        if (resource == null) {
            throw new IllegalArgumentException("Test file not found on classpath: " + UPLOAD_DIR + fileName);
        }
        try {
            return Path.of(resource.toURI());
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }
}
