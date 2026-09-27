package io.github.phlearning.bdd.runner;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectFile;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;

/**
 * Second pass, run by Surefire's {@code rerun-failed} execution: replays only the
 * scenarios listed in {@code target/rerun.txt}. It has to be a separate Surefire
 * execution because the JUnit Platform builds its test plan before anything runs.
 * <p>
 * Allure results of both passes land in the same directory: Allure groups the attempts
 * of a scenario and flags it as flaky when a failed attempt is followed by a pass.
 */
@Suite(failIfNoTests = false)
@IncludeEngines("cucumber")
@SelectFile("target/rerun.txt")
@ConfigurationParameter(
        key = PLUGIN_PROPERTY_NAME,
        value = RunCucumberTest.COMMON_PLUGINS
                + ", html:target/cucumber-reports/cucumber-rerun.html"
                + ", rerun:target/rerun-still-failing.txt")
public class RerunCucumberTest {}
