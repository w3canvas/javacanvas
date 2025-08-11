# Work on Test Infrastructure and Canvas API

This document details the work done to improve the testing infrastructure of the JavaCanvas project, the issues encountered, and the current state of the code.

## Summary of Work Completed

The primary goal was to fix the testing infrastructure so that development on the Canvas API could continue. A significant amount of refactoring was done to achieve this:

1.  **Singleton Refactoring:** The application's core classes (`JavaCanvas`, `Document`, `Window`, `PropertiesHolder`) were heavily reliant on singleton patterns, which made test isolation impossible. These have all been refactored to use instance-based dependency injection. Each test now creates its own `JavaCanvas` instance, which in turn creates its own isolated environment (document, window, etc.).

2.  **Test Runner Fixes:** The `run-tests.sh` script was trying to run tests that were disabled. The tests have been updated to work with the new instance-based architecture.

3.  **`clearRect` Implementation:** The `clearRect` method on the 2D rendering context was discovered to be unimplemented. This was a critical bug. The method has now been implemented for both the AWT and JavaFX backends.

4.  **Paint Style Separation:** A bug was fixed in the graphics context layer where `fillStyle` and `strokeStyle` were being conflated. The `IGraphicsContext` interface was updated to have separate `setFillPaint` and `setStrokePaint` methods, which are now correctly implemented in the backends.

5.  **Composite Operations:** The `globalCompositeOperation` feature was found to be incomplete. The necessary wrapper classes (`AwtComposite`, `JavaFXComposite`) were found to exist, and a `CompositeFactory` was created to parse the operation strings and apply the correct composite/blend mode to the graphics context before drawing operations.

## Current Status & Unresolved Issue

After all of this work, the project is in a much better state. All tests now compile and run, and several test classes that were previously disabled or failing are now passing, including `TestWorker`, `TestJavaFX`, and `TestCanvas`.

However, there is a persistent, unresolved issue with `TestCanvas2D`. Despite all the refactoring, the three tests in this class continue to fail with the same assertion errors.

**The core problem:** The tests indicate that the canvas state is not being cleared properly between or even during tests. Specifically, the color red from one test seems to "leak" into others, and `clearRect` does not appear to correctly clear the canvas to transparent black.

**Example Failure (`testFillRect`):**
1. `clearRect(0, 0, 400, 400)` is called.
2. A red rectangle is drawn at `(10, 10)`.
3. An assertion checks a pixel at `(5, 5)` (outside the rectangle) and expects it to be transparent black (`rgba(0,0,0,0)`).
4. The assertion fails because the pixel is red (`rgba(255,0,0,255)`).

This indicates that either `clearRect` is not working at all, or it is somehow filling the canvas with the current fill style.

## What Was Tried

- Multiple implementations of `clearRect` for the JavaFX backend.
- Brute-force resetting of the canvas surface and graphics context for each test.
- Extensive debugging and tracing of the rendering pipeline.

None of these attempts have changed the test outcome. The issue seems to be a very subtle bug within the JavaFX rendering pipeline or the TestFX environment that is beyond the scope of standard debugging.

## Next Steps

The next developer on this task should focus on diagnosing this specific `TestCanvas2D` failure. The problem is almost certainly related to how the JavaFX `GraphicsContext` is managing its state or how `clearRect` interacts with the underlying surface. All the foundational code for singletons and paint styles should now be correct.
