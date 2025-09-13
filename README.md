Testing Annotations Maven Plugin

A comprehensive Maven plugin for generating detailed test reports with support for custom annotations and advanced test metadata.
Project Structure

The project consists of three main modules:

    testing-annotations: Contains custom annotations for test metadata

    testing-core: Core classes for test reporting and processing

    testing-annotations-maven-plugin: Maven plugin implementation for report generation

Features

    Detailed JSON Reports: Comprehensive test execution reports in JSON format

    Custom Annotations: Rich set of annotations for test metadata

    Automatic Scanning: Automatic discovery of test classes with custom annotations

    Sample Data Generation: Option to generate sample data for demonstration

    Flexible Configuration: Extensive configuration options via Maven parameters

    Step-based Testing: Support for test steps with individual results

    Multiple Taxonomies: Support for severity, priority, level, type, and methodology classifications

Annotations
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

    P0, P1, P2, P3, P4 (P0 - highest priority)

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

    version - test version (default: "1.0")

    description - test description

@TestStep

Test step description (repeatable via @TestSteps):

    order - step execution order

    description - step description

Requirements

    Java 11 or higher

    Maven 3.6 or higher

Installation

Build and install all modules to your local Maven repository:
bash

mvn clean install

Usage
1. Add Dependencies

Add the required dependencies to your project's pom.xml:
xml

//<dependencies>
//    <dependency>
//        <groupId>io.github.lyazginad</groupId>
//        <artifactId>testing-annotations</artifactId>
//        <version>1.0.0</version>
//    </dependency>

//    <!-- Optional: Only needed if using core functionality directly -->
//    <dependency>
//        <groupId>io.github.lyazginad</groupId>
//        <artifactId>testing-core</artifactId>
//        <version>1.0.0</version>
//    </dependency>
//</dependencies>

2. Configure the Plugin

Add the plugin to your project's pom.xml:
xml

//<build>
//    <plugins>
//        <plugin>
//            <groupId>io.github.lyazginad</groupId>
//            <artifactId>testing-annotations-maven-plugin</artifactId>
//            <version>1.0.0</version>
//            <configuration>
//                <outputDirectory>${project.build.directory}/test-reports</outputDirectory>
//                <reportFileName>custom-test-report.json</reportFileName>
//                <prettyPrint>true</prettyPrint>
//                <generateSampleData>false</generateSampleData>
//                <scanTestClasses>true</scanTestClasses>
//                <strictScanning>true</strictScanning>
//            </configuration>
//        </plugin>
//    </plugins>
//</build>

3. Plugin Configuration Parameters
   Parameter	Default	Description
   outputDirectory	${project.build.directory}/test-reports	Directory for report output
   reportFileName	custom-test-report.json	Report file name
   prettyPrint	true	JSON formatting for readability
   generateSampleData	false	Generate sample test data
   scanTestClasses	true	Scan test classes for annotations
   strictScanning	true	Strict scanning mode (only processes classes with custom annotations)
4. Annotation Usage Example
   java

import io.github.lyazginad.testing.annotations.*;

public class ComprehensiveTestExample {

    @TestCase(
        order = 1,
        name = "User Authentication Test",
        category = "security"
    )
    @Severity(Severity.Level.CRITICAL)
    @Priority(Priority.Level.P0)
    @TestLevel(TestLevel.Level.SYSTEM)
    @TestType(TestType.Type.SECURITY)
    @TestMethod(TestMethod.Method.BLACK_BOX)
    @TestInfo(
        author = "Jane Smith",
        version = "2.1",
        description = "Comprehensive test of user authentication functionality"
    )
    @TestSteps({
        @TestStep(order = 1, description = "Initialize test environment"),
        @TestStep(order = 2, description = "Navigate to login page"),
        @TestStep(order = 3, description = "Enter valid credentials"),
        @TestStep(order = 4, description = "Verify successful authentication"),
        @TestStep(order = 5, description = "Cleanup test data")
    })
    public void testUserAuthentication() {
        // Test implementation with step execution
        // Each step can be individually marked as passed/failed
    }

    @TestCase(order = 2, name = "Performance Load Test", category = "performance")
    @Severity(Severity.Level.HIGH)
    @Priority(Priority.Level.P1)
    @TestLevel(TestLevel.Level.INTEGRATION)
    @TestType(TestType.Type.PERFORMANCE)
    @TestMethod(TestMethod.Method.GRAY_BOX)
    public void testPerformanceUnderLoad() {
        // Performance test implementation
    }
}

5. Executing the Plugin

Run the plugin using Maven:
bash

# If configured in pom.xml
mvn testing:generate-report

# Direct execution
mvn io.github.lyazginad:testing-annotations-maven-plugin:1.0.0:generate-report

Report Structure

The generated JSON report includes:
General Statistics

    Execution timestamp

    Total test count

    Passed/failed tests count

    Total execution duration

    Success rate percentage

Test Details

    Class and method names

    Execution order and test name

    Category classification

    Pass/fail status with timestamps

    Error messages (if any)

    Complete step information with individual results

    Severity and priority levels

    Testing level, type, and methodology

    Author, version, and description metadata

Statistical Distributions

    Severity level distribution

    Priority level distribution

    Test category distribution

Advanced Features
Sample Data Generation

Enable sample data generation to demonstrate plugin functionality without real tests:
xml

<configuration>
    <generateSampleData>true</generateSampleData>
</configuration>

CI/CD Integration

The plugin can be integrated into CI/CD pipelines for automatic report generation after test execution. The JSON reports can be processed by other tools for visualization, analysis, or integration with test management systems.
Custom Processing

The JSON reports are designed to be easily consumable by other tools for:

    Test result visualization

    Trend analysis

    Quality metrics reporting

    Integration with test management systems

Output Example

After execution, the plugin provides detailed console output:
text

=== DETAILED TEST REPORT SUMMARY ===
Execution Time: 2024-01-15T10:30:45.123
Total Tests: 15
Passed: 12
Failed: 3
Success Rate: 80.00%
Total Duration: 45678ms
--- Severity Distribution ---
CRITICAL: 2 tests
HIGH: 5 tests
MEDIUM: 6 tests
LOW: 2 tests
--- Priority Distribution ---
P0: 3 tests
P1: 4 tests
P2: 5 tests
P3: 3 tests
--- Category Distribution ---
security: 4 tests
performance: 3 tests
authentication: 5 tests
integration: 3 tests

Support

For issues, questions, or contributions, please refer to the project's issue tracker.
License

This project is licensed under the MIT License - see the LICENSE file for details.
