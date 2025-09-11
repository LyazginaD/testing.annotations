package io.github.lyazginad.testing.model;

import io.github.lyazginad.testing.annotations.Priority;
import io.github.lyazginad.testing.annotations.Severity;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the result of a single test case execution
 */
public class TestResult {
    private final String className;
    private final String methodName;
    private int order;
    private String testName;
    private String category;
    private boolean passed;
    private final LocalDateTime startTime;
    private LocalDateTime endTime;
    private String errorMessage;
    private final List<StepResult> steps = new ArrayList<>();
    private Severity.Level severity;
    private Priority.Level priority;
    private String testLevel;
    private String testType;
    private String testMethod;
    private String author;
    private String version;
    private String description;

    public TestResult(String className, String methodName, int order, String testName) {
        this.className = className;
        this.methodName = methodName;
        this.order = order;
        this.testName = testName;
        this.passed = true;
        this.startTime = LocalDateTime.now();
        this.category = "general";
        this.severity = Severity.Level.MEDIUM;
        this.priority = Priority.Level.P2;
        this.testLevel = "UNIT";
        this.testType = "FUNCTIONAL";
        this.testMethod = "BLACK_BOX";
    }

    public long getDuration() {
        if (startTime != null && endTime != null) {
            return java.time.Duration.between(startTime, endTime).toMillis();
        }
        return 0;
    }

    public void addStep(StepResult step) {
        steps.add(step);
    }

    public void markCompleted(boolean success, String error) {
        this.endTime = LocalDateTime.now();
        this.passed = success;
        this.errorMessage = error;

        // Mark all steps as completed if the test is done
        for (StepResult step : steps) {
            if (step.getEndTime() == null) {
                step.markCompleted(success, error);
            }
        }
    }

    public void markStepCompleted(int stepOrder, boolean success, String error) {
        for (StepResult step : steps) {
            if (step.getOrder() == stepOrder) {
                step.markCompleted(success, error);
                break;
            }
        }
    }

    // Getters
    public String getClassName() { return className; }
    public String getMethodName() { return methodName; }
    public int getOrder() { return order; }
    public String getTestName() { return testName; }
    public String getCategory() { return category; }
    public boolean isPassed() { return passed; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public String getErrorMessage() { return errorMessage; }
    public List<StepResult> getSteps() { return steps; }
    public Severity.Level getSeverity() { return severity; }
    public Priority.Level getPriority() { return priority; }
    public String getTestLevel() { return testLevel; }
    public String getTestType() { return testType; }
    public String getTestMethod() { return testMethod; }
    public String getAuthor() { return author; }
    public String getVersion() { return version; }
    public String getDescription() { return description; }

    // Setters only for fields that need to be modified
    public void setOrder(int order) { this.order = order; }
    public void setTestName(String testName) { this.testName = testName; }
    public void setCategory(String category) { this.category = category; }
    public void setSeverity(Severity.Level severity) { this.severity = severity; }
    public void setPriority(Priority.Level priority) { this.priority = priority; }
    public void setTestLevel(String testLevel) { this.testLevel = testLevel; }
    public void setTestType(String testType) { this.testType = testType; }
    public void setTestMethod(String testMethod) { this.testMethod = testMethod; }
    public void setAuthor(String author) { this.author = author; }
    public void setVersion(String version) { this.version = version; }
    public void setDescription(String description) { this.description = description; }
}