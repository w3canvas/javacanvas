# UNDONE - Incomplete Features & Technical Debt

This file tracks features that are incomplete, approximated, or need implementation work.

## CSS Blend Modes (High Priority)

### Problem
The Canvas 2D `globalCompositeOperation` property supports CSS blend modes that are not natively supported by AWT's `AlphaComposite` or fully by JavaFX's `BlendMode`.

### Current Status
Both AWT and JavaFX backends fall back to `source-over` for unsupported blend modes:

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
| lighter | ⚠️ Approx | ✅ ADD | Should be additive blend |
| copy | ✅ | ⚠️ Approx | |
| xor | ✅ | ⚠️ Approx | |
| multiply | ❌ Fallback | ✅ | Needs custom AWT Composite |
| screen | ❌ Fallback | ✅ | Needs custom AWT Composite |
| overlay | ❌ Fallback | ✅ | Needs custom AWT Composite |
| darken | ❌ Fallback | ✅ | Needs custom AWT Composite |
| lighten | ❌ Fallback | ✅ | Needs custom AWT Composite |
| color-dodge | ❌ Fallback | ✅ | Needs custom AWT Composite |
| color-burn | ❌ Fallback | ✅ | Needs custom AWT Composite |
| hard-light | ❌ Fallback | ✅ | Needs custom AWT Composite |
| soft-light | ❌ Fallback | ✅ | Needs custom AWT Composite |
| difference | ❌ Fallback | ✅ | Needs custom AWT Composite |
| exclusion | ❌ Fallback | ✅ | Needs custom AWT Composite |
| hue | ❌ Fallback | ❌ Fallback | Needs custom implementation |
| saturation | ❌ Fallback | ❌ Fallback | Needs custom implementation |
| color | ❌ Fallback | ❌ Fallback | Needs custom implementation |
| luminosity | ❌ Fallback | ❌ Fallback | Needs custom implementation |

### Solution Path

1. **AWT Backend**: Implement custom `java.awt.Composite` classes that perform pixel-level blending:
   ```java
   public class AwtMultiplyComposite implements Composite {
       public CompositeContext createContext(...) {
           return new MultiplyCompositeContext();
       }
   }
   ```

2. **HSL Blend Modes** (hue, saturation, color, luminosity): Both backends need custom implementations that:
   - Convert RGB to HSL color space
   - Apply the blend operation
   - Convert back to RGB

3. **Reference Implementation**: See W3C Compositing and Blending spec:
   https://www.w3.org/TR/compositing-1/#blending

### Files to Modify
- `src/main/java/com/w3canvas/javacanvas/backend/awt/AwtComposite.java`
- `src/main/java/com/w3canvas/javacanvas/backend/javafx/JavaFXComposite.java`
- New: `src/main/java/com/w3canvas/javacanvas/backend/awt/AwtBlendComposite.java`

---

## Legacy JDK Support (Medium Priority)

### Problem
The codebase targets JDK 17+ but has a `-Plegacy` build mode for JDK 8. Some architectural cleanups remain.

### Current Status
- ✅ `CompositeFactory.java` - Refactored to be backend-agnostic
- ✅ `CanvasRenderingContext2D.java` (Rhino) - Removed unused JavaFX imports
- ⚠️ Build tested with Maven, needs Gradle verification
- ❓ JDK 8 actual compilation not tested

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
