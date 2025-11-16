# ArcTo Conversion Bug Analysis

## Bug Location
`src/main/java/com/w3canvas/javacanvas/backend/javafx/JavaFXGraphicsContext.java:536-626`

Method: `convertFxPathToAwtPath()`

## Problem Description

When `isPointInStroke()` is called on paths containing `arcTo()` elements, the JavaFX `ArcTo` path element must be converted to an AWT `Arc2D` for hit testing. The conversion algorithm implements the SVG arc endpoint-to-center parameterization, but **fails to properly account for the sweep direction**.

## Root Cause

At line 607:
```java
double extent = Math.toDegrees(angle(ux, uy, vx, vy));
```

The extent (sweep angle) is calculated but **never adjusted based on the `sweepFlag`** parameter from the JavaFX ArcTo.

According to SVG arc semantics:
- `sweepFlag = true` → clockwise (positive extent in AWT)
- `sweepFlag = false` → counter-clockwise (negative extent in AWT)

The current code calculates the absolute extent angle but doesn't apply the correct sign based on sweep direction.

## Impact

- **Test Failures:** `testIsPointInStrokeWithArcTo` + 10 related tests fail
- **Incorrect Hit Detection:** Points that should be on the stroke are reported as not on the stroke, and vice versa
- **Scope:** Only affects `isPointInStroke()` when arcTo is used; rendering is correct (JavaFX handles it natively)

## Fix

After line 607, adjust the extent based on the sweepFlag:

```java
double extent = Math.toDegrees(angle(ux, uy, vx, vy));

// Adjust sweep direction based on sweepFlag
// sweepFlag=true means clockwise (positive), sweepFlag=false means counter-clockwise (negative)
if (!sweepFlag && extent > 0) {
    extent -= 360;
} else if (sweepFlag && extent < 0) {
    extent += 360;
}
```

## Additional Considerations

The current implementation also doesn't handle the rotation (xAxisRotation) when appending the Arc2D. For rotated elliptical arcs, we would need to either:

1. Apply an AffineTransform to rotate the Arc2D, or
2. Approximate the rotated elliptical arc with cubic Bezier curves

For circular arcs (where radiusX == radiusY), rotation doesn't matter, so the current implementation works.

## Testing

To verify the fix:
1. Enable TestCanvas2D in `src/test/java/com/w3canvas/javacanvas/test/TestCanvas2D.java`
2. Run `testIsPointInStrokeWithArcTo`
3. Verify all 11 related tests pass

## References

- [SVG Arc Implementation Notes](https://www.w3.org/TR/SVG/implnote.html#ArcConversionEndpointToCenter)
- JavaFX ArcTo documentation
- AWT Arc2D documentation
