package io.github.phlearning.bdd.logging;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

/**
 * Captures the log lines of the scenario running on the current thread, so they can be
 * attached to that scenario in the report. Each scenario (hooks and steps included)
 * runs on a single thread, which keeps parallel scenarios' logs apart.
 */
public class ScenarioLogAppender extends AppenderBase<ILoggingEvent> {

    private static final ThreadLocal<StringBuilder> BUFFER = new ThreadLocal<>();

    private final PatternLayout layout = new PatternLayout();
    private String pattern = "%d{HH:mm:ss.SSS} %-5level %logger{20} - %msg%n";

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    @Override
    public void start() {
        layout.setContext(getContext());
        layout.setPattern(pattern);
        layout.start();
        super.start();
    }

    @Override
    protected void append(ILoggingEvent event) {
        StringBuilder buffer = BUFFER.get();
        if (buffer != null) {
            buffer.append(layout.doLayout(event));
        }
    }

    /** Starts capturing on the current thread. */
    public static void startCapture() {
        BUFFER.set(new StringBuilder());
    }

    /** Stops capturing on the current thread and returns what was captured. */
    public static String stopCapture() {
        StringBuilder buffer = BUFFER.get();
        BUFFER.remove();
        return buffer == null ? "" : buffer.toString();
    }
}
