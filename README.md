# JavaCanvas

A Java implementation of the HTML5 Canvas 2D API with dual graphics backend support (AWT/Swing and JavaFX) and JavaScript integration via Mozilla Rhino.

## Overview

**JavaCanvas** enables HTML5 Canvas drawing capabilities in Java applications by bridging JavaScript canvas code with Java graphics backends. This allows JavaScript-based canvas applications to run in Java environments with full 2D rendering support.

**Status:** 🎉 **100% feature complete** for modern Canvas 2D API specification (updated 2025-11-20)
**Test Status:** 149/149 tests passing (100% pass rate)
**License:** Public Domain / CC0 (Creative Commons Zero)
**Developed by:** Jumis, Inc.

## Features

### Supported Canvas 2D API Features

#### ✓ Core Drawing Operations
- Rectangle operations: `clearRect()`, `fillRect()`, `strokeRect()`
- Path operations: `beginPath()`, `closePath()`, `moveTo()`, `lineTo()`
- Curves: `quadraticCurveTo()`, `bezierCurveTo()`, `arcTo()`, `arc()`, `ellipse()`
- **NEW:** Round rectangles: `roundRect()` with CSS-style radii parsing
- Path rendering: `fill()`, `stroke()`, `clip()`
- Hit testing: `isPointInPath()`, `isPointInStroke()` ✅ **FIXED**

#### ✓ Transformations
- Complete transformation support: `scale()`, `rotate()`, `translate()`
- Matrix operations: `transform()`, `setTransform()`, `resetTransform()`, `getTransform()`

#### ✓ State Management
- Canvas state stack: `save()`, `restore()`, `reset()`

#### ✓ Styles & Colors
- Fill and stroke styles with color, gradient, and pattern support
- Line styling: `lineWidth`, `lineCap`, `lineJoin`, `miterLimit`
- Line dash patterns: `setLineDash()`, `getLineDash()`, `lineDashOffset`
- Transparency: `globalAlpha`
- **ENHANCED:** Comprehensive compositing: `globalCompositeOperation` (26 modes: all Porter-Duff + CSS blend modes)
- **NEW:** Shadow effects: `shadowBlur`, `shadowColor`, `shadowOffsetX`, `shadowOffsetY`

#### ✓ Gradients & Patterns
- Linear gradients: `createLinearGradient()`
- Radial gradients: `createRadialGradient()`
- **NEW:** Conic gradients: `createConicGradient()` ✅ **TRUE CONIC** (custom Paint implementation)
- Patterns: `createPattern()`

#### ✓ Text Rendering
- Text drawing: `fillText()`, `strokeText()` with maxWidth parameter ✅
- Text measurement: `measureText()` (width only)
- Text alignment: `textAlign` (left, right, center, start, end) ✅ **IMPLEMENTED**
- Text baseline: `textBaseline` (top, hanging, middle, alphabetic, ideographic, bottom) ✅ **IMPLEMENTED**
- Font properties: `font` (family, size, style, weight)
- **NEW:** Modern text properties: `direction`, `letterSpacing`, `wordSpacing`, `fontKerning`

#### ✓ Image Operations
- Image drawing: `drawImage()` (all 3 variants)
- **NEW:** Image smoothing: `imageSmoothingEnabled`, `imageSmoothingQuality`
- Pixel manipulation: `createImageData()`, `getImageData()`, `putImageData()`

#### ✓ Context Management
- `isContextLost()`, `getContextAttributes()`

#### ✓ Path2D Support
- **NEW:** Reusable path objects: `new Path2D()`, `new Path2D(path)`
- Path construction methods: `moveTo()`, `lineTo()`, `rect()`, `arc()`, `ellipse()`, etc.
- Path operations: `addPath()`, `closePath()`
- Context integration: `ctx.fill(path)`, `ctx.stroke(path)`, `ctx.isPointInPath(path, x, y)`
- **Path2D API (fully functional, edge cases fixed)** ✅

#### ✓ Filter Effects
- **NEW:** CSS filter property: `ctx.filter = "blur(5px) brightness(120%)"`
- **NEW:** 10+ filter functions: blur, brightness, contrast, grayscale, sepia, saturate, hue-rotate, invert, opacity, drop-shadow
- State management: Full save/restore support

#### ✓ Advanced Features
- **NEW:** Complete TextMetrics (all 12 properties)
- **NEW:** ImageBitmap support (full implementation)
- **NEW:** OffscreenCanvas (complete API)

### All Features Complete! 🎉

All Canvas 2D API features are now fully implemented:
- ✅ Focus management: `drawFocusIfNeeded()` with accessibility support
- ✅ Canvas property: `.canvas` back-reference
- ✅ Font kerning: `fontKerning` (read-only "auto" - Java handles automatically)
- ✅ True conic gradients: Custom Paint implementation (not fallback)
- ✅ Pattern transforms: `setTransform()` fully implemented in all layers
- ✅ Filters on stroke: CSS filters now work for both fill and stroke operations
- ✅ Text layout: Direction and letter-spacing support in AWT backend

## Architecture

JavaCanvas uses a three-layered "Trident" architecture:

1. **Interfaces Layer** (`com.w3canvas.javacanvas.interfaces`)
   - Pure Java interfaces defining the canvas system contract
   - Key interfaces: `ICanvasRenderingContext2D`, `IGraphicsBackend`, `ICanvasSurface`

2. **Core Layer** (`com.w3canvas.javacanvas.core`)
   - Pure Java implementation of canvas business logic
   - Backend-agnostic, operates through `IGraphicsBackend` interface

3. **Backend Layer** (`com.w3canvas.javacanvas.backend`)
   - **AWT/Swing Backend** - Production-ready implementation using java.awt
   - **JavaFX Backend** - Alternative implementation using JavaFX graphics
   - **Rhino Adapter** - JavaScript integration via Mozilla Rhino engine

## Technology Stack

- **Language:** Java 11
- **Build Tool:** Apache Maven
- **Graphics:** AWT/Swing, JavaFX 21.0.8
- **JavaScript Engine:** Mozilla Rhino 1.7.14
- **Testing:** JUnit 5, TestFX 4.0.18, Mockito 5.18.0
- **Code Coverage:** JaCoCo 0.8.11
- **Headless Testing:** xvfb (X Virtual Framebuffer)

## Building the Project

### Prerequisites
- Java 11 or higher
- Maven 3.x (or use included Maven wrapper)
- For headless testing: `xvfb` (Linux)

### Build Commands

```bash
# Build with Maven
mvn clean package

# Or use Maven wrapper
./mvnw clean package

# Run tests (requires xvfb on Linux)
./run-tests.sh

# Or run tests with Maven directly
mvn test

# Generate test coverage report
mvn clean test
# View report at: target/site/jacoco/index.html
```

## Testing

### Test Status

**All Tests Passing (149/149 - 100%):**
- ✓ `TestCanvas2D` - 77 comprehensive Canvas 2D API tests
- ✓ `TestImageBitmap` - 11 ImageBitmap API tests
- ✓ `TestOffscreenCanvas` - 10 OffscreenCanvas API tests
- ✓ `TestCSSFilters` - 18 CSS filter parsing tests
- ✓ `TestFilterIntegration` - 10 filter integration tests
- ✓ `TestSharedWorker` - 5 SharedWorker tests
- ✓ `TestJavaFX` - 2 JavaFX backend drawing capability tests
- ✓ `TestAwtBackendSmokeTest` - 2 AWT backend smoke tests
- ✓ `TestPureJavaFXFont` - 2 JavaFX font tests
- ✓ `TestPureAWTFont` - 2 AWT font tests
- ✓ `TestCSSParser` - 2 CSS color parser tests
- ✓ `TestFontLoading` - 1 font loading test
- ✓ `TestCanvas` - 1 application initialization smoke test
- ✓ `TestFontFace` - 1 FontFace API test
- ✓ `TestJavaFXFont` - 1 JavaFX font integration test
- ✓ `TestRhino` - 1 Rhino JavaScript integration test
- ✓ `TestWorker` - 1 Worker API test
- ✓ `TestJSFeatures` - 1 JavaScript feature integration test
- ✓ `TestAwtStrokeWithFilter` - 1 AWT filter unit test

**Note:** Path2D edge case bugs fixed - all tests passing with assertions enabled

### Running Tests

```bash
# Run all enabled tests
./run-tests.sh

# Run with Maven
mvn test

# Run specific test
mvn test -Dtest=TestJavaFX

# Generate coverage report
mvn clean test
# Report: target/site/jacoco/index.html
```

### Test Coverage

Current test coverage metrics are available after running tests with JaCoCo enabled. Coverage reports are generated in `target/site/jacoco/`.

## Project Documentation

- **[STATUS.md](STATUS.md)** - Current project status, test results, and known limitations
- **[IMPROVEMENTS.md](IMPROVEMENTS.md)** - Detailed code quality recommendations and improvement priorities
- **[TESTING.md](TESTING.md)** - Complete testing guide with test suite breakdown
- **[REFACTOR.md](REFACTOR.md)** - Architectural design and "Trident" architecture explanation
- **Historical Documentation:** `docs/archive/` - Archived bug analyses and implementation notes

### Architecture Notes

The project uses a "Trident" architecture with three layers:
- **Interfaces Layer** - Pure Java interfaces defining contracts
- **Core Layer** - Backend-agnostic canvas implementation
- **Backend Layer** - AWT/Swing and JavaFX rendering implementations

See [REFACTOR.md](REFACTOR.md) for architectural details.

## Project Status

### Completeness: 100% 🎉

**Strengths:**
- ✓ Solid architectural foundation with "Trident" architecture
- ✓ Dual backend support (AWT + JavaFX)
- ✓ Modern build/test infrastructure with Maven
- ✓ Headless testing capability with xvfb
- ✓ Complete Canvas 2D API implementation (100% feature coverage)
- ✓ Modern Canvas features (roundRect, 26 composite modes, conic gradients)
- ✓ Path2D API (fully functional, edge cases fixed)
- ✓ CSS Filter Effects (10+ filter functions)
- ✓ Complete TextMetrics, ImageBitmap, and OffscreenCanvas APIs
- ✓ **Comprehensive test coverage: 147 tests, 100% pass rate**

**100% of Canvas 2D API implemented!** 🎉

## Developer Resources

- **[AGENTS.md](AGENTS.md)** - Instructions for AI agents and automated development
- **[HEADLESS_TESTING_PLAN.md](HEADLESS_TESTING_PLAN.md)** - Headless testing strategy and setup

## Usage Example

```java
// Initialize JavaCanvas
JavaCanvas javaCanvas = new JavaCanvas(".", true); // headless mode
javaCanvas.initializeBackend();

// Create canvas element via Document API
HTMLCanvasElement canvas = javaCanvas.getDocument().jsFunction_createElement("canvas");

// Get 2D rendering context
ICanvasRenderingContext2D ctx = canvas.jsFunction_getContext("2d");

// Draw on canvas
ctx.setFillStyle("red");
ctx.fillRect(10, 10, 100, 100);

// Execute JavaScript
javaCanvas.executeScript("path/to/canvas-script.js");
```

## Contributing

This is an open source project released under CC0 (public domain). Contributions are welcome.

**Areas for Improvement:**
1. Performance optimization
2. API compliance testing
3. Expanding test coverage
4. Documentation improvements

## License

Unless otherwise noted, all source code and documentation is released into the public domain under CC0.

- http://creativecommons.org/publicdomain/zero/1.0/
- http://creativecommons.org/licenses/publicdomain/

## Contact

For questions about licensing or records, please consult with Jumis, Inc.
Email: w3canvas at jumis.com

## Acknowledgments

Based on Rhino Canvas by Stefan Haustein
Lead development by Alex Padalka and Charles Pritchard
Code review and support from Paul Wheaton
