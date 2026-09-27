package io.github.phlearning.bdd.steps;

import io.cucumber.java.fr.Alors;
import io.cucumber.java.fr.Quand;
import io.github.phlearning.bdd.api.ApiClient;
import io.github.phlearning.bdd.api.DummyJsonApi;
import io.github.phlearning.bdd.api.DummyJsonApi.NewProduct;
import io.github.phlearning.bdd.config.Config;
import io.restassured.response.Response;

import java.util.Map;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.assertj.core.api.Assertions.assertThat;

public class ApiSteps {

    private final ApiClient client;
    private final DummyJsonApi api;
    private final Config config = Config.get();
    private String accessToken;
    private NewProduct createdProduct;

    public ApiSteps(ApiClient client, DummyJsonApi api) {
        this.client = client;
        this.api = api;
    }

    private Response response() {
        return client.lastResponse();
    }

    // --- Authentication ---------------------------------------------------------------

    @Quand("je m'authentifie sur l'API avec l'utilisateur de démonstration")
    public void jeMAuthentifieAvecLUtilisateurDeDemonstration() {
        api.login(config.get("dummyjson.username"), config.get("dummyjson.password"));
    }

    @Quand("je m'authentifie sur l'API avec l'identifiant {string} et le mot de passe {string}")
    public void jeMAuthentifieAvec(String username, String password) {
        api.login(username, password);
    }

    @Alors("la réponse contient un jeton d'accès")
    public void laReponseContientUnJetonDAcces() {
        accessToken = response().jsonPath().getString("accessToken");
        assertThat(accessToken).as("accessToken").isNotBlank();
    }

    @Quand("je consulte mon profil avec ce jeton")
    public void jeConsulteMonProfilAvecCeJeton() {
        assertThat(accessToken).as("jeton obtenu à une étape précédente").isNotNull();
        api.currentUser(accessToken);
    }

    @Quand("je consulte mon profil avec le jeton {string}")
    public void jeConsulteMonProfilAvecLeJeton(String token) {
        api.currentUser(token);
    }

    @Quand("je consulte mon profil sans jeton")
    public void jeConsulteMonProfilSansJeton() {
        api.currentUserWithoutToken();
    }

    @Alors("le profil est celui de l'utilisateur de démonstration")
    public void leProfilEstCeluiDeLUtilisateurDeDemonstration() {
        assertThat(response().jsonPath().getString("username")).isEqualTo(config.get("dummyjson.username"));
    }

    // --- Products -----------------------------------------------------------------------

    @Quand("je consulte le produit {int}")
    public void jeConsulteLeProduit(int id) {
        api.product(id);
    }

    @Quand("je recherche les produits {string}")
    public void jeRechercheLesProduits(String query) {
        api.searchProducts(query);
    }

    @Alors("la recherche renvoie au moins {int} produit(s)")
    public void laRechercheRenvoieAuMoins(int count) {
        assertThat(response().jsonPath().getInt("total")).isGreaterThanOrEqualTo(count);
        assertThat(response().jsonPath().getList("products")).isNotEmpty();
    }

    @Alors("la recherche ne renvoie aucun produit")
    public void laRechercheNeRenvoieAucunProduit() {
        assertThat(response().jsonPath().getInt("total")).isZero();
        assertThat(response().jsonPath().getList("products")).isEmpty();
    }

    /** Vertical table: {@code | titre | ... |}, {@code | prix | ... |}, {@code | catégorie | ... |}. */
    @Quand("je crée le produit suivant :")
    public void jeCreeLeProduitSuivant(Map<String, String> fields) {
        createdProduct =
                new NewProduct(fields.get("titre"), Double.parseDouble(fields.get("prix")), fields.get("catégorie"));
        api.addProduct(createdProduct);
    }

    @Alors("le produit créé reprend ces informations avec un nouvel identifiant")
    public void leProduitCreeReprendCesInformations() {
        var json = response().jsonPath();
        assertThat(json.getInt("id")).isPositive();
        assertThat(json.getString("title")).isEqualTo(createdProduct.title());
        assertThat(json.getDouble("price")).isEqualTo(createdProduct.price());
        assertThat(json.getString("category")).isEqualTo(createdProduct.category());
    }

    // --- Generic checks ---------------------------------------------------------------

    @Alors("la réponse a le statut {int}")
    public void laReponseALeStatut(int status) {
        assertThat(response().statusCode()).as("statut HTTP").isEqualTo(status);
    }

    @Alors("le champ {string} de la réponse vaut {string}")
    public void leChampDeLaReponseVaut(String path, String value) {
        assertThat(response().jsonPath().getString(path)).as(path).isEqualTo(value);
    }

    @Alors("la réponse respecte le schéma {string}")
    public void laReponseRespecteLeSchema(String schema) {
        response().then().assertThat().body(matchesJsonSchemaInClasspath("schemas/" + schema + ".json"));
    }

    @Alors("la réponse arrive en moins de {int} ms")
    public void laReponseArriveEnMoinsDe(int millis) {
        assertThat(response().time()).as("temps de réponse (ms)").isLessThan(millis);
    }
}
