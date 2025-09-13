package io.github.lyazginad.testing.util;

import io.github.lyazginad.testing.annotations.*;
import io.github.lyazginad.testing.model.TestResult;
import io.github.lyazginad.testing.model.StepResult;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

/**
 * Utility class for processing custom testing annotations
 */
public class AnnotationProcessor {

    public static void processTestAnnotations(Method method, TestResult testResult) {
        // Process @TestCase annotation
        if (method.isAnnotationPresent(TestCase.class)) {
            TestCase testCase = method.getAnnotation(TestCase.class);
            testResult.setOrder(testCase.order());
            testResult.setTestName(testCase.name().isEmpty() ? method.getName() : testCase.name());
            testResult.setCategory(testCase.category());
        }

        // Process @Severity annotation
        if (method.isAnnotationPresent(Severity.class)) {
            Severity severity = method.getAnnotation(Severity.class);
            testResult.setSeverity(severity.value());
        }

        // Process @Priority annotation
        if (method.isAnnotationPresent(Priority.class)) {
            Priority priority = method.getAnnotation(Priority.class);
            testResult.setPriority(priority.value());
        }

        // Process @TestLevel annotation
        if (method.isAnnotationPresent(TestLevel.class)) {
            TestLevel testLevel = method.getAnnotation(TestLevel.class);
            testResult.setTestLevel(testLevel.value().name());
        }

        // Process @TestType annotation
        if (method.isAnnotationPresent(TestType.class)) {
            TestType testType = method.getAnnotation(TestType.class);
            testResult.setTestType(testType.value().name());
        }

        // Process @TestMethod annotation
        if (method.isAnnotationPresent(TestMethod.class)) {
            TestMethod testMethod = method.getAnnotation(TestMethod.class);
            testResult.setTestMethod(testMethod.value().name());
        }

        // Process @TestInfo annotation
        if (method.isAnnotationPresent(TestInfo.class)) {
            TestInfo testInfo = method.getAnnotation(TestInfo.class);
            testResult.setAuthor(testInfo.author());
            testResult.setVersion(testInfo.version());
            testResult.setDescription(testInfo.description());
        }

        // Process @TestStep annotations
        if (method.isAnnotationPresent(TestStep.class)) {
            TestStep testStep = method.getAnnotation(TestStep.class);
            StepResult stepResult = new StepResult(testStep.order(), testStep.description());
            testResult.addStep(stepResult);
        }

        // Process multiple @TestStep annotations
        if (method.isAnnotationPresent(TestSteps.class)) {
            TestSteps testSteps = method.getAnnotation(TestSteps.class);
            for (TestStep step : testSteps.value()) {
                StepResult stepResult = new StepResult(step.order(), step.description());
                testResult.addStep(stepResult);
            }
        }
    }

    public static boolean isTestMethod(Method method) {
        return method.isAnnotationPresent(TestCase.class) ||
                isJUnitTest(method) ||
                isTestNGTest(method) ||
                method.isAnnotationPresent(TestLevel.class) ||
                method.isAnnotationPresent(TestType.class) ||
                method.isAnnotationPresent(TestMethod.class);
    }

    private static boolean isJUnitTest(Method method) {
        try {
            @SuppressWarnings("unchecked")
            Class<? extends java.lang.annotation.Annotation> junitTest =
                    (Class<? extends java.lang.annotation.Annotation>) Class.forName("org.junit.Test");
            return method.isAnnotationPresent(junitTest);
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private static boolean isTestNGTest(Method method) {
        try {
            @SuppressWarnings("unchecked")
            Class<? extends java.lang.annotation.Annotation> testngTest =
                    (Class<? extends java.lang.annotation.Annotation>) Class.forName("org.testng.annotations.Test");
            return method.isAnnotationPresent(testngTest);
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static List<TestStep> getTestSteps(Method method) {
        List<TestStep> steps = new ArrayList<>();

        if (method.isAnnotationPresent(TestStep.class)) {
            steps.add(method.getAnnotation(TestStep.class));
        }

        if (method.isAnnotationPresent(TestSteps.class)) {
            steps.addAll(Arrays.asList(method.getAnnotation(TestSteps.class).value()));
        }

        return steps;
    }
}