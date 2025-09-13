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
import org.apache.maven.project.MavenProject;

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

/**
 * Maven Mojo для генерации тестовых отчетов
 */
@Mojo(name = "generate-report", defaultPhase = LifecyclePhase.TEST, threadSafe = true)
public class TestingReportMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;

    @Parameter(defaultValue = "${project.build.directory}", required = true, readonly = true)
    private String buildDirectory;

    @Parameter(defaultValue = "${project.build.testOutputDirectory}", required = true, readonly = true)
    private String testOutputDirectory;

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

    @Override
    public void execute() throws MojoExecutionException {
        getLog().info("Generating custom test report with annotations...");

        try {
            // Создаем выходную директорию
            Path outputPath = Paths.get(outputDirectory);
            if (!Files.exists(outputPath)) {
                Files.createDirectories(outputPath);
                getLog().info("Created output directory: " + outputDirectory);
            }

            // Генерируем отчет
            TestReport testReport = generateTestReport();

            // Записываем отчет в файл
            writeReportToFile(testReport);

            // Логируем summary
            logReportSummary(testReport);

        } catch (Exception e) {
            throw new MojoExecutionException("Failed to generate test report", e);
        }
    }

    private TestReport generateTestReport() throws Exception {
        TestReport report = new TestReport();

        if (generateSampleData) {
            getLog().info("Generating sample test data...");
            generateSampleData(report);
        }

        if (scanTestClasses) {
            getLog().info("Scanning for test classes...");
            scanTestClasses(report);
        }

        return report;
    }

    private void scanTestClasses(TestReport report) throws Exception {
        File testClassesDir = new File(testOutputDirectory);

        if (!testClassesDir.exists()) {
            getLog().warn("Test classes directory does not exist: " + testOutputDirectory);
            return;
        }

        // Создаем classloader для загрузки тестовых классов
        URL[] urls = new URL[] { testClassesDir.toURI().toURL() };
        try (URLClassLoader classLoader = new URLClassLoader(urls, getClass().getClassLoader())) {
            scanDirectoryForTests(testClassesDir, report, classLoader);
        }
    }

    private void scanDirectoryForTests(File directory, TestReport report, ClassLoader classLoader) {
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        scanDirectoryForTests(file, report, classLoader);
                    } else if (file.getName().endsWith(".class")) {
                        processClassFile(file, report, classLoader);
                    }
                }
            }
        }
    }

    private void processClassFile(File classFile, TestReport report, ClassLoader classLoader) {
        String className = getClassNameFromFile(classFile, new File(testOutputDirectory));

        try {
            Class<?> clazz = classLoader.loadClass(className);
            processTestClass(clazz, report);
        } catch (ClassNotFoundException e) {
            getLog().debug("Class not found: " + className);
        } catch (NoClassDefFoundError e) {
            getLog().debug("Skipping class " + className + " due to missing dependencies");
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
            if (hasTestingAnnotations(method)) {
                try {
                    TestResult testResult = new TestResult(
                            clazz.getName(),
                            method.getName(),
                            report.getTotalTests() + 1,
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

    private void generateSampleData(TestReport report) {
        // Простые sample data без сложных зависимостей
        TestResult test1 = new TestResult("com.example.SampleTest", "testSample1", 1, "Sample Test 1");
        test1.setCategory("sample");
        test1.markCompleted(true, null);
        report.addTestResult(test1);

        TestResult test2 = new TestResult("com.example.SampleTest", "testSample2", 2, "Sample Test 2");
        test2.setCategory("sample");
        test2.markCompleted(false, "Sample failure");
        report.addTestResult(test2);
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

    private void logReportSummary(TestReport report) {
        getLog().info("=== TEST REPORT SUMMARY ===");
        getLog().info("Total Tests: " + report.getTotalTests());
        getLog().info("Passed: " + report.getPassedTests());
        getLog().info("Failed: " + report.getFailedTests());
        getLog().info("Success Rate: " + String.format("%.2f", report.getSuccessRate()) + "%");
    }
}