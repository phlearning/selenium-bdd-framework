package io.github.phlearning.bdd.runner;

import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

/**
 * Entry point run by Surefire. Glue, plugins and filters are configured in
 * {@code junit-platform.properties} and can be overridden with -D system properties.
 */
@Suite
@IncludeEngines("cucumber")
@SelectPackages("features")
public class RunCucumberTest {
}
