package io.github.phlearning.bdd.config;

import io.github.cdimascio.dotenv.Dotenv;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Locale;
import java.util.Optional;
import java.util.Properties;

/**
 * Single entry point for configuration values.
 * <p>
 * A key such as {@code base.url} is resolved from the first source that defines it:
 * <ol>
 *     <li>JVM system property {@code -Dbase.url=...}</li>
 *     <li>environment variable {@code BASE_URL}</li>
 *     <li>local {@code .env} file (never committed)</li>
 *     <li>{@code config/environments/<env>.properties}</li>
 *     <li>{@code config/default.properties}</li>
 * </ol>
 * The active environment is itself read with key {@code env}.
 */
public final class Config {

    private static final String DEFAULTS = "config/default.properties";
    private static final String ENVIRONMENT_FILE = "config/environments/%s.properties";

    private final Properties fileProperties;
    private final Dotenv dotenv;

    private Config() {
        this.dotenv = Dotenv.configure().ignoreIfMissing().load();
        Properties defaults = load(DEFAULTS, true);
        String env = lookupOverride("env").orElse(defaults.getProperty("env"));
        this.fileProperties = new Properties();
        this.fileProperties.putAll(defaults);
        this.fileProperties.putAll(load(ENVIRONMENT_FILE.formatted(env), true));
    }

    private static final class Holder {
        private static final Config INSTANCE = new Config();
    }

    public static Config get() {
        return Holder.INSTANCE;
    }

    public Optional<String> find(String key) {
        return lookupOverride(key).or(() -> Optional.ofNullable(fileProperties.getProperty(key)))
                .map(String::trim)
                .filter(value -> !value.isEmpty());
    }

    public String get(String key) {
        return find(key).orElseThrow(() -> new IllegalStateException(
                "Missing configuration '%s' (system property, env var %s, .env or properties file)"
                        .formatted(key, toEnvName(key))));
    }

    public int getInt(String key) {
        return Integer.parseInt(get(key));
    }

    public boolean getBoolean(String key) {
        return Boolean.parseBoolean(get(key));
    }

    private Optional<String> lookupOverride(String key) {
        String envName = toEnvName(key);
        return Optional.ofNullable(System.getProperty(key))
                .or(() -> Optional.ofNullable(System.getenv(envName)))
                .or(() -> Optional.ofNullable(dotenv.get(envName, null)));
    }

    static String toEnvName(String key) {
        return key.replace('.', '_').replace('-', '_').toUpperCase(Locale.ROOT);
    }

    private static Properties load(String resource, boolean required) {
        Properties properties = new Properties();
        try (InputStream in = Config.class.getClassLoader().getResourceAsStream(resource)) {
            if (in == null) {
                if (required) {
                    throw new IllegalStateException("Configuration file not found on classpath: " + resource);
                }
                return properties;
            }
            properties.load(in);
            return properties;
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read " + resource, e);
        }
    }
}
