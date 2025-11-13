# JavaCanvas

A Java implementation of the HTML5 Canvas 2D API with dual graphics backend support (AWT/Swing and JavaFX) and JavaScript integration via Mozilla Rhino.

## Overview

**JavaCanvas** enables HTML5 Canvas drawing capabilities in Java applications by bridging JavaScript canvas code with Java graphics backends. This allows JavaScript-based canvas applications to run in Java environments with full 2D rendering support.

**Status:** ~60-65% feature complete for modern Canvas 2D API specification
**License:** Public Domain / CC0 (Creative Commons Zero)
**Developed by:** Jumis, Inc.

## Features

### Supported Canvas 2D API Features

#### ✓ Core Drawing Operations
- Rectangle operations: `clearRect()`, `fillRect()`, `strokeRect()`
- Path operations: `beginPath()`, `closePath()`, `moveTo()`, `lineTo()`
- Curves: `quadraticCurveTo()`, `bezierCurveTo()`, `arcTo()`, `arc()`, `ellipse()`
- Path rendering: `fill()`, `stroke()`, `clip()`
- Hit testing: `isPointInPath()`, `isPointInStroke()` ⚠️ (see Known Issues)

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
- Basic compositing: `globalCompositeOperation` (limited modes)

#### ✓ Gradients & Patterns
- Linear gradients: `createLinearGradient()`
- Radial gradients: `createRadialGradient()`
- Patterns: `createPattern()`

#### ✓ Text Rendering
- Basic text drawing: `fillText()`, `strokeText()`
- Text measurement: `measureText()` (width only)
- Text properties: `font`, `textAlign`, `textBaseline`

#### ✓ Image Operations
- Image drawing: `drawImage()` (all 3 variants)
- Pixel manipulation: `createImageData()`, `getImageData()`, `putImageData()`

#### ✓ Context Management
- `isContextLost()`, `getContextAttributes()`

### Missing Features (Modern Canvas 2D API)

#### ⚠️ Non-Functional Features
- **Filters** - Property exists but always returns "none" (not implemented)
- **TextMetrics** - Only `width` is accurate; other properties return placeholders

#### ✗ Not Implemented
- **Path2D** - Reusable path objects and methods
- **Shadow Effects** - `shadowBlur`, `shadowColor`, `shadowOffsetX`, `shadowOffsetY`
- **Image Smoothing** - `imageSmoothingEnabled`, `imageSmoothingQuality`
- **Conic Gradients** - `createConicGradient()`
- **Round Rectangles** - `roundRect()` method
- **Modern Text Features** - `direction`, `letterSpacing`, `wordSpacing`, `fontKerning`
- **Advanced Compositing** - Only 2/26 blend modes implemented (source-over, copy)
- **ImageBitmap** - No support for ImageBitmap objects
- **Focus Management** - `drawFocusIfNeeded()`
- **Canvas Property** - `.canvas` back-reference

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

**Passing Tests:**
- ✓ `TestJavaFX` - JavaFX backend drawing capabilities
- ✓ `TestCSSParser` - CSS color parser
- ✓ `TestCanvas` - Application initialization smoke test

**Disabled Tests:**
- ⚠️ `TestCanvas2D` - 35 comprehensive Canvas 2D API tests (state management issues)
- ⚠️ `TestWorker` - Web Worker tests (incomplete OffscreenCanvas implementation)

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

### Critical Bugs

1. **`isPointInStroke()` with arcTo** - [UNDONE.md](UNDONE.md)
   - JavaFX `ArcTo` to AWT `Arc2D` conversion has geometry bugs
   - Affects: `testIsPointInStrokeWithArcTo` + 10 related tests
   - Location: `src/main/java/com/w3canvas/javacanvas/backend/awt/AwtGraphicsContext.java`

2. **State Management in Tests** - [TESTING.md](TESTING.md)
   - TestCanvas2D suite disabled due to test interference
   - Tests share state despite isolation attempts
   - Indicates deeper architectural coupling issues

3. **Missing JavaScript Source Files** - [AGENTS.md](AGENTS.md)
   - Main application logic files missing from repository
   - Stub classes created to maintain buildability
   - Limited functionality without JavaScript files

### Design Limitations

- **Tight Coupling** - Deep integration between GUI and business logic
- **Static Dependencies** - Extensive use of singletons complicates testing
- **Rhino Dependency** - Hard coupling to Rhino JavaScript engine

## Project Status

### Completeness: ~60-65%

**Strengths:**
- ✓ Solid architectural foundation
- ✓ Dual backend support (AWT + JavaFX)
- ✓ Core drawing operations functional
- ✓ Modern build/test infrastructure
- ✓ Headless testing capability

**Gaps:**
- Missing ~35-40% of modern Canvas 2D API features
- Critical test suite disabled due to bugs
- Known conversion bugs in path operations
- Limited composite/blend mode support
- No shadow effects implementation

### Development Roadmap

**Phase 1 - Fix Foundation** (High Priority)
- [ ] Fix state management issues (enable TestCanvas2D)
- [ ] Resolve arcTo/isPointInStroke bug
- [ ] Implement proper test isolation
- [ ] Achieve >50% test coverage

**Phase 2 - Core Features** (High Priority)
- [ ] Implement shadow effects
- [ ] Add Path2D support
- [ ] Implement image smoothing controls
- [ ] Expand composite/blend modes (Porter-Duff operations)
- [ ] Make filter property functional

**Phase 3 - Modern Features** (Medium Priority)
- [ ] Add `roundRect()`
- [ ] Implement conic gradients
- [ ] Complete OffscreenCanvas implementation
- [ ] Add modern text properties
- [ ] Implement ImageBitmap

**Phase 4 - Polish** (Lower Priority)
- [ ] Complete TextMetrics properties
- [ ] Add focus management
- [ ] Performance optimization
- [ ] Comprehensive documentation
- [ ] API compliance testing

## Documentation

- [README](README.md) - This file
- [TESTING.md](TESTING.md) - Testing guide and test status
- [UNDONE.md](UNDONE.md) - Known bugs and incomplete work
- [REFACTOR.md](REFACTOR.md) - Architecture refactoring plan
- [AGENTS.md](AGENTS.md) - Developer instructions
- [HEADLESS_TESTING_PLAN.md](HEADLESS_TESTING_PLAN.md) - Headless testing strategy

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

**Priority Areas:**
1. Fixing the arcTo conversion bug
2. Resolving test state management issues
3. Implementing missing Canvas API features (shadows, Path2D, etc.)
4. Expanding test coverage
5. Performance optimization

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
