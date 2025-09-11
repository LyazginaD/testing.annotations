Testing Annotations Maven Plugin

A plugin for generating comprehensive test reports with support for custom annotations.
Features

    Generation of detailed reports in JSON format

    Support for custom annotations for test metadata

    Automatic scanning of test classes

    Generation of sample data for demonstration

    Flexible configuration via Maven parameters

    Support for JUnit and TestNG tests

    Detailed information about each test and execution steps

Annotations

The plugin supports the following annotations:
@TestCase

Main annotation for marking test methods:

    order - execution order (lower values execute first)

    name - human-readable test name

    category - test category

@Severity

Test severity level:

    CRITICAL, HIGH, MEDIUM, LOW, TRIVIAL

@Priority

Test execution priority:

    P0, P1, P2, P3, P4 (P0 - highest)

@TestLevel

Testing level:

    UNIT, INTEGRATION, SYSTEM, ACCEPTANCE

@TestType

Testing type:

    FUNCTIONAL, PERFORMANCE, SECURITY, USABILITY, COMPATIBILITY

@TestMethod

Testing methodology:

    BLACK_BOX, WHITE_BOX, GRAY_BOX, POSITIVE, NEGATIVE, BOUNDARY_VALUE, EQUIVALENCE_PARTITIONING, STATE_TRANSITION

@TestInfo

Additional test information:

    author - test author

    version - test version

    description - test description

@TestStep

Test step description (supports multiple usage):

    order - step order

    description - step description

Requirements

    Java 11 or higher

    Maven 3.6 or higher

Installation

Build and install the plugin to your local Maven repository:
bash

mvn clean install

Usage in Project
1. Adding Annotations Dependency

Add the dependency to the dependencies section of your pom.xml:
xml

<dependency>
    <groupId>io.github.lyazginad</groupId>
    <artifactId>testing-annotations</artifactId>
    <version>1.0.0</version>
</dependency>

2. Adding Plugin to Project

Add the plugin to the build/plugins section of your pom.xml:
xml

<build>
    <plugins>
        <plugin>
            <groupId>io.github.lyazginad</groupId>
            <artifactId>testing-annotations</artifactId>
            <version>1.0.0</version>
            <configuration>
                <!-- Optional settings -->
                <outputDirectory>${project.build.directory}/test-reports</outputDirectory>
                <reportFileName>custom-test-report.json</reportFileName>
                <prettyPrint>true</prettyPrint>
                <generateSampleData>false</generateSampleData>
                <scanTestClasses>true</scanTestClasses>
            </configuration>
        </plugin>
    </plugins>
</build>

3. Plugin Configuration Parameters

Plugin parameters:
Parameter	Default	Description
outputDirectory	${project.build.directory}/test-reports	Directory for report output
reportFileName	custom-test-report.json	Report file name
prettyPrint	true	JSON formatting for readability
generateSampleData	false	Generate sample data
scanTestClasses	true	Scan test classes
4. Example of Annotation Usage
   java

import io.github.lyazginad.testing.annotations.*;

public class ExampleTest {

    @TestCase(
        order = 1,
        name = "User Login Test",
        category = "authentication"
    )
    @Severity(Severity.Level.CRITICAL)
    @Priority(Priority.Level.P0)
    @TestLevel(TestLevel.Level.SYSTEM)
    @TestType(TestType.Type.FUNCTIONAL)
    @TestMethod(TestMethod.Method.BLACK_BOX)
    @TestInfo(
        author = "John Doe",
        description = "Tests user login functionality"
    )
    @TestSteps({
        @TestStep(order = 1, description = "Navigate to login page"),
        @TestStep(order = 2, description = "Enter valid credentials"),
        @TestStep(order = 3, description = "Click login button"),
        @TestStep(order = 4, description = "Verify successful login")
    })
    public void testUserLogin() {
        // Test implementation
    }
}

5. Running the Plugin

Execute the plugin using Maven:
bash

mvn io.github.lyazginad:testing-annotations:generate-test-report

Or, if the plugin is added to pom.xml:
bash

mvn testing-annotations:generate-test-report

Report Structure

The report includes the following information:
General Statistics

    Execution time

    Total test count

    Passed/failed tests

    Total duration

    Success rate

Test Details

    Class and method names

    Execution order

    Category

    Result (pass/fail)

    Start and end times

    Error message (if any)

    Test steps (if specified)

    Severity and priority levels

    Testing level, type, and methodology

    Author and version information

Distributions

    By severity levels

    By execution priorities

    By test categories

Sample Output

After execution, the plugin outputs detailed information to the console:
text

=== DETAILED TEST REPORT SUMMARY ===
Execution Time: 2023-01-01T12:00:00
Total Tests: 10
Passed: 8
Failed: 2
Success Rate: 80.00%
Total Duration: 12345ms
--- Severity Distribution ---
CRITICAL: 2 tests
HIGH: 3 tests
MEDIUM: 5 tests
--- Priority Distribution ---
P0: 2 tests
P1: 3 tests
P2: 5 tests
--- Category Distribution ---
authentication: 3 tests
payment: 2 tests
performance: 5 tests

Advanced Features
Sample Data Generation

To demonstrate plugin functionality without real tests, enable sample data generation:
xml

<configuration>
    <generateSampleData>true</generateSampleData>
</configuration>

CI/CD Integration

The plugin can be integrated into CI/CD pipelines for automatic report generation after test execution.
Custom Result Processing

JSON reports can be further processed by other tools for visualization or analysis.
Support

For usage questions and issues, please refer to the project's Issues section.
License

This project is licensed under the MIT License - see the LICENSE file for details.