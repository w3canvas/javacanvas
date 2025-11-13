# Testing Guide

This document outlines the testing architecture for the JavaCanvas project and provides instructions on how to run the tests.

## Running the Tests

The entire test suite can be run using a single script. This script handles the setup of the headless environment and executes the tests using Maven.

To run all tests, execute the following command from the root of the project:

```bash
./run-tests.sh
```

This will run all enabled tests and report the results to the console.

## Test Architecture

The testing framework has been modernized and consolidated to use standard Java and Maven conventions.

*   **Test Framework:** All tests are written using [JUnit 5](https://junit.org/junit5/).
*   **GUI Testing:** GUI-related tests use [TestFX](https://github.com/TestFX/TestFX), which provides a framework for testing JavaFX applications.
*   **Build & Execution:** The project is built and tested using [Apache Maven](https://maven.apache.org/). The `maven-surefire-plugin` is used to execute the tests.
*   **Headless Environment:** The tests are run in a headless environment using `xvfb` (X virtual framebuffer). This allows GUI tests to run on servers without a physical display.

## Test Status

The test suite has been refactored to be more robust and maintainable. The status of the individual test classes is as follows:

*   **`TestJavaFX`**: **Passing**. This is a key test that verifies the JavaFX backend's drawing capabilities. It has been converted to a full TestFX test.
*   **`TestCSSParser`**: **Passing**. This is a simple unit test for the CSS color parser.
*   **`TestCanvas`**: **Passing**. This is a smoke test that verifies that the application can be initialized in headless mode without crashing.
*   **`TestCanvas2D`**: **Re-enabled (35 tests)**. This comprehensive test suite was previously disabled due to state-sharing issues. The root cause was identified as thread-local Rhino Context confusion between JUnit and JavaFX Application threads. Fixed by removing unnecessary Context.enter/exit calls from setUp/tearDown. See `STATE_MANAGEMENT_BUG_ANALYSIS.md` for details.
*   **`TestWorker`**: **Disabled**. This test is disabled because the application's `Worker` implementation is incomplete. It is missing a required `OffscreenCanvas` class, which is likely part of the missing JavaScript source files.

The old test scripts (`test.sh`, `test-javafx.sh`) have been removed and replaced by the consolidated `./run-tests.sh` script.

## Test Coverage

Test coverage reporting has been added using JaCoCo. After running tests, coverage reports are available at:

```
target/site/jacoco/index.html
```

To generate coverage reports:

```bash
mvn clean test
```
