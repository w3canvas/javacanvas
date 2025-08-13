# JavaCanvas Project: Outstanding Work

This document outlines the current state of the JavaCanvas project, including missing features and a roadmap for future development.

## 1. Critical Missing Components

### 1.1. Original JavaScript Application
- **Status:** MISSING
- **Description:** As noted in `AGENTS.md`, the original JavaScript files that contained the main application logic are missing. Without these, the full functionality of the Rhino-based components cannot be tested or restored.
- **Next Step:** Attempt to locate the original source. If it cannot be found, a new demo application should be created to drive the development of the canvas API.

## 2. Canvas API Feature Verification and Implementation

This section lists the features of the HTML5 Canvas 2D API and their current status in this project. The immediate goal is to get all features to a "Verified" state.

### 2.1. Drawing Rectangles
- `fillRect(x, y, w, h)`: **Verified**
- `strokeRect(x, y, w, h)`: **Verified**
- `clearRect(x, y, w, h)`: **Verified**

### 2.2. Drawing Paths
- `beginPath()`: **Verified**
- `closePath()`: **Verified**
- `moveTo(x, y)`: **Verified**
- `lineTo(x, y)`: **Verified**
- `stroke()`: **Partially Verified** (Basic path tested, but no complex shapes)
- `fill()`: **Not Verified**
- `rect(x, y, w, h)`: **Not Verified**
- `arc(x, y, r, sa, ea, anti-cw)`: **Not Verified**
- `arcTo(x1, y1, x2, y2, r)`: **Not Verified**
- `quadraticCurveTo(cpx, cpy, x, y)`: **Not Verified**
- `bezierCurveTo(cp1x, cp1y, cp2x, cp2y, x, y)`: **Not Verified**

### 2.3. Drawing Text
- `fillText(text, x, y, maxWidth)`: **Not Verified** (Failing tests, text is always rendered in black)
- `strokeText(text, x, y, maxWidth)`: **Not Verified** (Failing tests, text is always rendered in black)
- `measureText(text)`: **Not Verified**

### 2.4. Line Styles
- `lineWidth`: **Partially Verified**
- `lineCap`: **Not Verified**
- `lineJoin`: **Not Verified**
- `miterLimit`: **Not Verified**

### 2.5. Fill and Stroke Styles
- `fillStyle`: **Partially Verified** (Color strings work, but Gradients/Patterns are not tested)
- `strokeStyle`: **Partially Verified** (Color strings work, but Gradients/Patterns are not tested)
- `createLinearGradient(x0, y0, x1, y1)`: **Not Verified**
- `createRadialGradient(x0, y0, r0, x1, y1, r1)`: **Not Verified**
- `addColorStop(offset, color)`: **Not Verified**
- `createPattern(image, repetition)`: **Not Verified**

### 2.6. Compositing
- `globalAlpha`: **Not Verified**
- `globalCompositeOperation`: **Partially Verified** ('copy' and 'source-over' are tested)

### 2.7. Drawing Images
- `drawImage(...)`: **Not Verified**

### 2.8. Pixel Manipulation
- `createImageData(width, height)`: **Not Verified**
- `getImageData(sx, sy, sw, sh)`: **Verified**
- `putImageData(imageData, dx, dy)`: **Verified**

### 2.9. State
- `save()`: **Not Verified**
- `restore()`: **Not Verified**

### 2.10. Transformations
- `scale(x, y)`: **Not Verified**
- `rotate(angle)`: **Not Verified**
- `translate(x, y)`: **Not Verified**
- `transform(a, b, c, d, e, f)`: **Not Verified**
- `setTransform(a, b, c, d, e, f)`: **Not Verified**

## 3. Web Worker API
- `Worker`: **Verified**
- `OffscreenCanvas`: **Verified**
- `postMessage`: **Verified**
- `onmessage`: **Verified**

## 4. Known Issues

### 4.1. JavaFX Backend Rendering
- **Status:** Unresolved
- **Description:** Several tests related to drawing operations (`fillText`, `strokeText`, `arcTo`) are failing when using the JavaFX backend. The operations render in black instead of the specified color. Extensive debugging has confirmed that the correct color is being passed to the JavaFX `GraphicsContext`, but it is not being applied during rendering. The root cause is unknown, but it is likely a subtle state management issue within the JavaFX backend or its interaction with the headless testing environment.

## 5. Testing Infrastructure

- **Image Comparison:** The current pixel-by-pixel assertion is brittle. A more robust image comparison framework is needed to properly test anti-aliased rendering. The existing `ImageComparator.java` class could be a starting point.
