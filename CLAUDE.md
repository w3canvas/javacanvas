# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

JavaCanvas is a Java implementation of the HTML5 Canvas 2D API with dual graphics backend support (AWT/Swing and JavaFX) and JavaScript integration via Mozilla Rhino and GraalJS. The project is **100% feature complete** with all 149 tests passing.

## Build and Development Commands

### Building

```bash
# Build with Maven (primary build tool)
mvn clean package

# Or use Maven wrapper
./mvnw clean package

# Build with Gradle (alternative)
./gradlew build             # Linux/Mac/Windows
.\gradlew.ps1 build         # Windows PowerShell alternative

# Build native image with GraalVM
mvn -Pnative package
# OR
./gradlew nativeCompile
```

**Gradle Configuration:** This project uses a project-local `.gradle` directory (already in `.gitignore`) instead of the user's home folder. This avoids Windows issues with special characters in usernames (e.g., `Char'les`). Both `gradlew.bat` and `gradlew.ps1` automatically set `GRADLE_USER_HOME` to the project's `.gradle` directory unless explicitly overridden.

If you need to use a shared Gradle home (rare), use `.\gradle-shared.ps1` on Windows.

**Important Gradle Notes:**
- JavaFX version must match Java toolchain version (17.0.13 for Java 17)
- Monocle version must also match (17.0.10 for Java 17)
- All TestFX/Prism system properties from pom.xml must be in build.gradle test configuration for headless testing

### Testing

```bash
# Run all tests (uses xvfb for headless testing on Linux)
./run-tests.sh

# Run tests directly with Maven
mvn test

# Run specific test class
mvn test -Dtest=TestCanvas2D

# Run specific test method
mvn test -Dtest=TestCanvas2D#testFillRect

# Generate coverage report (view at target/site/jacoco/index.html)
mvn clean test
```

**Important Testing Notes:**
- Tests require a headless environment (xvfb on Linux) for JavaFX/AWT rendering
- On Windows, tests may run directly but headless mode is recommended for CI
- Maven is configured with headless system properties in pom.xml
- All 149 tests must pass before committing changes

### Running the Application

```bash
# Run with Rhino JavaScript engine
java -cp target/javacanvas-1.0-SNAPSHOT.jar com.w3canvas.javacanvas.Main script.js

# Run with GraalJS engine
java -cp target/javacanvas-1.0-SNAPSHOT.jar com.w3canvas.javacanvas.Main --graal script.js
```

## Architecture: "Trident" Model

JavaCanvas uses a three-layered architecture that cleanly separates concerns:

### Layer 1: Interfaces (`com.w3canvas.javacanvas.interfaces`)
Pure Java interfaces defining the contract for the canvas system. No implementation code.

**Key Interfaces:**
- `ICanvasRenderingContext2D` - Main Canvas 2D API contract
- `IGraphicsBackend` - Factory for creating backend-specific components (surfaces, gradients, fonts)
- `IGraphicsContext` - Low-level rendering operations (draw, fill, stroke)
- `ICanvasSurface` - Represents a drawable surface with pixel data access
- `IPaint`, `IFont`, `IShape`, `IStroke`, `IComposite` - Rendering primitives
- `ICanvasGradient`, `ICanvasPattern` - Fill styles
- `IPath2D`, `IImageData`, `IImageBitmap`, `ITextMetrics` - Canvas objects

### Layer 2: Core (`com.w3canvas.javacanvas.core`)
Backend-agnostic implementation of Canvas 2D business logic. **Never imports AWT or JavaFX classes.**

**Key Classes:**
- `CoreCanvasRenderingContext2D` - Complete Canvas 2D API implementation
  - Maintains rendering state (fillStyle, strokeStyle, lineWidth, etc.)
  - Manages state stack for save/restore
  - Delegates rendering to IGraphicsContext (backend-agnostic)
  - Handles Path2D, filters, shadows, compositing
- `Path2D` - Reusable path objects
- `ImageData`, `ImageBitmap` - Image manipulation
- `TextMetrics` - Text measurement results
- `ColorParser`, `CSSFilterParser` - CSS parsing utilities
- `CompositeFactory` - Creates composite operations

**Critical Architecture Rule:** Core layer must remain backend-agnostic. All rendering operations go through `IGraphicsBackend` and `IGraphicsContext` interfaces.

### Layer 3: Backends (`com.w3canvas.javacanvas.backend`)

#### AWT Backend (`backend.awt`)
Production-ready AWT/Swing implementation. This is the primary, fully-featured backend.

**Key Classes:**
- `AwtGraphicsBackend` - Factory for AWT components
- `AwtGraphicsContext` - Wraps Graphics2D for rendering
- `AwtCanvasSurface` - BufferedImage-based surface
- `Awt*` classes - AWT-specific implementations of gradients, patterns, fonts, etc.

**AWT Backend Specifics:**
- Uses Graphics2D for all rendering
- Custom Paint implementations for conic gradients (AwtConicGradient)
- Approximations for some CSS blend modes (hue, saturation, color, luminosity)
- TextLayout for advanced text features (letter spacing, direction)

#### JavaFX Backend (`backend.javafx`)
Alternative implementation using JavaFX graphics. Less feature-complete than AWT.

**Key Classes:**
- `JavaFXGraphicsBackend` - Factory for JavaFX components
- `JavaFXGraphicsContext` - Wraps GraphicsContext for rendering
- `JavaFXCanvasSurface` - Canvas-based surface
- `JavaFX*` classes - JavaFX-specific implementations

**JavaFX Backend Limitations:**
- Some Porter-Duff operations fall back to SRC_OVER
- Advanced text properties (direction, spacing) stored but not fully rendered
- Limited composite mode support compared to AWT

#### Rhino Adapter (`backend.rhino`)
Bridges Core implementations to Mozilla Rhino JavaScript engine. Provides DOM-like APIs.

**Key Classes:**
- `CanvasRenderingContext2D` - Rhino scriptable wrapper around CoreCanvasRenderingContext2D
- `HTMLCanvasElement` - JavaScript canvas element
- `Document` - JavaScript document object
- `Window` - JavaScript global window object
- Gradient, Pattern, ImageData wrappers for Rhino

**JavaScript Integration:**
- All Rhino adapter classes extend Scriptable/ScriptableObject
- Adapters delegate to Core layer implementations
- Provides HTML5-like JavaScript API surface

## Key Development Patterns

### State Management
The Canvas 2D API maintains extensive state (fillStyle, strokeStyle, transformations, etc.). State is managed in `CoreCanvasRenderingContext2D` and can be saved/restored via a stack:

```java
// State is stored in ContextState inner class
save();  // Pushes current state onto stack
// ... modify state ...
restore();  // Pops state from stack
```

### Backend-Agnostic Rendering
All rendering goes through interfaces to support multiple backends:

```java
// CORRECT: Use backend interface
IGraphicsContext gc = surface.getGraphicsContext();
gc.fillRect(x, y, width, height);

// WRONG: Don't use AWT/JavaFX directly in Core layer
Graphics2D g = ...;  // Never in Core layer!
```

### Resource Management
Graphics contexts must be properly disposed:

```java
// AwtCanvasSurface.getGraphicsContext() creates a new Graphics2D
// Caller must call dispose() when done, OR use surface's internal gc
IGraphicsContext gc = surface.getGraphicsContext();
try {
    // ... rendering ...
} finally {
    gc.dispose();  // Critical for AWT backend
}
```

### Filter Application
Filters are parsed as CSS strings and applied as BufferedImageOp:

```java
ctx.setFilter("blur(5px) brightness(120%)");
// CSSFilterParser extracts filter functions
// Applied during rendering operations in backend
```

## Common Gotchas

1. **Graphics Disposal**: AWT Graphics2D objects must be disposed. The AwtCanvasSurface maintains an internal graphics context that gets disposed on reset().

2. **Backend Differences**: Not all features work identically across backends. AWT is the reference implementation. Always test with both backends when adding features.

3. **Headless Testing**: JavaFX requires special headless configuration (xvfb, monocle). Tests include `@Timeout` annotations because headless initialization is slow.

4. **Rhino vs Core**: When adding Canvas API features:
   - Implement in Core layer first (backend-agnostic)
   - Add backend implementations (AWT, JavaFX)
   - Add Rhino wrappers last (thin delegation)

5. **Path2D Transform Application**: Paths store their own transforms. When rendering a Path2D, combine its transform with the current context transform.

6. **State Stack**: The state stack stores deep copies of state, including transformation matrices. Never modify state objects after pushing to stack.

## Test Architecture

Tests are organized by functionality:

- `TestCanvas2D` - 77 comprehensive Canvas 2D API tests (core rendering, transforms, paths)
- `TestImageBitmap` - ImageBitmap API (11 tests)
- `TestOffscreenCanvas` - OffscreenCanvas API (10 tests)
- `TestCSSFilters` - CSS filter parsing (18 tests)
- `TestFilterIntegration` - Filter rendering integration (10 tests)
- `TestSharedWorker`, `TestWorker` - Web Worker APIs
- `TestJavaFX`, `TestAwtBackendSmokeTest` - Backend-specific tests
- Font and integration tests

**Test Patterns:**
- Use `VisualRegressionHelper.compareToGoldenMaster()` for pixel-perfect rendering validation
- Golden masters stored in `src/test/resources/golden-masters/`
- Generate new baselines with `-DgenerateGoldenMasters=true`
- Tests extend `ApplicationTest` for JavaFX integration
- Proper synchronization (no Thread.sleep) for async operations

## Adding New Canvas Features

1. **Add to Interface** (`ICanvasRenderingContext2D`)
   ```java
   void newFeature(double param);
   ```

2. **Implement in Core** (`CoreCanvasRenderingContext2D`)
   ```java
   @Override
   public void newFeature(double param) {
       // State management, validation
       gc.drawNewFeature(param);  // Delegate to backend
   }
   ```

3. **Add Backend Method** (`IGraphicsContext`)
   ```java
   void drawNewFeature(double param);
   ```

4. **Implement in Backends** (`AwtGraphicsContext`, `JavaFXGraphicsContext`)
   ```java
   @Override
   public void drawNewFeature(double param) {
       // AWT or JavaFX specific rendering
   }
   ```

5. **Add Rhino Wrapper** (`CanvasRenderingContext2D`)
   ```java
   public void jsFunction_newFeature(double param) {
       coreContext.newFeature(param);
   }
   ```

6. **Write Tests** (`TestCanvas2D`)
   ```java
   @Test
   void testNewFeature() {
       ctx.newFeature(100);
       // Verify rendering
   }
   ```

## Code Quality Standards

- **JavaDoc**: All public interfaces and classes must have complete JavaDoc
- **Parameter Validation**: Validate parameters with descriptive IllegalArgumentException messages
- **Resource Management**: Always dispose Graphics2D contexts
- **No printStackTrace**: Use proper logging or throw exceptions
- **Error Handling**: Meaningful error messages for users
- **Test Coverage**: All new features must have tests
- **No Magic Numbers**: Extract as named constants
- **Backend Agnostic Core**: Core layer never imports java.awt or javafx packages

## Related Documentation

- **README.md** - Project overview, features, usage examples
- **STATUS.md** - Current status, test results, known limitations
- **REFACTOR.md** - Architectural design and Trident model explanation
- **TESTING.md** - Complete testing guide and test suite breakdown
- **IMPROVEMENTS.md** - Code quality recommendations and priorities
