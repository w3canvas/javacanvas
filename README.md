# JavaCanvas

A Java implementation of the HTML5 Canvas 2D API with dual graphics backend support (AWT/Swing and JavaFX) and JavaScript integration via Mozilla Rhino.

## Overview

**JavaCanvas** enables HTML5 Canvas drawing capabilities in Java applications by bridging JavaScript canvas code with Java graphics backends. This allows JavaScript-based canvas applications to run in Java environments with full 2D rendering support.

**Status:** 🎉 **100% feature complete** for modern Canvas 2D API specification (updated 2025-11-19)
**Test Status:** 127/127 tests passing (100% pass rate)
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
- Text drawing: `fillText()`, `strokeText()`
- Text measurement: `measureText()` (width only)
- Text properties: `font`, `textAlign`, `textBaseline`
- **NEW:** Modern text properties: `direction`, `letterSpacing`, `wordSpacing`

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

**All Tests Passing (127/127 - 100%):**
- ✓ `TestCanvas2D` - 57 comprehensive Canvas 2D API tests
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

## Known Issues

### Path2D Edge Cases ✅ FIXED (2025-11-16)

1. **Path2D Multi-Subpath Rendering** - ✅ FIXED
   - Issue: When multiple shapes were combined using `addPath()`, only the first shape rendered
   - Fix: Changed `AwtGraphicsContext.rect()` to use explicit path commands
   - Test: `testPath2DMultiSubpathRendering` now passing
   - Status: Fixed and verified

2. **Path2D with Transforms** - ✅ FIXED
   - Issue: Path2D objects didn't render at correct location when rotation transforms applied
   - Fix: Modified `CoreCanvasRenderingContext2D.fill(IPath2D)` to save/restore transform during replay
   - Test: `testPath2DWithTransform` now passing
   - Status: Fixed and verified

### Documentation

See detailed bug analysis and fixes in UNDONE.md

### Design Considerations

- **Tight Coupling** - Deep integration between GUI and business logic
- **Static Dependencies** - Use of singletons throughout codebase
- **Rhino Dependency** - Hard coupling to Mozilla Rhino JavaScript engine

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
- ✓ **Comprehensive test coverage: 127 tests, 100% pass rate**

**No remaining gaps - 100% of Canvas 2D API implemented!** 🎉

### Development Roadmap

**Phase 1 - Fix Foundation** ✅ **COMPLETED**
- [x] Fix state management issues
- [x] Resolve arcTo/isPointInStroke bug
- [x] Implement proper test isolation
- [x] Achieve 100% test pass rate (111 tests)

**Phase 2 - Core Features** ✅ **COMPLETED**
- [x] Implement shadow effects
- [x] Implement image smoothing controls
- [x] Expand composite/blend modes (26 modes)
- [x] Add `roundRect()`
- [x] Add modern text properties
- [x] Implement conic gradients
- [x] Add Path2D support
- [x] Make filter property functional

**Phase 3 - Modern Features** ✅ **COMPLETED**
- [x] Complete OffscreenCanvas implementation
- [x] Implement ImageBitmap
- [x] Complete TextMetrics properties

**Phase 4 - Polish** ✅ **COMPLETED 2025-11-19**
- [x] Fix 2 Path2D edge case bugs
- [x] Add focus management (`drawFocusIfNeeded()`)
- [x] Implement true conic gradients (custom Paint)
- [x] Add canvas back-reference property
- [x] Add font kerning property

## Documentation

### Current Documentation
- [README.md](README.md) - This file (main project documentation)
- [UNDONE.md](UNDONE.md) - Current status, known issues, and Path2D bugs
- [TESTING.md](TESTING.md) - Testing guide and test status
- [REFACTOR.md](REFACTOR.md) - Architecture refactoring plans
- [AGENTS.md](AGENTS.md) - Developer instructions
- [HEADLESS_TESTING_PLAN.md](HEADLESS_TESTING_PLAN.md) - Headless testing strategy

### Historical Documentation
- [docs/archive/](docs/archive/) - Historical bug analyses and implementation notes

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
