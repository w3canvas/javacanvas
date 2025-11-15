# ArcTo isPointInStroke Investigation

## Test Failure
**Test:** `testIsPointInStrokeWithArcTo`
**Location:** `/home/user/javacanvas/src/test/java/com/w3canvas/javacanvas/test/TestCanvas2D.java:1104`
**Status:** FAILING ❌

## Test Details
```java
ctx.beginPath();
ctx.moveTo(20, 20);
ctx.arcTo(120, 20, 120, 70, 50);
ctx.setLineWidth(10);
// Don't stroke the path, isPointInStroke should work on the current path
inStroke.complete(ctx.isPointInStroke(70, 16));
```

**Expected:** `true`
**Actual:** `false`

## Investigation Summary

### Path Construction Analysis
The `arcTo(120, 20, 120, 70, 50)` creates:
1. **LineTo** to tangent point t1 = (70, 20)
2. **ArcTo** from t1 to tangent point t2 = (120, 70) with radius 50

**Geometric calculations:**
- Control points: (120, 20) and (120, 70)
- Angle between directions: 90°
- Tangent distance: 50 / tan(45°) = 50
- t1 = (120, 20) - 50 * (1, 0) = (70, 20)
- t2 = (120, 20) + 50 * (0, 1) = (120, 70)
- Cross product = 1.0 (positive = left turn)

### Arc Center Candidates
Two possible arc centers exist:
1. **Center A:** (70, 70) - arc from -90° to 0° (extent 90°)
2. **Center B:** (120, 20) - arc from 180° to 90° (extent -90°)

**Correct center:** (70, 70) based on tangent direction analysis

### Point (70, 16) Analysis
- Distance from center (70, 70): ~54.0 pixels
- Within stroke range [45, 55]: ✓ YES
- At angle: -90° (start of arc from center A)
- **Should be in stroke:** ✓ YES

### Changes Made

#### 1. SweepFlag Calculation (Already in HEAD)
**File:** `JavaFXGraphicsContext.java:426`
**Change:** `< 0` → `> 0`
```java
// Changed from:
boolean sweepFlag = (dx01 * dy12 - dy01 * dx12) < 0;

// To:
boolean sweepFlag = (dx01 * dy12 - dy01 * dx12) > 0;
```
**Reasoning:** Positive cross product (left turn) should set sweepFlag=true to select the correct arc center

#### 2. Extent Adjustment Logic (My Change)
**File:** `JavaFXGraphicsContext.java:758-764`
**Original:**
```java
if (!sweepFlag && extent > 0) {
    extent -= 360;
} else if (sweepFlag && extent < 0) {
    extent += 360;
}
```

**Changed to:**
```java
// SVG/Canvas: sweepFlag=true means clockwise, sweepFlag=false means counter-clockwise
// AWT Arc2D: positive extent is counter-clockwise, negative extent is clockwise
// So for clockwise (sweepFlag=true), we need negative extent
if (sweepFlag) {
    extent = -extent;
}
```

**Reasoning:** AWT Arc2D extent semantics are inverted from SVG sweep direction

### Test Result
❌ **Still FAILING** after both changes

## Possible Issues

1. **Path Element Order**: The LineTo before the ArcTo might be interfering
2. **Stroke Calculation**: AWT BasicStroke may handle the arc differently than expected
3. **Coordinate System**: Subtle differences in how JavaFX and AWT handle y-down coordinates
4. **Test Point**: Point (70, 16) is 4 pixels above the arc start - might be edge case
5. **Additional Bug**: There may be another issue in the conversion code not yet identified

## Next Steps

1. Add detailed logging to `convertFxPathToAwtPath()` to see actual path elements
2. Create standalone test to verify AWT Arc2D stroke behavior
3. Compare with reference implementation to see how other browsers handle this
4. Consider if the line from (20,20) to (70,20) should also be tested
5. Verify test expectations are correct

## Files Modified
- `/home/user/javacanvas/src/main/java/com/w3canvas/javacanvas/backend/javafx/JavaFXGraphicsContext.java` (extent adjustment logic)