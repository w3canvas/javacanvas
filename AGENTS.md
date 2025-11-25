# Agent Instructions for JavaCanvas

This document provides context and instructions for AI agents (Claude, Gemini, etc.) working on the JavaCanvas project.

## Project Overview

JavaCanvas is a **100% feature-complete** Java implementation of the HTML5 Canvas 2D API. It supports dual graphics backends (AWT/Swing and JavaFX) and integrates with JavaScript engines (Rhino and GraalJS).

**Key Capabilities:**
- Full Canvas 2D API (paths, images, text, gradients, patterns)
- Modern features: `roundRect`, `conicGradient`, `filter`, `Path2D`
- Headless testing support
- Native Image support (GraalVM)

## Architecture ("Trident" Model)

The project is structured into three layers:
1.  **Interfaces** (`com.w3canvas.javacanvas.interfaces`): Pure Java contracts.
2.  **Core** (`com.w3canvas.javacanvas.core`): Backend-agnostic business logic. **NEVER import AWT/JavaFX here.**
3.  **Backends** (`com.w3canvas.javacanvas.backend`):
    - `backend.awt`: Production-ready AWT implementation.
    - `backend.javafx`: JavaFX implementation.
    - `backend.rhino`: JavaScript bindings.

## Build System (Gradle)

The project has been migrated to **Gradle** (Kotlin DSL).

### Essential Commands

-   **Build:** `.\gradle-safe.ps1 build` (Windows) / `./gradlew build` (Linux/Mac)
-   **Test:** `.\gradle-safe.ps1 test` / `./gradlew test`
-   **Run (Rhino):** `.\gradle-safe.ps1 run --args="examples/hello.js"`
-   **Run (GraalJS):** `.\gradle-safe.ps1 run --args="--graal examples/hello.js"`
-   **Native Image:** `.\gradle-safe.ps1 nativeCompile`

### ⚠️ Critical Windows Environment Note

**Issue:** Java/Gradle has a known bug handling user paths with special characters (e.g., `C:\Users\Char'les`).
**Fix:** ALWAYS use the `.\gradle-safe.ps1` wrapper script on Windows. It automatically sets `GRADLE_USER_HOME` to `c:\wip\gradle_home` to bypass the issue.
**Do NOT** run `./gradlew` directly on Windows if the user path contains an apostrophe.

### JBang Support (Alternative Build/Run)

For Windows users or quick tasks, **JBang** is the preferred way to run the project, avoiding Gradle path issues.
- **Run:** `jbang JBangRunner.java examples/hello.js`
- **Test:** `jbang TestRunner.java`
- **Server:** `jbang RestRunner.java 8080`
- **Native Build:** `jbang export native JBangRunner.java` (requires GraalVM)

## Development Guidelines

1.  **State Management:** Canvas state is deep-copied in `ContextState`. Never modify state objects after pushing to the stack.
2.  **Resource Disposal:** AWT `Graphics2D` objects must be disposed. Use `try-finally` blocks when obtaining graphics contexts.
3.  **Headless Testing:** Tests are designed to run headlessly. On Linux, this requires `xvfb`. On Windows, it works out of the box but may require the `gradle-safe.ps1` wrapper.
4.  **Test Suite:** All 149 tests must pass. Use `VisualRegressionHelper` for pixel-perfect rendering checks.

## Documentation References

-   `README.md`: General overview and usage.
-   `CLAUDE.md`: Detailed developer guide and architecture deep-dive.
-   `REFACTOR.md`: Architectural decisions.
-   `TESTING.md`: Test suite documentation.

