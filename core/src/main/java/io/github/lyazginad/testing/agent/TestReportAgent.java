package io.github.lyazginad.testing.agent;

import io.github.lyazginad.testing.model.TestReport;
import io.github.lyazginad.testing.model.TestResult;
import io.github.lyazginad.testing.util.AnnotationProcessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

import java.io.File;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.ProtectionDomain;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Java Agent for automatic test execution tracking and report generation
 */
public class TestReportAgent {

    private static final String OUTPUT_DIRECTORY = "target/test-reports";
    private static final String REPORT_FILE_NAME = "custom-test-report.json";
    private static final boolean PRETTY_PRINT = true;

    private static TestReport testReport = new TestReport();
    private static Map<String, TestResult> currentTestResults = new ConcurrentHashMap<>();
    private static Map<String, LocalDateTime> testStartTimes = new ConcurrentHashMap<>();
    private static Instrumentation instrumentation;

    /**
     * Premain method for agent startup
     */
    public static void premain(String args, Instrumentation inst) {
        System.out.println("=== Test Report Agent Initialized ===");
        instrumentation = inst;

        // Register class transformer
        TestTransformer transformer = new TestTransformer();
        inst.addTransformer(transformer, true);

        // Add shutdown hook for report generation on termination
        Runtime.getRuntime().addShutdownHook(new Thread(TestReportAgent::generateFinalReport));

        System.out.println("Agent successfully registered. Ready to track tests.");
    }

    /**
     * Agentmain method for dynamic attachment
     */
    public static void agentmain(String args, Instrumentation inst) {
        System.out.println("Test Report Agent attached at runtime");
        premain(args, inst);
    }

    /**
     * Class transformer for intercepting test methods
     */
    private static class TestTransformer implements ClassFileTransformer {

        @Override
        public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                                ProtectionDomain protectionDomain, byte[] classfileBuffer) {

            try {
                String dotClassName = className.replace('/', '.');

                // Transform only test classes
                if (dotClassName.contains("Test") && !dotClassName.contains("$")) {
                    ClassPool cp = ClassPool.getDefault();
                    CtClass ctClass = cp.makeClass(new java.io.ByteArrayInputStream(classfileBuffer));

                    boolean transformed = false;

                    // Iterate through all class methods
                    for (CtMethod method : ctClass.getDeclaredMethods()) {
                        if (isTestMethod(method)) {
                            transformTestMethod(method);
                            transformed = true;
                        }
                    }

                    if (transformed) {
                        System.out.println("Transformed test class: " + dotClassName);
                        return ctClass.toBytecode();
                    }
                }
            } catch (Exception e) {
                System.err.println("Error transforming class " + className + ": " + e.getMessage());
            }

            return null; // Return null if transformation is not required
        }

        private boolean isTestMethod(CtMethod method) throws NotFoundException {
            String methodName = method.getName();
            return methodName.startsWith("test") ||
                    methodName.contains("Test") ||
                    hasTestAnnotations(method);
        }

        private boolean hasTestAnnotations(CtMethod method) {
            try {
                Object[] annotations = method.getAnnotations();
                for (Object ann : annotations) {
                    String annStr = ann.toString();
                    if (annStr.contains("Test") || annStr.contains("org.junit") || annStr.contains("org.testng")) {
                        return true;
                    }
                }
            } catch (Exception e) {
                // Ignore annotation retrieval errors
            }
            return false;
        }

        private void transformTestMethod(CtMethod method) {
            try {
                String methodName = method.getName();
                String className = method.getDeclaringClass().getName();

                // Add logic before test execution
                method.insertBefore(
                        "io.github.lyazginad.testing.agent.TestReportAgent.testStarted(\"" +
                                className + "#" + methodName + "\", \"" + className + "\", \"" + methodName + "\");"
                );

                // Add logic after successful test execution
                method.insertAfter(
                        "io.github.lyazginad.testing.agent.TestReportAgent.testFinished(\"" +
                                className + "#" + methodName + "\", true, null);",
                        true
                );

                // Add logic for exception interception
                method.addCatch(
                        "{" +
                                "   io.github.lyazginad.testing.agent.TestReportAgent.testFinished(\"" +
                                className + "#" + methodName + "\", false, $e.getMessage());" +
                                "   throw $e;" +
                                "}",
                        ClassPool.getDefault().get("java.lang.Exception")
                );

            } catch (Exception e) {
                System.err.println("Error transforming method " + method.getName() + ": " + e.getMessage());
            }
        }
    }

    /**
     * Called when test execution starts
     */
    public static void testStarted(String testId, String className, String methodName) {
        try {
            TestResult testResult = new TestResult(className, methodName,
                    testReport.getTotalTests() + 1,
                    methodName);

            // Try to get annotations via reflection
            try {
                Class<?> testClass = Class.forName(className);
                Method testMethod = testClass.getDeclaredMethod(methodName);
                AnnotationProcessor.processTestAnnotations(testMethod, testResult);
            } catch (Exception e) {
                // If unable to get annotations, use default values
                System.out.println("Note: Could not process annotations for " + testId + ": " + e.getMessage());
            }

            currentTestResults.put(testId, testResult);
            testStartTimes.put(testId, LocalDateTime.now());

            System.out.println("🔵 Test started: " + testId);

        } catch (Exception e) {
            System.err.println("Error in testStarted for " + testId + ": " + e.getMessage());
        }
    }

    /**
     * Called when test execution completes
     */
    public static void testFinished(String testId, boolean success, String errorMessage) {
        try {
            TestResult testResult = currentTestResults.get(testId);
            if (testResult != null) {
                testResult.markCompleted(success, errorMessage);
                testReport.addTestResult(testResult);

                currentTestResults.remove(testId);
                testStartTimes.remove(testId);

                String status = success ? "✅ PASS" : "❌ FAIL";
                System.out.println(status + " Test finished: " + testId);

                if (!success && errorMessage != null) {
                    System.out.println("   Error: " + errorMessage);
                }
            }
        } catch (Exception e) {
            System.err.println("Error in testFinished for " + testId + ": " + e.getMessage());
        }
    }

    /**
     * Marks test step completion
     */
    public static void testStepCompleted(String testId, int stepOrder, boolean success, String error) {
        try {
            TestResult testResult = currentTestResults.get(testId);
            if (testResult != null) {
                testResult.markStepCompleted(stepOrder, success, error);
            }
        } catch (Exception e) {
            System.err.println("Error in testStepCompleted for " + testId + ": " + e.getMessage());
        }
    }

    /**
     * Generates final report
     */
    private static void generateFinalReport() {
        try {
            // Complete all unfinished tests
            for (String testId : currentTestResults.keySet()) {
                testFinished(testId, false, "Test did not complete properly");
            }

            // Create report directory
            Path outputPath = Paths.get(OUTPUT_DIRECTORY);
            if (!Files.exists(outputPath)) {
                Files.createDirectories(outputPath);
                System.out.println("Created output directory: " + OUTPUT_DIRECTORY);
            }

            // Write report to file
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());

            if (PRETTY_PRINT) {
                objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
            }

            File outputFile = new File(OUTPUT_DIRECTORY, REPORT_FILE_NAME);
            objectMapper.writeValue(outputFile, testReport);

            System.out.println("📊 Report generated: " + outputFile.getAbsolutePath());
            printSummary();

        } catch (Exception e) {
            System.err.println("Error generating final report: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Prints test execution summary
     */
    private static void printSummary() {
        System.out.println("\n=== TEST EXECUTION SUMMARY ===");
        System.out.println("Total Tests: " + testReport.getTotalTests());
        System.out.println("Passed: " + testReport.getPassedTests());
        System.out.println("Failed: " + testReport.getFailedTests());
        System.out.println("Success Rate: " + String.format("%.2f", testReport.getSuccessRate()) + "%");
        System.out.println("Total Duration: " + testReport.getTotalDuration() + "ms");

        if (!testReport.getSeveritySummary().isEmpty()) {
            System.out.println("--- Severity Distribution ---");
            testReport.getSeveritySummary().forEach((severity, count) ->
                    System.out.println("  " + severity + ": " + count + " tests"));
        }

        if (!testReport.getPrioritySummary().isEmpty()) {
            System.out.println("--- Priority Distribution ---");
            testReport.getPrioritySummary().forEach((priority, count) ->
                    System.out.println("  " + priority + ": " + count + " tests"));
        }

        if (!testReport.getCategorySummary().isEmpty()) {
            System.out.println("--- Category Distribution ---");
            testReport.getCategorySummary().forEach((category, count) ->
                    System.out.println("  " + category + ": " + count + " tests"));
        }
    }

    /**
     * Returns current report (for external use)
     */
    public static TestReport getCurrentReport() {
        return testReport;
    }

    /**
     * Resets current report (for testing)
     */
    public static void resetReport() {
        testReport = new TestReport();
        currentTestResults.clear();
        testStartTimes.clear();
    }

    /**
     * Utility for manual test result addition
     */
    public static void addManualTestResult(TestResult testResult) {
        testReport.addTestResult(testResult);
    }
}