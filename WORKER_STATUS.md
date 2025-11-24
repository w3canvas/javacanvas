# Worker API Implementation Status

## Completed Work

### Event-Driven EventLoop Architecture ✅
- **EventLoop interface** - Abstraction for queuing Runnable tasks
- **MainThreadEventLoop** - Integrates with JavaFX Platform.runLater() or Swing invokeLater()
- **WorkerThreadEventLoop** - Dedicated thread with BlockingQueue.take() (NO busy-waiting!)
- Both event loops use `BlockingQueue<Runnable>` and block until work arrives
- **NO Thread.sleep() loops anywhere** - proper event-driven architecture

### MessagePort Message Buffering ✅
- MessagePort has its own `messageQueue` (LinkedBlockingQueue)
- Messages can be queued IMMEDIATELY, even before `handlerRuntime` is set
- When `onmessage` is set, all pending messages are drained and processed
- Handles race condition where messages arrive before worker is fully initialized

### IWindowHost Abstraction ✅
- Created `IWindowHost` interface to abstract AWT/Swing vs JavaFX
- `SwingWindowHost` implementation wraps `RootPaneContainer`
- Document, Node, HTMLCanvasElement updated to use IWindowHost
- Proper null checks and instanceof checks for backend-specific code

### Integration
- RhinoRuntime and GraalRuntime create and start EventLoops
- SharedWorker uses WorkerThreadEventLoop
- Main thread uses MainThreadEventLoop
- SharedWorker worker thread parks on Object.wait() instead of busy-looping

## Test Results

Messages now flow correctly between threads:
```
DEBUG: jsSet_onmessage called on thread: Test worker, id=1
DEBUG: postMessage() called, message queued
DEBUG: Worker onmessage called on thread: Thread-8
DEBUG: Processing 1 pending messages
DEBUG: drainTask executing on thread: WorkerEventLoop
DEBUG: Message handler called successfully
DEBUG: Response sent back to main thread
DEBUG: Main thread drainTask on JavaFX Application Thread
```

## Remaining Issue: Rhino Context Thread-Locality (CRITICAL)

### The Problem
Rhino's `Context` is thread-local and isolated. **Scriptable objects created in one Context cannot have methods called on them from another Context**, even if:
- We inject the object reference into the new Context's scope ✅ TRIED - DOESN'T WORK
- We reuse the same scope
- We set the runtime in thread local
- We're on the same or different threads

### Current Situation (After GlobalScope Injection Attempt)
1. Main thread script runs on "Test worker" thread (id=1), creates `document` as Scriptable object
2. JavaCanvas stores `document` and `window` in RhinoRuntime fields
3. Message handler queued to MainThreadEventLoop
4. MainThreadEventLoop uses Platform.runLater(), runs on "JavaFX Application Thread" (id=36)
5. NEW Context is created (getCurrentContext() returns null on JavaFX thread)
6. RhinoRuntime.ensureMainThreadGlobals() injects `document` and `window` into new Context's scope
7. **Rhino blocks method calls**: `document` reference is accessible, but calling `document.getElementById()` fails
8. Error: "TypeError: Cannot find default value for object" at line 11 of test-sharedworker-main.js

### Why Injection Doesn't Work
- The `document` object is a Scriptable (extends ScriptableObject) created in the original Context
- Rhino internally checks if method calls on Scriptable objects are from the same Context
- Cross-Context method calls are blocked by Rhino's security/isolation design
- Simply having the reference in scope is not sufficient

### Root Cause: DOM Tightly Coupled to Rhino
The current DOM implementation (Document, Node, Element, etc.) is located in `com.w3canvas.javacanvas.backend.rhino.impl.node` and extends ScriptableObject. This makes it inherently Context-bound.

### Potential Solutions (Ordered by Viability)

**Option 1: Refactor DOM to use "Trident" Architecture** ⭐ RECOMMENDED
- Extract DOM logic to backend-agnostic core layer (like Canvas 2D)
- Create `com.w3canvas.javacanvas.core.dom` with interface-based design
- Create Rhino and GraalJS adapters
- Pros: Clean architecture, enables GraalJS support, follows project patterns
- Cons: Significant refactoring effort (but well-defined based on existing Canvas work)
- Status: This aligns with user's observation about DOM coupling to Rhino

**Option 2: MainThreadEventLoop Reuses Same Context**
- Store the original Context in RhinoRuntime
- MainThreadEventLoop reuses that Context instead of creating new ones
- Pros: Minimal code changes, preserves async model
- Cons: May have threading issues with Rhino, Context might not be thread-safe

**Option 3: Synchronous MainThreadEventLoop for Tests**
- Add flag to disable Platform.runLater() in test environments
- Main thread event loop runs synchronously on same thread
- Pros: Quick fix for tests, maintains architecture for production
- Cons: Tests don't exercise real async behavior

**Option 4: Test with GraalJS**
- Switch to GraalJS engine for tests
- GraalJS may not have Context thread-locality restrictions
- Pros: Modern engine, better performance, may "just work"
- Cons: Requires GraalJS adapters for DOM (ties into Option 1)

**Option 5: Context-Agnostic Wrapper Pattern**
- Create wrapper objects that delegate to original objects via reflection
- Wrappers are created fresh in each Context
- Pros: Doesn't require full refactor
- Cons: Complex, performance overhead, may not work with Rhino's restrictions

## Attempted Solutions

### 1. Global Scope Injection ❌ FAILED
- Stored document/window in RhinoRuntime fields
- Injected references into new Context's scope
- **Result**: References accessible, but Rhino blocks method calls on Scriptable objects from different Context
- **Reason**: Rhino's internal security checks prevent cross-Context method invocations

### 2. Synchronous Mode ❌ PARTIALLY FAILED
- Added `synchronousMode` flag to MainThreadEventLoop
- Tasks run immediately on calling thread if on main thread
- Checks thread-local runtime to avoid running on worker thread
- **Result**: Works for same-runtime calls, but cross-runtime calls still fail
- **Reason**: When worker sends message to main thread, handler must run in main thread Context, not worker Context. Queuing it normally brings us back to the original Context isolation problem.

## Conclusion

**Rhino's Context thread-locality is a fundamental architectural limitation that cannot be worked around without significant refactoring.**

The issue is not with the event loop architecture (which is correct per HTML5 spec), but with DOM objects (Document, Node, Element) being tightly coupled to Rhino's ScriptableObject, which is Context-bound.

## Recommended Path Forward: DOM Trident Refactoring ⭐

Following the successful Canvas 2D "Trident" architecture pattern:

### Phase 1: Extract Core DOM Layer
- Create `com.w3canvas.javacanvas.core.dom` package
- Define interfaces: `ICoreDocument`, `ICoreElement`, `ICoreNode`, etc.
- Implement backend-agnostic DOM logic (tree structure, event dispatch, etc.)

### Phase 2: Create Rhino Adapter
- `com.w3canvas.javacanvas.backend.rhino.dom`
- Thin wrappers that extend ScriptableObject
- Delegate to core DOM implementations
- Similar to how CanvasRenderingContext2D wraps CoreCanvasRenderingContext2D

### Phase 3: Add GraalJS Support
- `com.w3canvas.javacanvas.backend.graal.dom`
- GraalJS polyglot adapters
- Enables testing if GraalJS handles cross-thread better

### Benefits
- ✅ Decouples DOM logic from JavaScript engine
- ✅ Enables GraalJS support (may not have Context thread-locality)
- ✅ Follows established project architecture patterns
- ✅ Makes DOM testable independent of JS engine
- ✅ Aligns with user's observation about DOM coupling to Rhino

## Architecture Summary

The event-driven architecture is **sound and correct** per HTML5 Worker spec:
- ✅ Event loops block on queue until work arrives (no busy-waiting)
- ✅ Messages queue immediately and process when handler is ready
- ✅ Worker and main thread have independent event loops
- ✅ Proper FIFO message ordering
- ✅ Integration with UI toolkit event loops (JavaFX/Swing)

The issue is **Rhino-specific** and relates to its Context thread-locality design, not the event loop architecture itself.
