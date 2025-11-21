# Testing Guide

This document outlines the testing architecture for the JavaCanvas project and provides instructions on how to run the tests.

**Last Updated:** 2025-11-19

## Running the Tests

### Quick Start

To run all tests, execute from the project root:

```bash
./run-tests.sh
```

This script handles headless environment setup using `xvfb` and executes all tests via Maven.

### Alternative Methods

```bash
# Run with Maven directly
mvn test

# Run specific test class
mvn test -Dtest=TestCanvas2D

# Run with coverage report
mvn clean test
# View report at: target/site/jacoco/index.html
```

## Test Architecture

### Framework Components

- **Test Framework:** [JUnit 5](https://junit.org/junit5/)
- **GUI Testing:** [TestFX](https://github.com/TestFX/TestFX) for JavaFX applications
- **Build Tool:** [Apache Maven](https://maven.apache.org/) with `maven-surefire-plugin`
- **Headless Environment:** `xvfb` (X Virtual Framebuffer)
- **Coverage Reporting:** [JaCoCo](https://www.jacoco.org/) 0.8.11

### Visual Regression Testing

The project includes a visual regression framework (`VisualRegressionHelper`) for pixel-perfect rendering validation:

- **Golden Masters:** Reference images stored in `src/test/resources/golden-masters/`
- **Comparison:** Configurable pixel tolerance (default: 5% difference, 5px tolerance)
- **Generation:** Use `-DgenerateGoldenMasters=true` to create new baselines
- **Environment Support:** Different baselines for headless vs GUI environments

## Current Test Status

**Total:** 147 tests across 17 test classes (100% passing)

### Test Suite Breakdown

| Test Class | Tests | Status | Coverage |
|------------|-------|--------|----------|
| TestCanvas2D | 77 | ✅ Passing | Core Canvas 2D API, transformations, paths, shapes, edge cases |
| TestImageBitmap | 11 | ✅ Passing | ImageBitmap creation, rendering, resource management |
| TestOffscreenCanvas | 10 | ✅ Passing | OffscreenCanvas API, convertToBlob, transferToImageBitmap |
| TestCSSFilters | 18 | ✅ Passing | CSS filter parsing (blur, brightness, etc.) |
| TestFilterIntegration | 10 | ✅ Passing | Filter application and state management |
| TestSharedWorker | 5 | ✅ Passing | SharedWorker communication, multiple connections |
| TestJavaFX | 2 | ✅ Passing | JavaFX backend rendering |
| AwtBackendSmokeTest | 2 | ✅ Passing | AWT backend basic rendering |
| PureJavaFXFontTest | 2 | ✅ Passing | JavaFX font metrics |
| PureAWTFontTest | 2 | ✅ Passing | AWT font metrics |
| TestCSSParser | 2 | ✅ Passing | CSS color parsing |
| TestFontLoading | 1 | ✅ Passing | Custom font loading |
| TestCanvas | 1 | ✅ Passing | Application initialization |
| TestFontFace | 1 | ✅ Passing | FontFace API |
| TestJavaFXFont | 1 | ✅ Passing | JavaFX font integration |
| TestRhino | 1 | ✅ Passing | Rhino JavaScript engine integration |
| TestWorker | 1 | ✅ Passing | Worker API |

### Well-Tested Features

✅ **Core Drawing Operations**
- Rectangles, paths, curves, arcs, ellipses
- Fill and stroke operations
- Path2D objects with addPath support

✅ **Transformations**
- translate, rotate, scale
- setTransform, resetTransform
- Transform state save/restore

✅ **Styling**
- Gradients (linear, radial, conic)
- Patterns (all repeat modes)
- Shadows (blur, color, offset)
- Blend modes (26 operations)

✅ **Text Rendering**
- fillText, strokeText, measureText
- All TextMetrics properties
- Font properties and modern text features

✅ **Images & Pixels**
- drawImage (all variants)
- ImageData manipulation
- ImageBitmap API

✅ **Modern Features**
- OffscreenCanvas
- CSS filters
- roundRect
- Workers and SharedWorker

### Test Coverage Gaps

The following areas need additional test coverage (see [IMPROVEMENTS.md](IMPROVEMENTS.md) for details):

**Recently Added Tests (9 new tests in TestCanvas2D):** ✅
- ✅ `getTransform()` - now tested
- ✅ `isContextLost()` - now tested
- ✅ `getContextAttributes()` - now tested
- ✅ `reset()` comprehensive state clearing - now tested
- ✅ Parameter validation (lineWidth, arc radius, etc.) - now tested
- ✅ Fill rules ("evenodd" vs "nonzero") - now tested

**Still Missing Tests:**
- `drawFocusIfNeeded()` - no tests
- Advanced edge cases (degenerate transforms, very large coordinates)
- putImageData with dirty rectangles (all 7 parameters)
- Unicode text rendering

**Test Quality Issues:**
- High pixel tolerance in headless mode (10-15 pixels)
- Sleep-based timing in Worker tests (should use proper synchronization)
- Limited visual regression coverage (only 7 tests)
- Missing test documentation (JavaDoc)

## Test Coverage Reports

After running tests, JaCoCo generates coverage reports at:

```
target/site/jacoco/index.html
```

Open this file in a browser to view:
- Line coverage by package and class
- Branch coverage
- Cyclomatic complexity
- Missed/covered instructions

## Writing New Tests

### Basic Test Structure

```java
@Test
public void testFeatureName() {
    // Setup
    ICanvasRenderingContext2D ctx = canvas.getContext("2d");

    // Execute
    ctx.fillStyle = "red";
    ctx.fillRect(10, 10, 50, 50);

    // Verify
    assertPixel(ctx, 35, 35, 255, 0, 0, 255);
}
```

### Visual Regression Tests

```java
@Test
public void testVisualFeature() {
    // Draw content
    ctx.fillStyle = "blue";
    ctx.fillRect(0, 0, 100, 100);

    // Compare to golden master
    assertTrue(VisualRegressionHelper.compareToGoldenMaster(
        canvas, "testVisualFeature"));
}
```

### Headless Testing Considerations

- Use tolerance for pixel comparisons (anti-aliasing differences)
- Generate separate golden masters for headless environments
- Avoid sleep-based timing (use CompletableFuture with timeout)
- Test both AWT and JavaFX backends when applicable

## Continuous Integration

The test suite is designed to run in CI/CD environments:

1. **Headless Support:** All tests run with `xvfb`
2. **No External Dependencies:** All test resources included
3. **Fast Execution:** 147 tests complete in ~9 minutes
4. **Deterministic:** Visual regression with tolerance handles environment differences

## Troubleshooting

### Tests Fail in Headless Mode

- Generate headless-specific golden masters with `-DgenerateGoldenMasters=true`
- Increase pixel tolerance for anti-aliasing differences
- Check that xvfb is installed and running

### Tests Hang

- Look for deadlocks in JavaFX Application thread
- Check for missing Context.exit() in Rhino tests
- Ensure proper cleanup in tearDown methods
- **Headless Initialization:** JavaFX initialization can be slow in headless environments (xvfb). Tests extending `ApplicationTest` include a `@BeforeAll` warmup method and extended timeouts (`@Timeout`) to handle this.

### Flaky Tests

- Replace Thread.sleep() with proper synchronization
- Use CompletableFuture with timeout for async operations
- Increase tolerance for timing-sensitive assertions

## Related Documentation

- [STATUS.md](STATUS.md) - Current project status and completion
- [IMPROVEMENTS.md](IMPROVEMENTS.md) - Recommended improvements and missing test coverage
- [README.md](README.md) - Main project documentation

## Contact

For questions about testing or to contribute tests:
- Email: w3canvas at jumis.com
- GitHub: https://github.com/w3canvas/javacanvas
