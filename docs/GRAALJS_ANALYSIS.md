# GraalJS Worker Support - Architectural Analysis

## Current State

### What Works
- **Rhino Workers**: 100% functional (5/5 SharedWorker tests passing)
- **Event Loop Architecture**: MainThreadEventLoop and WorkerThreadEventLoop are backend-agnostic
- **Core Layer**: All core Canvas 2D APIs are backend-agnostic (no Rhino dependencies)
- **GraalRuntime**: Has basic event loop integration

### What Doesn't Work
- **GraalJS Workers**: Not implemented - Worker/SharedWorker classes are Rhino-specific
- **GraalJS DOM**: No GraalJS DOM adapters exist (Document, HTMLCanvasElement, etc.)
- **GraalJS Testing**: TestGraal is disabled, no Worker tests for GraalJS

## Technical Analysis

### Rhino-Specific Components

The following components are tightly coupled to Mozilla Rhino and cannot work with GraalJS:

#### 1. SharedWorker (src/main/java/com/w3canvas/javacanvas/js/worker/SharedWorker.java)
```java
public class SharedWorker extends ProjectScriptableObject {
    // Extends Rhino ScriptableObject
    // Uses Context.getCurrentContext()
    // Uses ScriptableObject.getClassPrototype()
    // Requires RhinoRuntime specifically
}
```

**Rhino Dependencies:**
- Extends `ProjectScriptableObject` (Rhino-specific)
- Uses `Context.getCurrentContext()` to get runtime
- Uses `ScriptableObject.getClassPrototype()` for prototype chain
- Constructor expects `RhinoRuntime` from thread-local storage

#### 2. MessagePort (src/main/java/com/w3canvas/javacanvas/js/worker/MessagePort.java)
```java
public class MessagePort extends ProjectScriptableObject {
    // Extends Rhino ScriptableObject
    // Uses Scriptable for scope and prototype
}
```

**Rhino Dependencies:**
- Extends `ProjectScriptableObject`
- Uses `Scriptable` for parent scope
- Uses `ScriptableObject.getClassPrototype()` for prototype registration
- Message handler is Rhino `Function` or `Callable`

#### 3. DOM Adapters (src/main/java/com/w3canvas/javacanvas/backend/rhino/impl/node/*)
```java
public class Document extends Node { ... }
public class HTMLCanvasElement extends Image { ... }
public class Window extends Node { ... }
```

**Rhino Dependencies:**
- All extend Rhino `ScriptableObject` hierarchy
- Use Rhino property accessors (jsGet_*, jsSet_*, jsFunction_*)
- Rely on Rhino prototype chain for method resolution
- Cannot be used with GraalJS polyglot bindings

### Backend-Agnostic Components (Already Working)

#### 1. Event Loop Architecture ✅
```java
// These are pure Java, no JS engine dependencies
public abstract class EventLoop { ... }
public class MainThreadEventLoop extends EventLoop { ... }
public class WorkerThreadEventLoop extends EventLoop { ... }
```

**Status:** Fully functional for both Rhino and GraalJS
- Event loops use `BlockingQueue` for messaging
- No JS engine-specific code
- Already integrated into both RhinoRuntime and GraalRuntime

#### 2. Core Layer ✅
```java
// All core implementations are backend-agnostic
public class CoreCanvasRenderingContext2D { ... }
public class CoreHTMLCanvasElement { ... }
public class ImageBitmap { ... }
public class Path2D { ... }
```

**Status:** Ready for GraalJS
- No dependencies on Rhino or GraalJS
- Can be exposed to either engine
- Thread-safe and Context-independent

## Architectural Approaches for GraalJS

### Option A: Mirror Rhino Architecture (Most JavaScript-Like)

Create GraalJS-specific adapters that mirror the Rhino structure:

```
src/main/java/com/w3canvas/javacanvas/
├── backend/
│   ├── rhino/          # Rhino adapters (existing)
│   │   ├── impl/
│   │   │   ├── node/
│   │   │   │   ├── Document.java (extends ScriptableObject)
│   │   │   │   ├── HTMLCanvasElement.java
│   │   │   │   └── Window.java
│   ├── graal/          # NEW: GraalJS adapters
│   │   ├── impl/
│   │   │   ├── node/
│   │   │   │   ├── Document.java (uses @HostAccess)
│   │   │   │   ├── HTMLCanvasElement.java
│   │   │   │   └── Window.java
├── js/
│   ├── worker/
│   │   ├── rhino/      # NEW: Move existing to rhino/
│   │   │   ├── SharedWorker.java
│   │   │   ├── Worker.java
│   │   │   └── MessagePort.java
│   │   ├── graal/      # NEW: GraalJS versions
│   │   │   ├── SharedWorker.java
│   │   │   ├── Worker.java
│   │   │   └── MessagePort.java
│   │   └── core/       # NEW: Shared logic
│   │       └── WorkerThread.java
```

**Example GraalJS Adapter:**
```java
package com.w3canvas.javacanvas.backend.graal.impl.node;

import org.graalvm.polyglot.HostAccess;
import com.w3canvas.javacanvas.core.dom.CoreHTMLCanvasElement;

public class HTMLCanvasElement {
    private final CoreHTMLCanvasElement core;

    public HTMLCanvasElement(int width, int height) {
        this.core = new CoreHTMLCanvasElement(width, height, null);
    }

    @HostAccess.Export
    public int getWidth() {
        return core.getWidth();
    }

    @HostAccess.Export
    public void setWidth(int width) {
        core.setWidth(width);
    }

    @HostAccess.Export
    public Object getContext(String contextType) {
        if ("2d".equals(contextType)) {
            return new CanvasRenderingContext2D(core);
        }
        return null;
    }
}
```

**Pros:**
- Most JavaScript-like API
- Clean separation between engines
- Can use proper JavaScript property access (with Value.putMember in GraalJS)
- Maintainable architecture

**Cons:**
- Significant implementation work (many classes to create)
- Code duplication between Rhino and GraalJS adapters
- Need to maintain two parallel hierarchies

### Option B: Direct Core API Exposure (Quickest)

Expose Core APIs directly to GraalJS without adapters:

```java
// In GraalRuntime initialization
public GraalRuntime() {
    this.context = Context.newBuilder("js")
        .allowHostAccess(HostAccess.ALL)
        .build();

    // Expose core APIs directly
    context.getBindings("js").putMember("CoreCanvasRenderingContext2D",
        CoreCanvasRenderingContext2D.class);
}
```

**JavaScript Usage:**
```javascript
// Less JavaScript-like, but functional
var ctx = new CoreCanvasRenderingContext2D(null, backend, 400, 400);
ctx.setFillStyle("red");  // Method call instead of property
ctx.fillRect(10, 10, 50, 50);
```

**Pros:**
- Minimal implementation work
- No code duplication
- Core APIs are already backend-agnostic

**Cons:**
- Not JavaScript-like (method calls instead of properties)
- No DOM-like API (document.getElementById, etc.)
- Workers would use Java APIs directly, not HTML5 Worker API
- Tests would need different JavaScript code

### Option C: Hybrid Approach (Recommended)

Use direct Core API exposure for now, with a plan to add GraalJS adapters later:

**Phase 1 (Immediate):**
1. Create minimal GraalJS Worker/SharedWorker using direct Core APIs
2. Test basic Worker functionality with GraalJS
3. Document differences between Rhino and GraalJS approaches
4. Identify shared vs engine-specific code

**Phase 2 (Future):**
1. Create GraalJS DOM adapters for better JavaScript compatibility
2. Refactor shared Worker logic into engine-agnostic core
3. Update JavaCanvas to choose adapters based on runtime

**Implementation Plan:**
```java
// Step 1: Create GraalWorker (simplified)
package com.w3canvas.javacanvas.backend.graal.worker;

import org.graalvm.polyglot.*;
import com.w3canvas.javacanvas.rt.GraalRuntime;

public class GraalWorker {
    private final GraalRuntime workerRuntime;
    private final Thread workerThread;

    public GraalWorker(String scriptUrl) {
        // Create worker runtime with event loop
        this.workerRuntime = new GraalRuntime(true); // isWorker=true

        // Start worker thread
        this.workerThread = new Thread(() -> {
            try {
                // Load and execute worker script
                java.io.File script = new java.io.File(scriptUrl);
                workerRuntime.exec(new java.io.FileReader(script), scriptUrl);
            } catch (Exception e) {
                System.err.println("Worker script failed: " + e.getMessage());
            }
        });
        workerThread.start();
    }

    @HostAccess.Export
    public void postMessage(Object data) {
        // Queue message to worker event loop
        workerRuntime.getEventLoop().queueTask(() -> {
            // Trigger onmessage in worker
            // TODO: Implement message delivery
        });
    }
}
```

## Required Changes for Full GraalJS Support

### 1. JavaCanvas Initialization

**Current (Rhino-only):**
```java
private void initializeCommon() {
    this.document = new Document();  // Always Rhino Document
    this.window = new Window();      // Always Rhino Window
    runtime.putProperty("document", this.document);
    runtime.putProperty("window", this.window);
}
```

**Proposed (Engine-Agnostic):**
```java
private void initializeCommon() {
    if (runtime instanceof RhinoRuntime) {
        initializeRhinoDOM((RhinoRuntime) runtime);
    } else if (runtime instanceof GraalRuntime) {
        initializeGraalDOM((GraalRuntime) runtime);
    }
}

private void initializeRhinoDOM(RhinoRuntime runtime) {
    // Create Rhino adapters
    com.w3canvas.javacanvas.backend.rhino.impl.node.Document document =
        new com.w3canvas.javacanvas.backend.rhino.impl.node.Document();
    // ... existing Rhino initialization
}

private void initializeGraalDOM(GraalRuntime runtime) {
    // Create GraalJS adapters OR expose Core APIs
    com.w3canvas.javacanvas.backend.graal.impl.node.Document document =
        new com.w3canvas.javacanvas.backend.graal.impl.node.Document();
    // ... GraalJS initialization
}
```

### 2. Worker API Factory Pattern

Create a factory to produce engine-specific Workers:

```java
package com.w3canvas.javacanvas.js.worker;

public interface WorkerFactory {
    Object createWorker(String scriptUrl);
    Object createSharedWorker(String scriptUrl);
    Object createMessagePort();
}

// Rhino implementation
public class RhinoWorkerFactory implements WorkerFactory { ... }

// GraalJS implementation
public class GraalWorkerFactory implements WorkerFactory { ... }
```

### 3. MessagePort Abstraction

Extract message handling logic from Rhino-specific MessagePort:

```java
package com.w3canvas.javacanvas.js.worker.core;

/**
 * Backend-agnostic message port implementation.
 * Handles message queueing, entanglement, and delivery.
 */
public class CoreMessagePort {
    private final BlockingQueue<Object> messageQueue;
    private CoreMessagePort entangledPort;
    private Object messageHandler;  // Engine-specific handler

    public void postMessage(Object message) {
        if (entangledPort != null) {
            entangledPort.queueMessage(message);
        }
    }

    public void entangle(CoreMessagePort other) {
        this.entangledPort = other;
        other.entangledPort = this;
    }

    // Engine-specific wrappers call these methods
    protected void queueMessage(Object message) {
        messageQueue.offer(message);
        if (messageHandler != null) {
            deliverMessages();
        }
    }

    protected abstract void deliverMessages();
}
```

## Prototype Registration Lessons from Rhino

### What We Learned

The Rhino Worker implementation revealed that **prototype registration is critical** for cross-Context method calling:

```java
// Required in RhinoRuntime
ScriptableObject.defineClass(scope, HTMLCanvasElement.class);

// Required in Document.createElement()
Scriptable proto = ScriptableObject.getClassPrototype(scope, "HTMLCanvasElement");
element.setPrototype(proto);
```

**Without prototypes:**
- `canvas.has('getContext')` returns `false`
- `canvas.getContext('2d')` throws "Cannot find default value"
- Methods are not accessible from JavaScript

### GraalJS Equivalent

GraalJS uses a different mechanism - `@HostAccess` annotations and Value bindings:

```java
public class HTMLCanvasElement {
    @HostAccess.Export
    public Object getContext(String type) { ... }
}

// In GraalRuntime
Value bindings = context.getBindings("js");
bindings.putMember("HTMLCanvasElement", new HTMLCanvasElement());
```

**Key Difference:**
- Rhino: Prototype chain determines method availability
- GraalJS: @HostAccess determines method visibility
- Both engines need explicit registration, just different mechanisms

## Shared Architecture Recommendations

### 1. Three-Layer Model (Trident Pattern)

Maintain the existing three layers, add engine-specific adapters:

```
Layer 1: Interfaces (ICanvasRenderingContext2D, etc.)
Layer 2: Core (CoreCanvasRenderingContext2D - backend-agnostic)
Layer 3: Adapters
  ├── backend/rhino/    # Rhino adapters (ScriptableObject)
  ├── backend/graal/    # GraalJS adapters (@HostAccess)
  └── backend/awt/      # Graphics backends (unchanged)
```

### 2. Event Loop (Already Shared) ✅

The event loop architecture is already engine-agnostic:
- `EventLoop`, `MainThreadEventLoop`, `WorkerThreadEventLoop`
- Both RhinoRuntime and GraalRuntime use the same event loop classes
- No changes needed

### 3. Worker Thread Management

Extract common Worker thread logic:

```java
package com.w3canvas.javacanvas.js.worker.core;

/**
 * Backend-agnostic worker thread.
 * Handles script execution on a dedicated thread with event loop.
 */
public abstract class WorkerThreadBase {
    protected final EventLoop eventLoop;
    protected final Thread thread;
    protected final String scriptUrl;

    public WorkerThreadBase(String scriptUrl) {
        this.scriptUrl = scriptUrl;
        this.eventLoop = new WorkerThreadEventLoop();
        this.thread = new Thread(this::run);
    }

    public void start() {
        eventLoop.start();
        thread.start();
    }

    protected abstract void run();  // Engine-specific implementation
}

// Rhino implementation
public class RhinoWorkerThread extends WorkerThreadBase {
    private final RhinoRuntime runtime;

    @Override
    protected void run() {
        Context.enter();
        try {
            runtime.exec(new FileReader(scriptUrl), scriptUrl);
        } finally {
            Context.exit();
        }
    }
}

// GraalJS implementation
public class GraalWorkerThread extends WorkerThreadBase {
    private final GraalRuntime runtime;

    @Override
    protected void run() {
        runtime.exec(new FileReader(scriptUrl), scriptUrl);
    }
}
```

### 4. Factory Pattern for Runtime-Specific Objects

```java
package com.w3canvas.javacanvas.rt;

public interface JSRuntime {
    // Existing methods
    Object exec(String script);
    void putProperty(String name, Object value);
    Object getScope();
    EventLoop getEventLoop();

    // NEW: Factory methods for runtime-specific objects
    Object createDocument();
    Object createWindow();
    Object createWorker(String scriptUrl);
    Object createSharedWorker(String scriptUrl);
}
```

## Testing Strategy

### Current Test Status
- **Rhino**: 5/5 SharedWorker tests passing (TestSharedWorker.java)
- **GraalJS**: 0 Worker tests (TestGraal.java is disabled)

### Proposed GraalJS Tests

#### Phase 1: Core API Tests
```java
@Test
public void testGraalWorkerWithCoreAPI() {
    GraalRuntime runtime = new GraalRuntime();

    // Expose Core APIs
    runtime.putProperty("CoreCanvasRenderingContext2D",
        CoreCanvasRenderingContext2D.class);

    // Test basic rendering in worker context
    runtime.exec("var ctx = new CoreCanvasRenderingContext2D(null, backend, 400, 400);");
    runtime.exec("ctx.setFillStyle('red');");
    runtime.exec("ctx.fillRect(10, 10, 50, 50);");

    // Verify rendering
}
```

#### Phase 2: Worker API Tests (after GraalWorker implementation)
```java
@Test
public void testGraalSharedWorkerBasicCommunication() {
    GraalRuntime runtime = new GraalRuntime();

    // Register GraalWorker constructor
    runtime.putProperty("SharedWorker", GraalSharedWorker.class);

    // Create worker
    runtime.exec("var worker = new SharedWorker('test/test-worker.js');");

    // Test message passing
    // ... similar to Rhino tests
}
```

## Effort Estimation

### Minimal GraalJS Support (Direct Core API)
- **Effort**: 1-2 days
- **Scope**:
  - Expose Core APIs to GraalJS
  - Create basic GraalWorker/GraalSharedWorker
  - Write proof-of-concept tests
- **Result**: Workers functional but not JavaScript-like

### Full GraalJS Support (DOM Adapters)
- **Effort**: 1-2 weeks
- **Scope**:
  - Create GraalJS DOM adapters (Document, HTMLCanvasElement, Window, etc.)
  - Implement GraalJS Worker/SharedWorker with HTML5 API
  - Refactor shared Worker logic into core
  - Full test suite for GraalJS
- **Result**: Feature parity with Rhino, JavaScript-like API

## Recommended Next Steps

1. **Document Current State** ✅ (this file)

2. **Proof of Concept**: Create minimal GraalWorker
   - File: `src/main/java/com/w3canvas/javacanvas/backend/graal/worker/GraalWorker.java`
   - Goal: Verify event loop and message passing work with GraalJS
   - Test: Simple worker that echoes messages back

3. **Identify Shared Code**: Extract common Worker logic
   - Create `WorkerThreadBase` abstraction
   - Create `CoreMessagePort` for message handling
   - Refactor Rhino Workers to use shared base

4. **Architecture Decision**: Choose between:
   - Option A: Full GraalJS DOM adapters (better API, more work)
   - Option B: Direct Core API exposure (quick, less JavaScript-like)
   - Option C: Hybrid (start with B, migrate to A)

5. **Update Documentation**:
   - Update CLAUDE.md with GraalJS guidance
   - Document engine-specific patterns
   - Create GraalJS development guide

## Conclusion

**Current State:**
- Rhino Workers are fully functional (100% test pass rate)
- Event loop architecture is engine-agnostic and ready
- Core APIs are backend-agnostic and can work with GraalJS
- GraalJS lacks DOM adapters and Worker implementations

**Recommended Approach:**
- **Hybrid approach (Option C)** for incremental development
- Start with direct Core API exposure for quick validation
- Gradually add GraalJS adapters for better JavaScript compatibility
- Extract shared Worker logic for code reuse

**Key Architectural Insight:**
The event loop and core layers are already properly abstracted. The main work is creating engine-specific adapters for the JavaScript API surface. The Rhino prototype registration lessons apply to GraalJS via @HostAccess annotations - both engines need explicit method exposure, just with different mechanisms.

---

*Analysis completed: 2025-11-24*
*Rhino Workers: 5/5 passing*
*GraalJS Workers: Not yet implemented*
