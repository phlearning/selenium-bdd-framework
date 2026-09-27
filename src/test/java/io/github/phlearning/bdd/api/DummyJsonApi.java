package io.github.phlearning.bdd.api;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

/** Calls of the dummyjson.com API used by the scenarios: the API counterpart of a page object. */
public class DummyJsonApi {

    private static final String BASE_URL_KEY = "dummyjson.url";

    private final ApiClient client;

    public DummyJsonApi(ApiClient client) {
        this.client = client;
    }

    private RequestSpecification request() {
        return client.request(BASE_URL_KEY);
    }

    public Response login(String username, String password) {
        return client.remember(
                request().body(new Credentials(username, password)).post("/auth/login"));
    }

    public Response currentUser(String accessToken) {
        return client.remember(request().auth().oauth2(accessToken).get("/auth/me"));
    }

    public Response currentUserWithoutToken() {
        return client.remember(request().get("/auth/me"));
    }

    public Response product(int id) {
        return client.remember(request().pathParam("id", id).get("/products/{id}"));
    }

    public Response searchProducts(String query) {
        return client.remember(request().queryParam("q", query).get("/products/search"));
    }

    public Response addProduct(NewProduct product) {
        return client.remember(request().body(product).post("/products/add"));
    }

    public record Credentials(String username, String password) {}

    public record NewProduct(String title, double price, String category) {}
}
