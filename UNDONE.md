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

### Deferred / Limitations
- ⚠️ **JavaFX HSL modes**: hue, saturation, color, luminosity fall back to `source-over`.
  - **Status**: Explicitly handled in `JavaFXComposite.java` with comments explaining the limitation.
- ⚠️ **Porter-Duff Approximations**: `destination-in`, `destination-out`, `destination-atop`, `xor`, `copy` are approximated or fall back to `source-over` in JavaFX.
  - **Status**: Explicitly handled in `JavaFXComposite.java` with comments.

---

## Legacy JDK Support (Medium Priority)

### Problem
The codebase targets JDK 17+ but has a `-Plegacy` build mode for JDK 8. Some architectural cleanups remain.

### Deferred / Limitations
1. **JDK 8 Compilation**: Actual compilation on JDK 8 is not verified in CI (requires JDK 8 installation).
   - **Mitigation**: Gradle `-Plegacy` flag enforces language level 8.
2. **Native AOT**: `native-image` build is configured but not tested.
   - **Reason**: `native-image` tool is not available in the current environment.
   - **Status**: `reflect-config.json` has been updated with Rhino and JavaCanvas configuration.


