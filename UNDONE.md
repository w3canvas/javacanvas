# JavaCanvas Project: Outstanding Work

This document outlines the current state of the JavaCanvas project, including missing features. It is intended to guide future development efforts.

## 1. Missing Core API Implementations

The architectural refactoring aimed to create a backend-agnostic Core API. However, several key parts of the HTML5 Canvas spec have interfaces but no core implementation.

The following features are missing:

*   **Gradients:** Implemented for both linear and radial gradients with a backend-agnostic approach.
*   **Patterns:** Implemented with a backend-agnostic approach.
*   **Text Metrics:** Implemented with a backend-agnostic approach.
*   **Image Data:** Implemented with a backend-agnostic approach.

## 2. Incomplete or Problematic Features

*   **Web Workers (`Worker` and `OffscreenCanvas`):**
    - The `TestWorker` test is now enabled and passes.
    - The Java implementation for `Worker` and `OffscreenCanvas` exists but is tightly coupled to the Rhino scripting engine.
    - The implementation relies on loading JavaScript files from a `documentBase`, but the environment for this is not configured, and the necessary worker scripts are missing.

## 3. Missing Project Components

*   **Original JavaScript Source:** As noted in `AGENTS.md`, the original JavaScript files that contained the main application logic are missing from the repository. This includes the worker scripts mentioned above and potentially other critical library code. Without these, the full functionality of the Rhino-based components cannot be tested or restored.
