package io.github.phlearning.bdd.steps;

import io.cucumber.java.fr.Alors;
import io.cucumber.java.fr.Quand;
import io.cucumber.java.fr.Soit;
import io.github.phlearning.bdd.driver.DriverManager;
import io.github.phlearning.bdd.pages.internet.FileDownloadPage;
import io.github.phlearning.bdd.pages.internet.FileUploadPage;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

public class FileSteps {

    private static final String UPLOAD_DIR = "testdata/upload/";

    private final DriverManager driverManager;

    public FileSteps(DriverManager driverManager) {
        this.driverManager = driverManager;
    }

    private FileUploadPage page() {
        return new FileUploadPage(driverManager.getDriver());
    }

    private FileDownloadPage downloadPage() {
        return new FileDownloadPage(driverManager.getDriver());
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

    @Soit("je suis sur la page de téléchargement")
    public void jeSuisSurLaPageDeTelechargement() {
        downloadPage().open();
    }

    @Quand("je télécharge le fichier {string}")
    public void jeTelechargeLeFichier(String fileName) {
        downloadPage().download(fileName);
    }

    @Alors("le fichier {string} est téléchargé et n'est pas vide")
    public void leFichierEstTelecharge(String fileName) throws IOException {
        Path file = driverManager.session().downloads().waitFor(fileName);
        assertThat(file).exists().isRegularFile();
        assertThat(Files.size(file)).isPositive();
    }

    private static Path testFile(String fileName) {
        URL resource = FileSteps.class.getClassLoader().getResource(UPLOAD_DIR + fileName);
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
