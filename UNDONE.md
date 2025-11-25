# UNDONE - Incomplete Features & Technical Debt

This file tracks features that are incomplete, approximated, or need implementation work.

## CSS Blend Modes (Mostly Complete)

### Problem
The Canvas 2D `globalCompositeOperation` property supports CSS blend modes that are not natively supported by AWT's `AlphaComposite` or fully by JavaFX's `BlendMode`.

### Current Status
**AWT Backend**: Implemented via `AwtBlendComposite.java` - a custom `java.awt.Composite` implementation that performs pixel-level blending per W3C spec.
**JavaFX Backend**: Implemented via `JavaFXBlendRenderer.java` - performs pixel-level blending for unsupported modes.

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
| hue | ✅ AwtBlend | ✅ Custom | Via JavaFXBlendRenderer |
| saturation | ✅ AwtBlend | ✅ Custom | Via JavaFXBlendRenderer |
| color | ✅ AwtBlend | ✅ Custom | Via JavaFXBlendRenderer |
| luminosity | ✅ AwtBlend | ✅ Custom | Via JavaFXBlendRenderer |

### Implementation Notes

1. **AWT Backend**: `AwtBlendComposite.java` (~450 lines) implements all standard CSS blend modes using direct int[] pixel operations.
2. **JavaFX Backend**: `JavaFXBlendRenderer.java` implements fallback pixel-level blending for HSL modes and others not supported by JavaFX `BlendMode`.

### Deferred / Limitations
- None. JavaFX backend now supports all CSS blend modes and Porter-Duff operations via `JavaFXBlendRenderer`.

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
