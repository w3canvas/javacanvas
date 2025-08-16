# JavaCanvas Project: Outstanding Work

This document outlines the current state of the JavaCanvas project, tracking the implementation and verification status of the HTML Canvas API and related specifications. The goal is to achieve full, verifiable compliance with the standards.

The status of each feature is categorized as follows:
- **Verified**: The feature is implemented and has at least one passing automated test that verifies its basic functionality.
- **Implemented but Untested**: The feature is implemented in the Java and/or Rhino layer, but lacks specific automated tests to verify its behavior.
- **Partially Implemented or Failing**: The feature is either incomplete, or it has automated tests that are currently failing due to known or unknown issues.
- **Not Implemented**: The feature is missing from the codebase.

## 1. Canvas API Feature Status

### 1.1. Fully Implemented and Verified
The following features are considered stable and have passing tests.

- **Drawing Rectangles**: `fillRect()`, `strokeRect()`, `clearRect()`
- **Path Stroking and Filling**: `stroke()`, `fill()`
- **Path Primitives**: `beginPath()`, `closePath()`, `moveTo()`, `lineTo()`, `rect()`, `arc()`, `arcTo()`, `quadraticCurveTo()`, `bezierCurveTo()`
- **Line Styles**: `lineWidth`, `lineCap`
- **Fill and Stroke Styles**: `fillStyle` (colors), `strokeStyle` (colors), `createLinearGradient()`, `createRadialGradient()`, `addColorStop()`, `createPattern()`
- **Compositing**: `globalCompositeOperation` ('source-over', 'copy')
- **Pixel Manipulation**: `getImageData()`, `putImageData()`
- **Text**: `measureText()`
- **Text Drawing**: `fillText()`, `strokeText()` (AWT backend verified via smoke test)

### 1.2. Implemented but Untested
The following features have an implementation in the codebase but lack specific, targeted tests. The immediate priority is to write tests for these features to move them to the "Verified" state.

- **State Management**: `save()`, `restore()`
- **Transformations**: `scale()`, `rotate()`, `translate()`, `transform()`, `setTransform()`, `resetTransform()`
- **Path Methods**: `ellipse()`, `clip()`, `isPointInPath()`
- **Line Styles**: `lineJoin`, `miterLimit`, `setLineDash()`, `getLineDash()`, `lineDashOffset`
- **Text Styling**: `textAlign`, `textBaseline`
- **Compositing**: `globalAlpha`
- **Image Drawing**: `drawImage()`
- **Pixel Manipulation**: `createImageData()`
- **Text Drawing**: `fillText`'s `maxWidth` parameter is currently ignored by the AWT backend.

### 1.3. Partially Implemented or Failing

- **Font Loading**: `@font-face`, `document.fonts`
  - **Status**: PARTIALLY IMPLEMENTED
  - **Description**: The `FontFace` API is implemented for loading fonts from URLs, and the `RhinoFontFace` class exposes this to JavaScript. However, there are no rendering tests to confirm that loaded fonts are correctly applied to the canvas.
- **Image Loading**: `new Image()`, `image.src`, `image.onload`
  - **Status**: PARTIALLY IMPLEMENTED
  - **Description**: The `Image` class supports loading from data URIs and URLs. However, there are no tests that load an image and use `drawImage()` to render it to the canvas.
- **isPointInStroke()**:
  - **Status**: FAILING
  - **Description**: The `isPointInStroke()` method in the JavaFX backend is not implemented correctly and causes tests to fail. There is no straightforward way to check if a point is on the stroke of a path in JavaFX.
- **drawImage()**:
  - **Status**: FAILING
  - **Description**: The `drawImage()` method is not fully implemented and causes tests to fail. It does not correctly handle drawing a canvas onto another canvas.

### 1.4. Not Implemented
- (None at the moment, all major features have at least a partial implementation)

## 2. Known Issues

### 2.1. AWT Backend Test Timeouts
- **Status:** WORKAROUND IMPLEMENTED
- **Description:** The full test suite in `TestCanvas2D.java` times out when running with the AWT backend (`./mvnw -Dw3canvas.backend=awt test`). The test class is currently disabled for the AWT backend to prevent the timeout. This is a major issue that prevents full verification of the AWT backend. The cause of the timeout is unknown and needs further investigation. It is suspected to be a conflict between the TestFX framework and the AWT event dispatch thread.
