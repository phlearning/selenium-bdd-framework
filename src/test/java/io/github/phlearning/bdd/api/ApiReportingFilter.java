package io.github.phlearning.bdd.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.qameta.allure.Allure;
import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.http.Header;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Logs each call in one line and attaches the request and the response to the Allure step,
 * with secrets masked: credential headers and JSON fields such as {@code password} or
 * {@code accessToken}, wherever they appear (request bodies, but also responses: some APIs
 * send them back).
 */
public class ApiReportingFilter implements Filter {

    static final String MASK = "********";

    private static final Logger LOG = LoggerFactory.getLogger(ApiReportingFilter.class);
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final Set<String> SECRET_HEADERS = Set.of("authorization", "cookie", "set-cookie");
    private static final Set<String> SECRET_FIELDS =
            Set.of("password", "accesstoken", "refreshtoken", "token", "secret", "apikey");

    @Override
    public Response filter(
            FilterableRequestSpecification request, FilterableResponseSpecification spec, FilterContext context) {
        long start = System.nanoTime();
        Response response = context.next(request, spec);
        long millis = (System.nanoTime() - start) / 1_000_000;
        LOG.info("{} {} -> {} ({} ms)", request.getMethod(), request.getURI(), response.getStatusCode(), millis);

        Allure.addAttachment(
                "Requête " + request.getMethod() + " " + request.getURI(),
                "text/plain",
                request.getMethod() + " " + request.getURI() + "\n\n"
                        + headers(request.getHeaders().asList()) + "\n\n"
                        + mask(String.valueOf((Object) request.getBody())),
                ".txt");
        Allure.addAttachment(
                "Réponse " + response.getStatusCode() + " (" + millis + " ms)",
                "application/json",
                mask(response.asString()),
                ".json");
        return response;
    }

    private static String headers(List<Header> headers) {
        return headers.stream()
                .map(h -> h.getName() + ": "
                        + (SECRET_HEADERS.contains(h.getName().toLowerCase()) ? MASK : h.getValue()))
                .collect(Collectors.joining("\n"));
    }

    /** Masks secret fields of a JSON document; anything that is not JSON is returned unchanged. */
    static String mask(String body) {
        if (body == null || body.isBlank() || "null".equals(body)) {
            return "";
        }
        try {
            JsonNode root = JSON.readTree(body);
            maskNode(root);
            return JSON.writerWithDefaultPrettyPrinter().writeValueAsString(root);
        } catch (JsonProcessingException e) {
            return body;
        }
    }

    private static void maskNode(JsonNode node) {
        if (node instanceof ObjectNode object) {
            Iterator<Map.Entry<String, JsonNode>> fields = object.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                if (SECRET_FIELDS.contains(field.getKey().toLowerCase())) {
                    field.setValue(object.textNode(MASK));
                } else {
                    maskNode(field.getValue());
                }
            }
        } else if (node.isArray()) {
            node.forEach(ApiReportingFilter::maskNode);
        }
    }
}
