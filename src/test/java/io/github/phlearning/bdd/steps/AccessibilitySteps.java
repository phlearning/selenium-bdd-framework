package io.github.phlearning.bdd.steps;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.fr.Alors;
import io.github.phlearning.bdd.accessibility.AccessibilityAudit;
import io.github.phlearning.bdd.accessibility.AccessibilityAudit.Report;
import io.github.phlearning.bdd.driver.DriverManager;
import io.qameta.allure.Allure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class AccessibilitySteps {

    private static final Logger LOG = LoggerFactory.getLogger(AccessibilitySteps.class);

    private final DriverManager driverManager;

    public AccessibilitySteps(DriverManager driverManager) {
        this.driverManager = driverManager;
    }

    private Report audit() {
        Report report = AccessibilityAudit.run(driverManager.getDriver());
        LOG.info(
                "Accessibility audit: {} rule(s) broken on {}",
                report.violations().size(),
                report.url());
        Allure.addAttachment("Accessibility audit", "text/plain", report.summary(), ".txt");
        return report;
    }

    @Alors("la page respecte les règles d'accessibilité WCAG 2.1 AA")
    public void laPageRespecteLesReglesDAccessibilite() {
        Report report = audit();
        assertThat(report.violations()).as(report.summary()).isEmpty();
    }

    /**
     * Known issues are listed in the scenario, each with its reason: the page must break no
     * other rule. A known issue that disappears is reported, so that the list stays true.
     */
    @Alors("la page respecte les règles d'accessibilité WCAG 2.1 AA, hormis les écarts connus :")
    public void laPageRespecteLesReglesHormisLesEcartsConnus(DataTable knownIssues) {
        List<String> accepted =
                knownIssues.asMaps().stream().map(row -> row.get("règle")).toList();
        Report report = audit();
        List<String> fixed = accepted.stream()
                .filter(rule -> !report.brokenRules().contains(rule))
                .toList();
        if (!fixed.isEmpty()) {
            LOG.warn("Known accessibility issues no longer found, remove them from the scenario: {}", fixed);
            Allure.addAttachment("Known issues no longer found", "text/plain", String.join("\n", fixed), ".txt");
        }
        Report unexpected = report.excluding(accepted);
        assertThat(unexpected.violations()).as(unexpected.summary()).isEmpty();
    }
}
