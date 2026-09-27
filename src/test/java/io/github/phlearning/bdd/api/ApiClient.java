package io.github.phlearning.bdd.api;

import io.github.phlearning.bdd.config.Config;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

/**
 * Entry point of the API layer, the counterpart of {@code DriverManager} for the UI.
 * PicoContainer creates one per scenario: it keeps the scenario's last response so that
 * "Then" steps can check it.
 * <p>
 * Every request and response is logged in one line and attached to the Allure report,
 * secrets masked (see {@link ApiReportingFilter}).
 */
public class ApiClient {

    private final Config config = Config.get();
    private Response lastResponse;

    /** New request to the API whose base URL is configured under {@code baseUrlKey}. */
    public RequestSpecification request(String baseUrlKey) {
        return RestAssured.given()
                .baseUri(config.get(baseUrlKey))
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .filter(new ApiReportingFilter());
    }

    Response remember(Response response) {
        lastResponse = response;
        return response;
    }

    public Response lastResponse() {
        if (lastResponse == null) {
            throw new IllegalStateException("No API call has been made in this scenario yet");
        }
        return lastResponse;
    }
}
