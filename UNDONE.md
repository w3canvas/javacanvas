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

### 1.2. Implemented but Untested
The following features have an implementation in the codebase but lack specific, targeted tests. The immediate priority is to write tests for these features to move them to the "Verified" state.

- **State Management**: `save()`, `restore()`
- **Transformations**: `scale()`, `rotate()`, `translate()`, `transform()`, `setTransform()`, `resetTransform()`
- **Path Methods**: `ellipse()`, `clip()`, `isPointInPath()`, `isPointInStroke()`
- **Line Styles**: `lineJoin`, `miterLimit`, `setLineDash()`, `getLineDash()`, `lineDashOffset`
- **Text Styling**: `textAlign`, `textBaseline`
- **Compositing**: `globalAlpha`
- **Image Drawing**: `drawImage()`
- **Pixel Manipulation**: `createImageData()`

### 1.3. Partially Implemented or Failing

- **Text Drawing**: `fillText()`, `strokeText()`
  - **Status**: FAILING
  - **Description**: The existing tests for these methods are failing because text is always rendered in black, regardless of the `fillStyle` or `strokeStyle` set. Pure tests for both AWT (`PureAWTFontTest.testFillTextPure`) and JavaFX (`PureJavaFXFontTest.testFillTextPure`) have been added, which demonstrate that direct text rendering with `java.awt.Graphics2D` and `javafx.scene.canvas.GraphicsContext` works correctly. This confirms the issue lies within the project's canvas abstraction layer, not in the underlying graphics libraries themselves.
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

### 2.1. JavaFX and AWT Backend Text Rendering
- **Status:** Unresolved
- **Description:** As noted above, `fillText()` and `strokeText()` do not respect the current `fillStyle` or `strokeStyle`, rendering text in black. This is the most critical bug blocking full API compliance. Pure tests for both JavaFX and AWT have confirmed that the underlying `GraphicsContext` and `Graphics2D` are functioning correctly. The issue is now isolated to the abstraction layer, specifically the `CoreCanvasRenderingContext2D` and its interaction with the backend graphics contexts (`JavaFXGraphicsContext` and `AwtGraphicsContext`). The state information (specifically the fill/stroke paint) is not being correctly propagated to the backend contexts before their text rendering methods are called.

## 3. Rhino Interface and Testing

While the current test suite (`TestCanvas2D.java`) is excellent for verifying the Java backend, it does not fully validate the Rhino JavaScript interface. The tests are written in Java and call the Java `ICanvasRenderingContext2D` interface directly.

A future goal should be to create a test suite that runs directly in Rhino (e.g., using a JavaScript test framework) to ensure that the types and method calls are correctly handled between the JavaScript and Java layers. This would provide more robust verification of the entire system.
