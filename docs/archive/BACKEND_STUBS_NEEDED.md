# Backend Compilation Stubs Needed

The core layer implementation is complete, but the backends need stub implementations to compile.

## AwtGraphicsBackend.java

Add after `createRadialGradient()`:

```java
@Override
public ICanvasGradient createConicGradient(double startAngle, double x, double y) {
    // TODO: Implement proper conic gradient for AWT
    // For now, return a radial gradient as temporary fallback
    return new AwtRadialGradient(x, y, 0, x, y, 100, this);
}
```

## AwtGraphicsContext.java

Add after `rect()` method:

```java
@Override
public void roundRect(double x, double y, double w, double h, Object radii) {
    // TODO: Implement proper roundRect with corner radius parsing
    // For now, fall back to regular rect
    rect(x, y, w, h);
}
```

## JavaFXGraphicsBackend.java

Add after `createRadialGradient()`:

```java
@Override
public ICanvasGradient createConicGradient(double startAngle, double x, double y) {
    // TODO: Implement proper conic gradient for JavaFX
    // For now, return a radial gradient as temporary fallback
    return createRadialGradient(x, y, 0, x, y, 100);
}
```

## JavaFXGraphicsContext.java

Add after `rect()` method:

```java
@Override
public void roundRect(double x, double y, double w, double h, Object radii) {
    // TODO: Implement proper roundRect with corner radius parsing
    // Temporary: use regular rect
    rect(x, y, w, h);
}
```

## Why Stubs?

These stub implementations allow the code to compile and run while we implement the full functionality. The stubs:
- Provide basic fallback behavior
- Don't break existing functionality
- Can be replaced incrementally with full implementations

## Next Steps

1. Add these stubs to make the code compile
2. Commit the progress
3. Implement full backend support for each feature
4. Add comprehensive tests

## Testing the Stubs

Even with stubs, the following should work:
- ✅ Getting/setting shadow properties (stored but not rendered)
- ✅ Getting/setting image smoothing properties (stored but not applied)
- ✅ Getting/setting modern text properties (stored but not rendered)
- ✅ Calling roundRect() (falls back to rect())
- ✅ Creating conic gradients (falls back to radial gradients)
- ✅ save()/restore() correctly saves and restores all new properties
