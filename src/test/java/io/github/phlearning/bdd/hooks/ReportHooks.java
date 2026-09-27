package io.github.phlearning.bdd.hooks;

import io.cucumber.java.BeforeAll;
import io.github.phlearning.bdd.config.Config;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Properties;

/**
 * Adds run-level information to the Allure results: the "Environment" widget and the
 * failure categories (see {@code allure/categories.json}).
 */
public class ReportHooks {

    @BeforeAll
    public static void describeRun() {
        Path results = Path.of(System.getProperty("allure.results.directory", "target/allure-results"));
        Config config = Config.get();
        try {
            Files.createDirectories(results);

            Properties environment = new Properties();
            environment.setProperty("Environment", config.get("env"));
            environment.setProperty("saucedemo URL", config.get("saucedemo.url"));
            environment.setProperty("the-internet URL", config.get("the-internet.url"));
            environment.setProperty("Browser", config.get("browser"));
            environment.setProperty("Headless", config.get("headless"));
            environment.setProperty("Execution", config.get("execution"));
            environment.setProperty("Java", System.getProperty("java.version"));
            environment.setProperty("OS", System.getProperty("os.name") + " " + System.getProperty("os.version"));
            try (OutputStream out = Files.newOutputStream(results.resolve("environment.properties"))) {
                environment.store(out, null);
            }

            try (InputStream categories =
                    ReportHooks.class.getClassLoader().getResourceAsStream("allure/categories.json")) {
                if (categories != null) {
                    Files.copy(categories, results.resolve("categories.json"), StandardCopyOption.REPLACE_EXISTING);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot write Allure run information to " + results, e);
        }
    }
}
