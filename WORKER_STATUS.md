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

## Remaining Issue: Rhino Context Thread-Locality

### The Problem
Rhino's `Context` is thread-local and isolated. Objects created in one Context cannot be accessed from another Context, even if:
- We reuse the same scope
- We set the runtime in thread local
- We're on the same or different threads

### Current Situation
1. Main thread script runs on "Test worker" thread (id=1), creates `document`
2. Message handler queued to MainThreadEventLoop
3. MainThreadEventLoop uses Platform.runLater(), runs on "JavaFX Application Thread" (id=33)
4. NEW Context is created (getCurrentContext() returns null on JavaFX thread)
5. New Context can't access `document` from old Context
6. Error: "TypeError: Cannot find default value for object" when calling `document.getElementById()`

### Why This Happens
- Test executes script on "Test worker" thread
- Platform.runLater() executes on "JavaFX Application Thread"
- These are different threads with different Contexts
- Rhino doesn't allow cross-Context object access

### Potential Solutions

**Option 1: Run message handlers synchronously on same thread**
- Don't use Platform.runLater() for main thread in tests
- Keep handler on same thread as script execution
- Pros: Would work for tests
- Cons: Breaks async event loop model, not realistic

**Option 2: Make document/window available in all Contexts**
- Store document/window in a thread-safe global registry
- When new Context is created, re-inject these objects
- Pros: Maintains async model
- Cons: Complex, may have serialization issues

**Option 3: Use GraalJS instead of Rhino**
- GraalJS may have better cross-thread object sharing
- Pros: Modern engine, better performance
- Cons: Different API, requires refactoring

**Option 4: Keep all main thread work on one thread**
- Don't delegate to Platform.runLater() at all
- Run MainThreadEventLoop on the script execution thread
- Pros: Simpler, avoids Context issues
- Cons: Doesn't integrate with UI event loop

## Next Steps

1. **Test with GraalJS** - See if it handles cross-thread better
2. **Implement synchronous mode for tests** - Add flag to disable Platform.runLater()
3. **Document injection** - Try re-injecting document/window in new Contexts
4. **Architecture review** - May need fundamental redesign for Rhino's limitations

## Architecture Summary

The event-driven architecture is **sound and correct** per HTML5 Worker spec:
- ✅ Event loops block on queue until work arrives (no busy-waiting)
- ✅ Messages queue immediately and process when handler is ready
- ✅ Worker and main thread have independent event loops
- ✅ Proper FIFO message ordering
- ✅ Integration with UI toolkit event loops (JavaFX/Swing)

The issue is **Rhino-specific** and relates to its Context thread-locality design, not the event loop architecture itself.
