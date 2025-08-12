# JavaCanvas Project: Outstanding Work

This document outlines the current state of the JavaCanvas project, including high-priority bugs and missing features. It is intended to guide future development efforts.

## 1. High-Priority Blocker: `TestCanvas2D` Failures

The most critical issue is a persistent failure in the `TestCanvas2D` test suite. This prevents the validation of the core 2D rendering context and blocks further development.

**The Problem:** The tests indicate that the canvas state is not being cleared properly. Color from one test appears to "leak" into subsequent tests, and `clearRect` does not correctly clear the canvas to transparent black (`rgba(0,0,0,0)`). For example, a pixel that should be transparent after a clear operation is reported as being solid red from a previous drawing operation.

**What Was Tried:**
- Refactoring all singleton classes (`JavaCanvas`, `Document`, `Window`) to use instance-based dependency injection to ensure test isolation.
- Implementing `clearRect` for both AWT and JavaFX backends.
- Fixing bugs related to `fillStyle`/`strokeStyle` and `globalCompositeOperation`.

Despite these efforts, the `TestCanvas2D` failures persist. The issue is likely a subtle bug within the JavaFX rendering pipeline or the TestFX environment.

## 2. Missing Core API Implementations

The architectural refactoring aimed to create a backend-agnostic Core API. However, several key parts of the HTML5 Canvas spec have interfaces but no core implementation.

The following features are missing:

*   **Gradients:** The `ICanvasGradient` interface exists, but there is no factory method on the rendering context (`createLinearGradient`, `createRadialGradient`) and no core implementation.
*   **Patterns:** The `ICanvasPattern` interface exists, but there is no `createPattern` method on the rendering context and no core implementation.
*   **Text Metrics:** The `ITextMetrics` interface exists, but the `measureText` method on the rendering context is not fully implemented and there is no backend-agnostic implementation of the metrics object.
*   **Image Data:** The `IImageData` interface is implemented, but only in the `backend.rhino` package. It is tightly coupled to AWT and Rhino. A backend-agnostic implementation in the `core` package is needed, along with `createImageData` and `getImageData` methods on the rendering context.

## 3. Incomplete or Problematic Features

*   **Web Workers (`Worker` and `OffscreenCanvas`):**
    - The `TestWorker` test is disabled.
    - The Java implementation for `Worker` and `OffscreenCanvas` exists but is tightly coupled to the Rhino scripting engine.
    - The implementation relies on loading JavaScript files from a `documentBase`, but the environment for this is not configured, and the necessary worker scripts are missing.

## 4. Missing Project Components

*   **Original JavaScript Source:** As noted in `AGENTS.md`, the original JavaScript files that contained the main application logic are missing from the repository. This includes the worker scripts mentioned above and potentially other critical library code. Without these, the full functionality of the Rhino-based components cannot be tested or restored.
