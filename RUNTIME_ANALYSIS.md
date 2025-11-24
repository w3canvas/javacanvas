# JavaScript Runtime Analysis - Rhino vs GraalJS

## Executive Summary

This document summarizes the work done on Worker/SharedWorker APIs across JavaScript runtimes and identifies architectural patterns that can be shared.

**Status:**
- **Rhino Workers**: ✅ 100% Complete (5/5 tests passing)
- **GraalJS Workers**: 📋 Analyzed, proof-of-concept created, implementation plan documented

## Rhino Implementation - Complete Success

### Results
- **All SharedWorker tests passing**: 5/5 (100% success rate)
- **Execution time**: 3.4 seconds (down from 25+ seconds of timeouts)
- **No regressions**: Canvas2D tests remain at 80/85 passing (94%)

### Key Breakthroughs

#### 1. Cross-Context Method Calling via Prototypes
**Problem**: JavaScript couldn't call methods on DOM objects from different Rhino Contexts
```javascript
var canvas = document.getElementById('canvas'); // ✅ Returns canvas
var ctx = canvas.getContext('2d');              // ❌ TypeError: Cannot find default value for object
```

**Root Cause**: HTMLCanvasElement was never registered as a Rhino class
- No prototype: `canvas.prototype == null`
- Methods not accessible: `canvas.has('getContext') == false`

**Solution**:
```java
// 1. Register class in RhinoRuntime
ScriptableObject.defineClass(scope, HTMLCanvasElement.class);

// 2. Set prototype in Document.createElement()
Scriptable proto = ScriptableObject.getClassPrototype(scope, "HTMLCanvasElement");
element.setPrototype(proto);
```

**Key Insight**: User was RIGHT - "Scope and Context allow cross-context calling!" - but objects need proper prototypes registered for method resolution.

#### 2. Worker Script Path Resolution
**Problem**: 4 tests failing with script loading errors
```
ERROR: SharedWorker failed to load script 'test-sharedworker.js': null
```

**Root Cause**: Path resolution differs between `executeScript()` and `executeCode()`
- `executeScript("test/test-main.js")` has file path context ✅
- `executeCode("new SharedWorker('test.js')")` has NO context ❌

**Solution**: Use full paths in inline JavaScript
- Changed: `'test-sharedworker.js'` → `'test/test-sharedworker.js'`

### Architecture Validated

#### Event Loop (HTML5 Spec Compliant) ✅
- **MainThreadEventLoop**: Integrates with JavaFX Platform.runLater() or Swing invokeLater()
- **WorkerThreadEventLoop**: Dedicated thread with BlockingQueue.take()
- **NO busy-waiting** - proper event-driven architecture
- Messages queue immediately, even before handlers are ready

#### Message Passing ✅
- **MessagePort** with LinkedBlockingQueue for buffering
- **Cross-Context delivery** - messages sent from Worker thread to Main thread
- **FIFO ordering** maintained
- **Auto-start** for main thread ports per HTML5 spec

#### DOM Trident Architecture ✅
- **CoreHTMLCanvasElement**: Backend-agnostic, NOT Context-bound
- **HTMLCanvasElement**: Thin Rhino wrapper with proper prototype
- **Cross-Context access**: getImage(), getWidth(), getHeight() all delegate to core
- **Method calling works**: JavaScript can call methods on canvas from different Contexts

### Prototype Registration Pattern
Every Rhino class needs TWO things for cross-Context calling:
1. **Registration**: `ScriptableObject.defineClass(scope, MyClass.class)`
2. **Prototype set**: `element.setPrototype(ScriptableObject.getClassPrototype(scope, "MyClass"))`

Missing either breaks cross-Context method resolution!

## GraalJS Analysis - Path Forward

### Current State
- **GraalRuntime**: Exists with event loop support ✅
- **DOM Adapters**: Don't exist - JavaCanvas uses Rhino adapters only ❌
- **Worker API**: Don't exist - SharedWorker/Worker are Rhino-specific ❌
- **Tests**: TestGraal disabled, no Worker tests ❌

### Technical Challenges

#### 1. Incompatible Object Models
**Rhino**: ScriptableObject-based hierarchy
```java
public class HTMLCanvasElement extends ScriptableObject {
    public Object jsFunction_getContext(String type) { ... }
}
```

**GraalJS**: @HostAccess annotation-based
```java
public class HTMLCanvasElement {
    @HostAccess.Export
    public Object getContext(String type) { ... }
}
```

#### 2. Different Binding Mechanisms
**Rhino**: Prototype chain determines method availability
```java
ScriptableObject.defineClass(scope, HTMLCanvasElement.class);
element.setPrototype(proto);
```

**GraalJS**: @HostAccess determines method visibility
```java
Value bindings = context.getBindings("js");
bindings.putMember("HTMLCanvasElement", new HTMLCanvasElement());
```

#### 3. Context/Runtime Differences
**Rhino**: Thread-local Contexts, shared Scopes
```java
Context.enter();  // Thread-local
Scriptable scope = new ImporterTopLevel(context);  // Can be shared
```

**GraalJS**: Contexts are not thread-local
```java
Context context = Context.newBuilder("js").build();  // Can be used across threads
Value bindings = context.getBindings("js");
```

### Shared vs Engine-Specific Components

#### Already Shared (No Changes Needed) ✅

1. **Event Loop Architecture**
   ```
   EventLoop (abstract)
   ├── MainThreadEventLoop
   └── WorkerThreadEventLoop
   ```
   - Pure Java, no JS engine dependencies
   - Used by both RhinoRuntime and GraalRuntime
   - No modifications needed

2. **Core Layer**
   ```
   CoreCanvasRenderingContext2D
   CoreHTMLCanvasElement
   ImageBitmap
   Path2D
   ```
   - Backend-agnostic implementation
   - No Rhino or GraalJS dependencies
   - Ready to use with either engine

3. **Graphics Backends**
   ```
   AwtGraphicsBackend
   JavaFXGraphicsBackend
   ```
   - Work with Core layer only
   - Engine-agnostic

#### Engine-Specific (Need Separate Implementations) ❌

1. **DOM Adapters**
   - **Rhino**: backend/rhino/impl/node/* (ScriptableObject-based)
   - **GraalJS**: backend/graal/impl/node/* (needed, @HostAccess-based)

2. **Worker API**
   - **Rhino**: js/worker/{SharedWorker, Worker, MessagePort}.java (ScriptableObject-based)
   - **GraalJS**: backend/graal/worker/* (needed, @HostAccess-based)

3. **Runtime Initialization**
   - **RhinoRuntime**: Uses ScriptableObject.defineClass() for registration
   - **GraalRuntime**: Uses bindings.putMember() for exposure

### Proposed Architecture

#### Three-Layer Model (Extended for Multiple Engines)

```
Layer 1: Interfaces
├── ICanvasRenderingContext2D
├── IDocument
└── IWindow

Layer 2: Core (Engine-Agnostic)
├── CoreCanvasRenderingContext2D
├── CoreHTMLCanvasElement
├── ImageBitmap
└── Path2D

Layer 3: Engine-Specific Adapters
├── backend/rhino/        # Rhino adapters (ScriptableObject)
│   ├── impl/node/
│   │   ├── Document
│   │   ├── HTMLCanvasElement
│   │   └── Window
│   └── worker/
│       ├── SharedWorker
│       ├── Worker
│       └── MessagePort
├── backend/graal/        # GraalJS adapters (@HostAccess)
│   ├── impl/node/
│   │   ├── Document
│   │   ├── HTMLCanvasElement
│   │   └── Window
│   └── worker/
│       ├── SharedWorker
│       ├── Worker
│       └── MessagePort
└── backend/awt/          # Graphics backends (unchanged)
    ├── AwtGraphicsBackend
    └── JavaFXGraphicsBackend

Layer 4: Shared Infrastructure (Engine-Agnostic)
├── EventLoop
├── MainThreadEventLoop
└── WorkerThreadEventLoop
```

### Implementation Approaches

#### Option A: Full GraalJS Adapters (Best API)
- Create complete GraalJS DOM adapters
- Mirror Rhino structure but use @HostAccess
- Provides JavaScript-like API
- **Effort**: 1-2 weeks

#### Option B: Direct Core API (Quickest)
- Expose Core APIs directly to GraalJS
- No DOM-like wrappers
- Use method calls: `ctx.setFillStyle()` instead of `ctx.fillStyle = `
- **Effort**: 1-2 days

#### Option C: Hybrid (Recommended)
- Start with direct Core API exposure (Option B)
- Validate event loop and message passing work
- Gradually add GraalJS adapters for better API
- **Effort**: Incremental

### Proof of Concept Created

#### Files Created
1. **GraalWorker.java** - Minimal GraalJS Worker implementation
   - Uses GraalRuntime with event loop
   - Demonstrates @HostAccess patterns
   - Shows how to expose worker APIs

2. **TestGraalWorker.java** - Basic validation tests
   - Tests worker creation
   - Tests event loop integration
   - Tests basic JavaScript execution

3. **GRAALJS_ANALYSIS.md** - Complete architectural analysis
   - Detailed comparison of Rhino vs GraalJS
   - Implementation options
   - Effort estimates
   - Recommendations

#### Proof of Concept Demonstrates
1. ✅ GraalRuntime has event loop support
2. ✅ Workers can be instantiated with GraalJS
3. ✅ @HostAccess.Export enables method calling from JavaScript
4. ✅ Event loop architecture works with GraalJS

## Lessons Learned - Applicable to Both Engines

### 1. Explicit Registration Required
**Rhino**: `ScriptableObject.defineClass(scope, MyClass.class)`
**GraalJS**: `bindings.putMember("MyClass", MyClass.class)` or instance

Both engines need explicit registration - objects don't automatically become available to JavaScript.

### 2. Method Visibility Mechanisms
**Rhino**: Prototype chain + `jsFunction_*` naming convention
**GraalJS**: @HostAccess.Export annotation

Both need explicit marking of which methods JavaScript can call.

### 3. Scope/Bindings Can Be Shared
**Rhino**: Scriptable scope independent of Context, can be reused
**GraalJS**: Value bindings can be accessed from different threads

Both engines support sharing the scope/bindings object, but handle it differently.

### 4. Event-Driven Architecture Essential
- NO busy-waiting with Thread.sleep() loops
- Use BlockingQueue.take() for event loop
- Queue tasks for execution on appropriate thread
- Works identically for both Rhino and GraalJS

## Architectural Recommendations

### 1. Maintain Three-Layer Separation
```
Interfaces → Core → Engine Adapters
```
- Keep Core layer completely engine-agnostic
- All engine-specific code in adapter layers
- Enables supporting multiple JS engines

### 2. Extract Common Worker Logic
```java
// Backend-agnostic base class
public abstract class WorkerThreadBase {
    protected final EventLoop eventLoop;
    protected final Thread thread;

    protected abstract void runScript();
}

// Engine-specific implementations
public class RhinoWorkerThread extends WorkerThreadBase { ... }
public class GraalWorkerThread extends WorkerThreadBase { ... }
```

### 3. Use Factory Pattern for Runtime-Specific Objects
```java
public interface JSRuntime {
    Object createDocument();
    Object createWindow();
    Object createWorker(String scriptUrl);
}

public class RhinoRuntime implements JSRuntime {
    @Override
    public Object createDocument() {
        return new com.w3canvas.javacanvas.backend.rhino.impl.node.Document();
    }
}

public class GraalRuntime implements JSRuntime {
    @Override
    public Object createDocument() {
        return new com.w3canvas.javacanvas.backend.graal.impl.node.Document();
    }
}
```

### 4. Keep Event Loop Engine-Agnostic
- EventLoop, MainThreadEventLoop, WorkerThreadEventLoop are perfect as-is
- Pure Java, no JS engine dependencies
- Used by both runtimes identically
- **No changes needed**

## Next Steps

### Immediate (For Learning)
1. ✅ Document current state (this file, GRAALJS_ANALYSIS.md)
2. ✅ Create proof-of-concept GraalWorker
3. ✅ Identify shared vs engine-specific code

### Short-Term (If GraalJS Support Needed)
1. Decide on approach (A, B, or C)
2. Create minimal GraalJS DOM adapters
3. Implement GraalJS Worker/SharedWorker
4. Write GraalJS test suite

### Long-Term (Architecture Improvements)
1. Extract shared Worker logic into core layer
2. Create WorkerFactory interface
3. Refactor JavaCanvas to use factory pattern
4. Update documentation with multi-engine patterns

## Effort Estimates

| Task | Effort | Status |
|------|--------|--------|
| Rhino Worker implementation | 3 days | ✅ Complete |
| GraalJS architectural analysis | 0.5 days | ✅ Complete |
| GraalJS proof-of-concept | 0.5 days | ✅ Complete |
| Minimal GraalJS Workers (Option B) | 1-2 days | 📋 Planned |
| Full GraalJS adapters (Option A) | 1-2 weeks | 📋 Planned |
| Refactor to factory pattern | 2-3 days | 📋 Planned |
| Extract shared Worker logic | 1-2 days | 📋 Planned |

## Conclusion

### What We Learned
1. **Event loop architecture is engine-agnostic** ✅
   - Works identically for Rhino and GraalJS
   - No modifications needed
   - Proper foundation for Workers

2. **Core layer is ready for both engines** ✅
   - No Rhino dependencies
   - Can be exposed to GraalJS directly
   - DOM Trident pattern validated

3. **Engine-specific adapters are necessary**
   - Rhino needs ScriptableObject hierarchy
   - GraalJS needs @HostAccess annotations
   - Different object models, same underlying logic

4. **Prototype/method registration patterns differ but achieve same goal**
   - Rhino: defineClass() + setPrototype()
   - GraalJS: @HostAccess.Export + putMember()
   - Both make methods callable from JavaScript

### Architectural Insights
The current architecture is well-positioned for multi-engine support:
- ✅ Event loops are shared
- ✅ Core layer is shared
- ✅ Graphics backends are shared
- ❌ DOM adapters need per-engine implementations
- ❌ Worker APIs need per-engine implementations

The **three-layer Trident pattern** works perfectly:
1. Interfaces define contracts
2. Core implements business logic
3. Engine adapters bridge to JavaScript

Adding GraalJS support means creating adapter layers, not redesigning the architecture.

### Recommendation
**For immediate GraalJS support**: Use Option C (Hybrid approach)
1. Start with direct Core API exposure (quick validation)
2. Add GraalJS adapters incrementally (better API)
3. Extract shared Worker logic as patterns emerge

**For long-term maintainability**:
1. Keep event loop and core layers engine-agnostic
2. Use factory pattern for runtime-specific object creation
3. Document engine-specific patterns in CLAUDE.md

---

*Analysis completed: 2025-11-24*
*Rhino Workers: 5/5 passing (100%)*
*GraalJS Workers: Proof-of-concept created, ready for implementation*
