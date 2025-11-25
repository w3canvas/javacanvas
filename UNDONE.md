# UNDONE - Incomplete Features & Technical Debt

This file tracks features that are incomplete, approximated, or need implementation work.

## CSS Blend Modes (Mostly Complete)

### Problem
The Canvas 2D `globalCompositeOperation` property supports CSS blend modes that are not natively supported by AWT's `AlphaComposite` or fully by JavaFX's `BlendMode`.

### Current Status
**AWT Backend**: Implemented via `AwtBlendComposite.java` - a custom `java.awt.Composite` implementation that performs pixel-level blending per W3C spec.

| Blend Mode | AWT Status | JavaFX Status | Notes |
|------------|------------|---------------|-------|
| source-over | ✅ | ✅ | |
| source-in | ✅ | ✅ | |
| source-out | ✅ | ✅ | |
| source-atop | ✅ | ✅ | |
| destination-over | ✅ | ✅ | |
| destination-in | ✅ | ⚠️ Approx | |
| destination-out | ✅ | ⚠️ Approx | |
| destination-atop | ✅ | ⚠️ Approx | |
| lighter | ✅ AwtBlend | ✅ ADD | Additive blending |
| copy | ✅ | ⚠️ Approx | |
| xor | ✅ | ⚠️ Approx | |
| multiply | ✅ AwtBlend | ✅ | Via AwtBlendComposite |
| screen | ✅ AwtBlend | ✅ | Via AwtBlendComposite |
| overlay | ✅ AwtBlend | ✅ | Via AwtBlendComposite |
| darken | ✅ AwtBlend | ✅ | Via AwtBlendComposite |
| lighten | ✅ AwtBlend | ✅ | Via AwtBlendComposite |
| color-dodge | ✅ AwtBlend | ✅ | Via AwtBlendComposite |
| color-burn | ✅ AwtBlend | ✅ | Via AwtBlendComposite |
| hard-light | ✅ AwtBlend | ✅ | Via AwtBlendComposite |
| soft-light | ✅ AwtBlend | ✅ | Via AwtBlendComposite |
| difference | ✅ AwtBlend | ✅ | Via AwtBlendComposite |
| exclusion | ✅ AwtBlend | ✅ | Via AwtBlendComposite |
| hue | ✅ AwtBlend | ❌ Fallback | HSL blend implemented |
| saturation | ✅ AwtBlend | ❌ Fallback | HSL blend implemented |
| color | ✅ AwtBlend | ❌ Fallback | HSL blend implemented |
| luminosity | ✅ AwtBlend | ❌ Fallback | HSL blend implemented |

### Implementation Notes

1. **AWT Backend**: `AwtBlendComposite.java` (~450 lines) implements:
   - All standard CSS blend modes (multiply, screen, overlay, etc.)
   - HSL-based blend modes (hue, saturation, color, luminosity)
   - Proper alpha handling and premultiplied alpha conversion
   - Performance optimized with direct int[] pixel operations

2. **Reference**: W3C Compositing and Blending spec:
   https://www.w3.org/TR/compositing-1/#blending

### Remaining Work
- ✅ **Needs Build Verification**: AwtBlendComposite.java verified via `-Plegacy` build
- ⚠️ **JavaFX HSL modes**: hue, saturation, color, luminosity still fall back to source-over in JavaFX backend
- Consider visual regression tests for blend modes

### Files Modified
- `src/main/java/com/w3canvas/javacanvas/backend/awt/AwtComposite.java` - Routes blend modes to AwtBlendComposite
- `src/main/java/com/w3canvas/javacanvas/backend/awt/AwtBlendComposite.java` - NEW: Full blend mode implementation

---

## Legacy JDK Support (Medium Priority)

### Problem
The codebase targets JDK 17+ but has a `-Plegacy` build mode for JDK 8. Some architectural cleanups remain.

### Current Status
- ✅ `CompositeFactory.java` - Refactored to be backend-agnostic
- ✅ `CanvasRenderingContext2D.java` (Rhino) - Removed unused JavaFX imports
- ✅ Build tested with Gradle `-Plegacy`
- ❓ JDK 8 actual compilation not tested (but Gradle enforces language level)

### Remaining Work
1. Test actual compilation on JDK 8
2. Verify Gradle `-Plegacy` build works
3. Consider JDK 1.6 support (would require removing lambdas, streams, etc.)

---

## Test Coverage Gaps

### Font Rendering Tests
5 tests fail in CI due to font rendering differences:
- `testMaxWidthEdgeCases`
- `testMaxWidthScaling`
- `testTextAlignDetailed`
- `testTextBaselineDetailed`
- `testFontFace`

These are environment-dependent and may need:
- More tolerant pixel comparison
- Platform-specific golden masters
- Font installation in CI environment

---

## Native AOT (GraalVM)

### Status
Build configuration exists but not tested:
- `pom.xml` has `native` profile
- `build.gradle` has `graalvmNative` configuration

### Needs
- GraalVM JDK installation
- Reflection configuration for dynamic features
- Testing of native binary

---

## Documentation Gaps

- [ ] Update README with new build modes
- [ ] Document JBangRunner and JBangAwtRunner usage
- [ ] API documentation for new CompositeOperation enum
