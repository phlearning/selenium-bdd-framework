package io.github.phlearning.bdd.accessibility;

import com.deque.html.axecore.results.CheckedNode;
import com.deque.html.axecore.results.Results;
import com.deque.html.axecore.results.Rule;
import com.deque.html.axecore.selenium.AxeBuilder;
import org.openqa.selenium.WebDriver;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Accessibility audit of the current page with axe-core (Deque), injected into the page:
 * the rules of WCAG 2.1 levels A and AA.
 */
public final class AccessibilityAudit {

    /** axe-core tags of the WCAG 2.0 and 2.1 rules, levels A and AA. */
    public static final List<String> WCAG_21_AA = List.of("wcag2a", "wcag2aa", "wcag21a", "wcag21aa");

    /** A rule broken by the page, with the elements that break it. */
    public record Violation(String rule, String impact, String help, String helpUrl, List<String> elements) {

        @Override
        public String toString() {
            return "[%s] %s - %s (%d element(s): %s)\n    %s"
                    .formatted(impact, rule, help, elements.size(), String.join(", ", elements), helpUrl);
        }
    }

    /** Violations found on one page. */
    public record Report(String url, List<Violation> violations) {

        public List<String> brokenRules() {
            return violations.stream().map(Violation::rule).toList();
        }

        /** The report without the given rules, for pages with known and accepted issues. */
        public Report excluding(List<String> rules) {
            return new Report(
                    url,
                    violations.stream().filter(v -> !rules.contains(v.rule())).toList());
        }

        public String summary() {
            if (violations.isEmpty()) {
                return "No WCAG 2.1 AA violation on " + url;
            }
            return violations.size() + " WCAG 2.1 AA rule(s) broken on " + url + ":\n"
                    + violations.stream().map(Violation::toString).collect(Collectors.joining("\n"));
        }
    }

    private AccessibilityAudit() {}

    public static Report run(WebDriver driver) {
        Results results = new AxeBuilder().withTags(WCAG_21_AA).analyze(driver);
        if (results.isErrored()) {
            throw new IllegalStateException("axe-core could not audit the page: " + results.getErrorMessage());
        }
        List<Violation> violations = results.getViolations().stream()
                .map(AccessibilityAudit::toViolation)
                .toList();
        return new Report(results.getUrl(), violations);
    }

    private static Violation toViolation(Rule rule) {
        List<String> elements = rule.getNodes().stream()
                .map(CheckedNode::getTarget)
                .map(String::valueOf)
                .toList();
        return new Violation(rule.getId(), rule.getImpact(), rule.getHelp(), rule.getHelpUrl(), elements);
    }
}
