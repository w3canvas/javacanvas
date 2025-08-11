# Task: Create a Headless Test Environment for a Hybrid Java Application

## Description

This project is a hybrid Java application that combines JavaFX, AWT/Swing, and the Rhino JavaScript engine to implement an HTML5 Canvas API. The goal of this task is to create a stable and reliable headless test environment that can run unit and integration tests for the application.

## Challenges

The application's architecture presents several challenges for headless testing:

*   **Deep Integration of GUI and Logic:** The core application logic is tightly coupled with the AWT/Swing and JavaFX GUI toolkits. Many components expect to be part of a larger GUI hierarchy and have access to a `JFrame` or other GUI elements.
*   **Rhino JavaScript Engine:** The application uses the Rhino JavaScript engine to execute a significant portion of its logic. The Java and JavaScript code are tightly integrated, and many Java objects are exposed to the JavaScript environment as `Scriptable` objects.
*   **Static Singletons:** The application makes extensive use of static singletons (e.g., `Document.getInstance()`, `Window.getInstance()`) to provide access to core objects. This makes it difficult to use dependency injection and mocking frameworks for testing.

## Requirements

The headless test environment must meet the following requirements:

1.  **Headless Operation:** The environment must be able to run tests without a physical display. This is essential for running tests in CI/CD pipelines and other automated environments.
2.  **Full Application Context:** The environment must be able to initialize the full application context, including the Rhino runtime, the AWT/Swing and JavaFX toolkits, and the application's custom DOM implementation.
3.  **Support for Unit and Integration Tests:** The environment must support both unit tests, which test individual components in isolation, and integration tests, which test the interaction between multiple components.
4.  **Refactoring for Testability:** As part of this task, the application's code should be refactored to improve its testability. This may include:
    *   Creating interfaces to decouple the core logic from the GUI and Rhino implementations.
    *   Using dependency injection to provide mock implementations for the tests.
    *   Reducing the use of static singletons.
5.  **Documentation:** The test environment should be well-documented, with clear instructions on how to set it up and run the tests.

## Acceptance Criteria

*   All existing unit and integration tests pass in the headless environment.
*   The `TestJavaFX` test, which tests a `fillRect` call on a canvas, passes in the headless environment.
*   The code has been refactored to improve its testability.
*   The test environment is documented.
