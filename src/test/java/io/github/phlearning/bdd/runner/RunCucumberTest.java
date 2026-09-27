package io.github.phlearning.bdd.runner;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;

/**
 * First pass, run by Surefire's {@code default-test} execution. Failed scenarios are
 * written to {@code target/rerun.txt} and replayed by {@link RerunCucumberTest}.
 * Shared settings (glue, parallelism) live in {@code junit-platform.properties}.
 */
@Suite
@IncludeEngines("cucumber")
@SelectPackages("features")
@ConfigurationParameter(
        key = PLUGIN_PROPERTY_NAME,
        value = RunCucumberTest.COMMON_PLUGINS
                + ", html:target/cucumber-reports/cucumber.html"
                + ", rerun:target/rerun.txt")
public class RunCucumberTest {

    static final String COMMON_PLUGINS = "pretty, io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm";
}
