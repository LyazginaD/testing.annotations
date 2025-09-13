package io.github.lyazginad.testing.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Comprehensive test execution report containing all test results
 */
public class TestReport {
    private LocalDateTime executionTime;
    private int totalTests;
    private int passedTests;
    private int failedTests;
    private long totalDuration;
    private List<TestResult> testResults = new ArrayList<>();
    private Map<String, Integer> severitySummary = new HashMap<>();
    private Map<String, Integer> prioritySummary = new HashMap<>();
    private Map<String, Integer> categorySummary = new HashMap<>();

    public TestReport() {
        this.executionTime = LocalDateTime.now();
        initializeSummaries();
    }

    private void initializeSummaries() {
        // Initialize severity summary
        for (io.github.lyazginad.testing.annotations.Severity.Level level :
                io.github.lyazginad.testing.annotations.Severity.Level.values()) {
            severitySummary.put(level.name(), 0);
        }

        // Initialize priority summary
        for (io.github.lyazginad.testing.annotations.Priority.Level level :
                io.github.lyazginad.testing.annotations.Priority.Level.values()) {
            prioritySummary.put(level.name(), 0);
        }
    }

    public void addTestResult(TestResult result) {
        testResults.add(result);
        totalTests++;

        if (result.isPassed()) {
            passedTests++;
        } else {
            failedTests++;
        }

        totalDuration += result.getDuration();

        // Update severity summary
        String severityKey = result.getSeverity().name();
        severitySummary.put(severityKey, severitySummary.getOrDefault(severityKey, 0) + 1);

        // Update priority summary
        String priorityKey = result.getPriority().name();
        prioritySummary.put(priorityKey, prioritySummary.getOrDefault(priorityKey, 0) + 1);

        // Update category summary
        String categoryKey = result.getCategory();
        categorySummary.put(categoryKey, categorySummary.getOrDefault(categoryKey, 0) + 1);
    }

    public double getSuccessRate() {
        if (totalTests == 0) return 0.0;
        return (double) passedTests / totalTests * 100;
    }

    // Getters and setters
    public LocalDateTime getExecutionTime() { return executionTime; }
    public void setExecutionTime(LocalDateTime executionTime) { this.executionTime = executionTime; }
    public int getTotalTests() { return totalTests; }
    public void setTotalTests(int totalTests) { this.totalTests = totalTests; }
    public int getPassedTests() { return passedTests; }
    public void setPassedTests(int passedTests) { this.passedTests = passedTests; }
    public int getFailedTests() { return failedTests; }
    public void setFailedTests(int failedTests) { this.failedTests = failedTests; }
    public long getTotalDuration() { return totalDuration; }
    public void setTotalDuration(long totalDuration) { this.totalDuration = totalDuration; }
    public List<TestResult> getTestResults() { return testResults; }
    public void setTestResults(List<TestResult> testResults) { this.testResults = testResults; }
    public Map<String, Integer> getSeveritySummary() { return severitySummary; }
    public void setSeveritySummary(Map<String, Integer> severitySummary) { this.severitySummary = severitySummary; }
    public Map<String, Integer> getPrioritySummary() { return prioritySummary; }
    public void setPrioritySummary(Map<String, Integer> prioritySummary) { this.prioritySummary = prioritySummary; }
    public Map<String, Integer> getCategorySummary() { return categorySummary; }
    public void setCategorySummary(Map<String, Integer> categorySummary) { this.categorySummary = categorySummary; }
}