package io.github.lyazginad.testing.model;

import java.time.LocalDateTime;

/**
 * Represents the result of a single test step execution
 */
public class StepResult {
    private final int order;
    private final String description;
    private boolean passed;
    private final LocalDateTime startTime;
    private LocalDateTime endTime;
    private String errorMessage;

    public StepResult(int order, String description) {
        this.order = order;
        this.description = description;
        this.passed = true;
        this.startTime = LocalDateTime.now();
    }

    public long getDuration() {
        if (startTime != null && endTime != null) {
            return java.time.Duration.between(startTime, endTime).toMillis();
        }
        return 0;
    }

    public void markCompleted(boolean success, String error) {
        this.endTime = LocalDateTime.now();
        this.passed = success;
        this.errorMessage = error;
    }

    // Getters only (immutable for order and description)
    public int getOrder() { return order; }
    public String getDescription() { return description; }
    public boolean isPassed() { return passed; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public String getErrorMessage() { return errorMessage; }
}