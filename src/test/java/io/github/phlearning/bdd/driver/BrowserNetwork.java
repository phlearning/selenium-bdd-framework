package io.github.phlearning.bdd.driver;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.bidi.HasBiDi;
import org.openqa.selenium.bidi.module.Network;
import org.openqa.selenium.bidi.network.AddInterceptParameters;
import org.openqa.selenium.bidi.network.BeforeRequestSent;
import org.openqa.selenium.bidi.network.CacheBehavior;
import org.openqa.selenium.bidi.network.ContinueRequestParameters;
import org.openqa.selenium.bidi.network.InterceptPhase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Network traffic of one browser, through WebDriver BiDi: failed requests are recorded
 * from the moment the browser starts (HTTP errors and requests that never got a response),
 * and a scenario can make requests fail on purpose to check how the application copes.
 */
public final class BrowserNetwork {

    /** A request that failed: an HTTP status of 400 or more, or no response at all (status 0). */
    public record FailedRequest(String url, int status, String reason) {

        @Override
        public String toString() {
            return (status > 0 ? String.valueOf(status) : reason) + " " + url;
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(BrowserNetwork.class);

    private final Network network;
    /**
     * Interception decisions are sent from here, not from the thread that delivers BiDi events:
     * that thread also reads the command responses, so waiting on it would block forever.
     */
    private final ExecutorService decisions = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "bidi-network-interception");
        thread.setDaemon(true);
        return thread;
    });

    private final List<FailedRequest> failures = new CopyOnWriteArrayList<>();
    private final List<String> blockedPatterns = new CopyOnWriteArrayList<>();
    private boolean intercepting;

    private BrowserNetwork(Network network) {
        this.network = network;
    }

    /** Starts recording, or returns a disabled network when the driver has no BiDi connection. */
    static BrowserNetwork listen(WebDriver driver) {
        if (!(driver instanceof HasBiDi)) {
            return new BrowserNetwork(null);
        }
        BrowserNetwork browserNetwork = new BrowserNetwork(new Network(driver));
        browserNetwork.network.onResponseCompleted(response -> {
            int status = response.getResponseData().getStatus();
            if (status >= 400) {
                browserNetwork.fail(new FailedRequest(response.getRequest().getUrl(), status, "HTTP " + status));
            }
        });
        browserNetwork.network.onFetchError(error ->
                browserNetwork.fail(new FailedRequest(error.getRequest().getUrl(), 0, error.getErrorText())));
        return browserNetwork;
    }

    private void fail(FailedRequest failure) {
        LOG.debug("Failed request: {}", failure);
        failures.add(failure);
    }

    public boolean enabled() {
        return network != null;
    }

    /** Failed requests so far, oldest first. */
    public List<FailedRequest> failures() {
        return List.copyOf(failures);
    }

    /**
     * Makes every later request whose URL contains {@code urlPart} fail, as if the server
     * could not be reached. Other requests go through untouched.
     * <p>
     * ChromeDriver runs the commands of a session one at a time: a classic navigation
     * ({@code driver.get}, or a click that loads a new document) waits for the page, the page
     * waits for its intercepted requests, and their release waits for the navigation to end.
     * With Chrome, start blocking once the page is loaded, before the action whose requests
     * must fail (asynchronous loads: images, fetch, single-page navigation).
     */
    public synchronized void block(String urlPart) {
        if (!enabled()) {
            throw new IllegalStateException("Network interception needs WebDriver BiDi (bidi=true)");
        }
        blockedPatterns.add(urlPart);
        if (!intercepting) {
            // BiDi URL patterns match whole URL components, not substrings: intercept everything
            // and decide here. Every request then waits for this handler, hence the minimal work.
            // Responses served from the cache never reach the interception.
            network.setCacheBehavior(CacheBehavior.BYPASS);
            network.onBeforeRequestSent(request -> decisions.execute(() -> decide(request)));
            network.addIntercept(new AddInterceptParameters(InterceptPhase.BEFORE_REQUEST_SENT));
            intercepting = true;
        }
        LOG.info("Requests containing '{}' now fail", urlPart);
    }

    private void decide(BeforeRequestSent request) {
        if (!request.isBlocked()) {
            return;
        }
        String id = request.getRequest().getRequestId();
        String url = request.getRequest().getUrl();
        try {
            if (blockedPatterns.stream().anyMatch(url::contains)) {
                LOG.debug("Blocked request: {}", url);
                network.failRequest(id);
            } else {
                network.continueRequest(new ContinueRequestParameters(id));
            }
        } catch (RuntimeException e) {
            // Browser closed, or navigation already cancelled the request
            LOG.debug("Could not resolve intercepted request {}: {}", url, e.getMessage());
        }
    }
}
