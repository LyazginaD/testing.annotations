package io.github.lyazginad.testing.mojo;

import io.github.lyazginad.testing.annotations.*;
import io.github.lyazginad.testing.model.TestReport;
import io.github.lyazginad.testing.model.TestResult;
import io.github.lyazginad.testing.model.StepResult;
import io.github.lyazginad.testing.util.AnnotationProcessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.handler.ArtifactHandler;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Maven Mojo for generating comprehensive test reports with custom annotations
 */
@Mojo(name = "generate-report", defaultPhase = LifecyclePhase.TEST)
public class TestingReportMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project.build.directory}", required = true, readonly = true)
    private String buildDirectory;

    @Parameter(defaultValue = "${project.build.testOutputDirectory}", required = true, readonly = true)
    private String testOutputDirectory;

    @Parameter(defaultValue = "${project.artifacts}", required = true, readonly = true)
    private Set<Artifact> artifacts;

    @Parameter(property = "outputDirectory", defaultValue = "${project.build.directory}/test-reports")
    private String outputDirectory;

    @Parameter(property = "reportFileName", defaultValue = "custom-test-report.json")
    private String reportFileName;

    @Parameter(property = "prettyPrint", defaultValue = "true")
    private boolean prettyPrint;

    @Parameter(property = "generateSampleData", defaultValue = "false")
    private boolean generateSampleData;

    @Parameter(property = "scanTestClasses", defaultValue = "true")
    private boolean scanTestClasses;

    @Parameter(property = "strictScanning", defaultValue = "true")
    private boolean strictScanning;

    @Override
    public void execute() throws MojoExecutionException {
        getLog().info("Generating custom test report with annotations...");

        try {
            // Create output directory if it doesn't exist
            Path outputPath = Paths.get(outputDirectory);
            if (!Files.exists(outputPath)) {
                Files.createDirectories(outputPath);
                getLog().info("Created output directory: " + outputDirectory);
            }

            // Generate test report
            TestReport testReport = generateTestReport();

            // Write report to file
            writeReportToFile(testReport);

            // Log report summary with detailed information
            logDetailedReportSummary(testReport);

        } catch (Exception e) {
            throw new MojoExecutionException("Failed to generate test report", e);
        }
    }

    private TestReport generateTestReport() throws Exception {
        getLog().info("Generating test report...");

        TestReport report = new TestReport();

        if (generateSampleData) {
            getLog().info("Generating sample test data...");
            generateComprehensiveSampleData(report);
        }

        if (scanTestClasses) {
            getLog().info("Scanning for test classes...");
            scanTestClasses(report);
        }

        return report;
    }

    private void scanTestClasses(TestReport report) throws Exception {
        List<URL> testClassUrls = new ArrayList<>();

        // Add test classes directory
        File testClassesDir = new File(testOutputDirectory);
        if (testClassesDir.exists()) {
            testClassUrls.add(testClassesDir.toURI().toURL());
            getLog().info("Added test classes directory: " + testOutputDirectory);
        } else {
            getLog().warn("Test classes directory does not exist: " + testOutputDirectory);
        }

        // Add project dependencies
        if (artifacts != null) {
            for (Artifact artifact : artifacts) {
                ArtifactHandler handler = artifact.getArtifactHandler();
                if (handler != null && handler.isAddedToClasspath()) {
                    File artifactFile = artifact.getFile();
                    if (artifactFile != null && artifactFile.exists()) {
                        testClassUrls.add(artifactFile.toURI().toURL());
                        getLog().debug("Added artifact to classpath: " + artifactFile.getName());
                    }
                }
            }
        }

        if (testClassUrls.isEmpty()) {
            getLog().warn("No classpath URLs found for scanning");
            return;
        }

        URLClassLoader classLoader = new URLClassLoader(
                testClassUrls.toArray(new URL[0]),
                Thread.currentThread().getContextClassLoader()
        );

        // Scan test classes directory
        scanDirectoryForTests(new File(testOutputDirectory), report, classLoader);
        getLog().info("Scanning for test classes with custom annotations only");
    }

    private void scanDirectoryForTests(File directory, TestReport report, ClassLoader classLoader) {
        if (!directory.exists()) {
            getLog().warn("Test directory does not exist: " + directory.getAbsolutePath());
            return;
        }

        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        scanDirectoryForTests(file, report, classLoader);
                    } else if (file.getName().endsWith(".class")) {
                        try {
                            processClassFile(file, report, classLoader);
                        } catch (Exception e) {
                            getLog().warn("Failed to process class file: " + file.getName(), e);
                        }
                    }
                }
            }
        }
    }

    private void processClassFile(File classFile, TestReport report, ClassLoader classLoader) {
        String className = getClassNameFromFile(classFile, new File(testOutputDirectory));

        try {
            // Trying to load the class, but not handling loading errors
            Class<?> clazz = classLoader.loadClass(className);
            processTestClass(clazz, report);
        } catch (ClassNotFoundException e) {
            getLog().debug("Class not found: " + className);
        } catch (NoClassDefFoundError e) {
            // Ignoring classes with missing dependencies
            getLog().debug("Skipping class " + className + " due to missing dependencies: " + e.getMessage());
        } catch (Exception e) {
            getLog().warn("Failed to process class: " + className, e);
        }
    }

    private String getClassNameFromFile(File classFile, File baseDir) {
        String basePath = baseDir.getAbsolutePath();
        String filePath = classFile.getAbsolutePath();

        if (filePath.startsWith(basePath)) {
            String relativePath = filePath.substring(basePath.length() + 1);
            return relativePath.replace(File.separatorChar, '.').replace(".class", "");
        }
        return classFile.getName().replace(".class", "");
    }

    private void processTestClass(Class<?> clazz, TestReport report) {
        for (Method method : clazz.getDeclaredMethods()) {
            // Check only methods with lyazginad.testing annotations
            if (hasTestingAnnotations(method)) {
                try {
                    TestResult testResult = new TestResult(
                            clazz.getName(),
                            method.getName(),
                            Integer.MAX_VALUE,
                            method.getName()
                    );

                    AnnotationProcessor.processTestAnnotations(method, testResult);
                    testResult.markCompleted(true, null);

                    report.addTestResult(testResult);
                    getLog().info("Found annotated test method: " + clazz.getName() + "." + method.getName());

                } catch (Exception e) {
                    getLog().warn("Failed to process test method: " + method.getName(), e);
                }
            }
        }
    }

    // Остальные методы остаются без изменений...
    private void generateComprehensiveSampleData(TestReport report) {
        getLog().info("Generating comprehensive sample test data...");

        // Test with all annotation types
        TestResult comprehensiveTest = createComprehensiveTest();
        report.addTestResult(comprehensiveTest);

        // Tests demonstrating different TestLevel values
        TestResult unitTest = createSampleTest(
                "com.example.UnitTest", "testUnitLogic", 1, "Unit Logic Test",
                "unit", io.github.lyazginad.testing.annotations.Severity.Level.LOW,
                io.github.lyazginad.testing.annotations.Priority.Level.P3,
                "UNIT", "FUNCTIONAL", "WHITE_BOX", true, null
        );
        report.addTestResult(unitTest);

        TestResult integrationTest = createSampleTest(
                "com.example.IntegrationTest", "testIntegration", 2, "Integration Test",
                "integration", io.github.lyazginad.testing.annotations.Severity.Level.MEDIUM,
                io.github.lyazginad.testing.annotations.Priority.Level.P2,
                "INTEGRATION", "FUNCTIONAL", "BLACK_BOX", true, null
        );
        report.addTestResult(integrationTest);

        TestResult systemTest = createSampleTest(
                "com.example.SystemTest", "testSystem", 3, "System Test",
                "system", io.github.lyazginad.testing.annotations.Severity.Level.HIGH,
                io.github.lyazginad.testing.annotations.Priority.Level.P1,
                "SYSTEM", "PERFORMANCE", "GRAY_BOX", false, "System timeout"
        );
        report.addTestResult(systemTest);

        TestResult acceptanceTest = createSampleTest(
                "com.example.AcceptanceTest", "testAcceptance", 4, "Acceptance Test",
                "acceptance", io.github.lyazginad.testing.annotations.Severity.Level.CRITICAL,
                io.github.lyazginad.testing.annotations.Priority.Level.P0,
                "ACCEPTANCE", "USABILITY", "BLACK_BOX", true, null
        );
        report.addTestResult(acceptanceTest);

        // Tests demonstrating different TestMethod values
        TestResult positiveTest = createSampleTest(
                "com.example.PositiveTest", "testPositiveScenario", 5, "Positive Test",
                "validation", io.github.lyazginad.testing.annotations.Severity.Level.MEDIUM,
                io.github.lyazginad.testing.annotations.Priority.Level.P2,
                "UNIT", "FUNCTIONAL", "POSITIVE", true, null
        );
        report.addTestResult(positiveTest);

        TestResult negativeTest = createSampleTest(
                "com.example.NegativeTest", "testNegativeScenario", 6, "Negative Test",
                "validation", io.github.lyazginad.testing.annotations.Severity.Level.MEDIUM,
                io.github.lyazginad.testing.annotations.Priority.Level.P2,
                "UNIT", "FUNCTIONAL", "NEGATIVE", true, null
        );
        report.addTestResult(negativeTest);

        TestResult boundaryTest = createSampleTest(
                "com.example.BoundaryTest", "testBoundaryValues", 7, "Boundary Value Test",
                "validation", io.github.lyazginad.testing.annotations.Severity.Level.MEDIUM,
                io.github.lyazginad.testing.annotations.Priority.Level.P2,
                "UNIT", "FUNCTIONAL", "BOUNDARY_VALUE", false, "Boundary condition failed"
        );
        report.addTestResult(boundaryTest);

        // Test demonstrating COMPATIBILITY type
        TestResult compatibilityTest = createSampleTest(
                "com.example.CompatibilityTest", "testCompatibility", 8, "Compatibility Test",
                "compatibility", io.github.lyazginad.testing.annotations.Severity.Level.MEDIUM,
                io.github.lyazginad.testing.annotations.Priority.Level.P2,
                "SYSTEM", "COMPATIBILITY", "BLACK_BOX", true, null
        );
        report.addTestResult(compatibilityTest);
    }

    private TestResult createComprehensiveTest() {
        TestResult test = new TestResult(
                "com.example.ComprehensiveTest",
                "testComprehensiveScenario",
                0,
                "Comprehensive Test Scenario"
        );

        configureComprehensiveTest(test);
        addTestSteps(test);
        completeTestWithSteps(test);

        return test;
    }

    private void configureComprehensiveTest(TestResult test) {
        test.setCategory("comprehensive");
        test.setSeverity(io.github.lyazginad.testing.annotations.Severity.Level.CRITICAL);
        test.setPriority(io.github.lyazginad.testing.annotations.Priority.Level.P0);
        test.setTestLevel("SYSTEM");
        test.setTestType("SECURITY");
        test.setTestMethod("STATE_TRANSITION");
        test.setAuthor("Test Engineer");
        test.setVersion("2.0");
        test.setDescription("A comprehensive test demonstrating all annotation types");
    }

    private void addTestSteps(TestResult test) {
        test.addStep(new StepResult(1, "Initialize test environment"));
        test.addStep(new StepResult(2, "Execute security checks"));
        test.addStep(new StepResult(3, "Validate state transitions"));
        test.addStep(new StepResult(4, "Verify results"));
    }

    private void completeTestWithSteps(TestResult test) {
        test.markCompleted(true, null);

        // Mark some steps with different outcomes
        test.markStepCompleted(1, true, null);
        test.markStepCompleted(2, true, null);
        test.markStepCompleted(3, false, "State transition validation failed");
        test.markStepCompleted(4, true, null);
    }

    private TestResult createSampleTest(String className, String methodName, int order,
                                        String testName, String category,
                                        io.github.lyazginad.testing.annotations.Severity.Level severity,
                                        io.github.lyazginad.testing.annotations.Priority.Level priority,
                                        String testLevel, String testType, String testMethod,
                                        boolean passed, String errorMessage) {
        TestResult testResult = new TestResult(className, methodName, order, testName);
        testResult.setCategory(category);
        testResult.setSeverity(severity);
        testResult.setPriority(priority);
        testResult.setTestLevel(testLevel);
        testResult.setTestType(testType);
        testResult.setTestMethod(testMethod);

        testResult.markCompleted(passed, errorMessage);

        return testResult;
    }

    private void writeReportToFile(TestReport testReport) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        if (prettyPrint) {
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        }

        File outputFile = new File(outputDirectory, reportFileName);
        objectMapper.writeValue(outputFile, testReport);

        getLog().info("Report written to: " + outputFile.getAbsolutePath());
    }

    private void logDetailedReportSummary(TestReport report) {
        getLog().info("=== DETAILED TEST REPORT SUMMARY ===");
        getLog().info("Execution Time: " + report.getExecutionTime());
        getLog().info("Total Tests: " + report.getTotalTests());
        getLog().info("Passed: " + report.getPassedTests());
        getLog().info("Failed: " + report.getFailedTests());
        getLog().info("Success Rate: " + String.format("%.2f", report.getSuccessRate()) + "%");
        getLog().info("Total Duration: " + report.getTotalDuration() + "ms");

        getLog().info("--- Severity Distribution ---");
        report.getSeveritySummary().forEach((severity, count) ->
                getLog().info("  " + severity + ": " + count + " tests"));

        getLog().info("--- Priority Distribution ---");
        report.getPrioritySummary().forEach((priority, count) ->
                getLog().info("  " + priority + ": " + count + " tests"));

        getLog().info("--- Category Distribution ---");
        report.getCategorySummary().forEach((category, count) ->
                getLog().info("  " + category + ": " + count + " tests"));

        // Log individual test details
        getLog().info("--- Test Details ---");
        for (TestResult result : report.getTestResults()) {
            getLog().info("  " + result.getTestName() +
                    " - " + (result.isPassed() ? "PASS" : "FAIL") +
                    " - Severity: " + result.getSeverity() +
                    " - Level: " + result.getTestLevel() +
                    " - Type: " + result.getTestType() +
                    " - Method: " + result.getTestMethod());

            if (!result.getSteps().isEmpty()) {
                getLog().info("    Steps: " + result.getSteps().size());
                for (StepResult step : result.getSteps()) {
                    getLog().info("      Step " + step.getOrder() + ": " +
                            step.getDescription() + " - " +
                            (step.isPassed() ? "PASS" : "FAIL"));
                }
            }
        }
    }

    private boolean hasTestingAnnotations(Method method) {
        return method.isAnnotationPresent(TestCase.class) ||
                method.isAnnotationPresent(Severity.class) ||
                method.isAnnotationPresent(Priority.class) ||
                method.isAnnotationPresent(TestLevel.class) ||
                method.isAnnotationPresent(TestType.class) ||
                method.isAnnotationPresent(TestMethod.class) ||
                method.isAnnotationPresent(TestInfo.class) ||
                method.isAnnotationPresent(TestStep.class) ||
                method.isAnnotationPresent(TestSteps.class);
    }
}