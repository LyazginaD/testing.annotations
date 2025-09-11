markdown

# Testing Annotations Library

Custom annotations for enhanced test reporting and classification in Maven projects.

## Usage

1. Add the dependency to your project
2. Annotate your test methods with the provided annotations
3. Run: `mvn io.github.lyazginad:testing-annotations:1.0.0:generate-test-report`

## Available Annotations

- `@TestCase` - Basic test case information
- `@Severity` - Test severity level
- `@Priority` - Test priority level
- `@TestLevel` - Testing level (UNIT, INTEGRATION, etc.)
- `@TestType` - Type of testing (FUNCTIONAL, PERFORMANCE, etc.)
- `@TestMethod` - Testing methodology
- `@TestInfo` - Additional test metadata
- `@TestStep` - Individual test steps