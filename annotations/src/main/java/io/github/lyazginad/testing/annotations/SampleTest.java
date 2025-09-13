package io.github.lyazginad.testing.annotations;

public class SampleTest {

    @TestCase(order = 1, name = "User Login Test", category = "authentication")
    @Severity(Severity.Level.CRITICAL)
    @Priority(Priority.Level.P0)
    @TestLevel(TestLevel.Level.SYSTEM)
    @TestType(TestType.Type.FUNCTIONAL)
    @TestMethod(TestMethod.Method.BLACK_BOX)
    @TestInfo(author = "John Doe", description = "Tests user login functionality")
    @TestSteps({
            @TestStep(order = 1, description = "Navigate to login page"),
            @TestStep(order = 2, description = "Enter valid credentials"),
            @TestStep(order = 3, description = "Click login button"),
            @TestStep(order = 4, description = "Verify successful login")
    })
    public void testUserLogin() {
        // Test implementation would go here
    }

    @TestCase(order = 2, name = "Payment Processing Test", category = "payment")
    @Severity(Severity.Level.HIGH)
    @Priority(Priority.Level.P1)
    @TestLevel(TestLevel.Level.INTEGRATION)
    @TestType(TestType.Type.SECURITY)
    @TestMethod(TestMethod.Method.WHITE_BOX)
    public void testPaymentProcessing() {
        // Test implementation would go here
    }

    @TestCase(order = 3, name = "Performance Load Test", category = "performance")
    @Severity(Severity.Level.MEDIUM)
    @Priority(Priority.Level.P2)
    @TestLevel(TestLevel.Level.ACCEPTANCE)
    @TestType(TestType.Type.PERFORMANCE)
    public void testPerformance() {
        // Test implementation would go here
    }
}