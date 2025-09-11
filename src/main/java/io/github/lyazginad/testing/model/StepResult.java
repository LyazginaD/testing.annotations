package io.github.lyazginad.testing.model;

import java.time.LocalDateTime;

/**
 * Represents the result of a single test step execution
 */
public class StepResult {
    private int order;
    private String description;
    private boolean passed;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String errorMessage;
    private Throwable exception;

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

    public void markCompleted(boolean success, Throwable exception) {
        this.endTime = LocalDateTime.now();
        this.passed = success;
        this.exception = exception;
        this.errorMessage = exception != null ? exception.getMessage() : null;
    }

    // Getters and setters
    public int getOrder() { return order; }
    public void setOrder(int order) { this.order = order; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public boolean isPassed() { return passed; }
    public void setPassed(boolean passed) { this.passed = passed; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public Throwable getException() { return exception; }
    public void setException(Throwable exception) { this.exception = exception; }
}