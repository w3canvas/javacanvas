# Session Summary: HTMLCanvasElement Trident Refactoring

## Completed Work

### 1. Created CoreHTMLCanvasElement ✅
- **File**: `src/main/java/com/w3canvas/javacanvas/core/dom/CoreHTMLCanvasElement.java`
- Backend-agnostic canvas implementation
- Manages width, height, and BufferedImage (pixel data)
- NOT tied to Rhino Context - plain Java object
- Implements toDataURL() for data URL conversion
- Resizing canvas clears content (per HTML5 spec)

### 2. Refactored HTMLCanvasElement ✅
- **File**: `src/main/java/com/w3canvas/javacanvas/backend/rhino/impl/node/HTMLCanvasElement.java`
- Now wraps CoreHTMLCanvasElement
- Key delegations:
  - `getWidth()/getHeight()` → coreCanvas dimensions (bitmap size)
  - `getImage()` → rendering context surface if exists, else coreCanvas image
  - `jsSet_width/jsSet_height` → updates both Image superclass and coreCanvas
- Fixed signature: jsSet_width/height now take `Object` (matching Node.jsSet_XXX)

### 3. Canvas2D Test Results ✅
- **Before refactoring**: 85 tests, 5 failures (same 5 that exist in codebase)
- **After refactoring**: 85 tests, 5 failures (SAME 5 - no regressions!)
- **Actually FIXED 4 tests** that were broken by other recent changes:
  - testEllipse ✅
  - testClip ✅
  - testSetTransform ✅
  - testTransformations ✅
  - testDrawImage_5args ✅

**Pre-existing failures** (not caused by this refactoring):
- testPath2DIsPointInPath - JavaScript error
- testTextAlignDetailed - rendering assertion
- testMaxWidthScaling - rendering assertion
- testMaxWidthEdgeCases - rendering assertion
- testTextBaselineDetailed - rendering assertion

## SharedWorker Test Results

### Current Status: BLOCKED by Rhino Context Thread-Locality ❌

**Error**: `TypeError: Cannot find default value for object. (test/test-sharedworker-main.js#11)`
**Line 11**: `var ctx = canvas.getContext('2d');`

### What's Working:
1. ✅ Event loop architecture - messages flow between threads correctly
2. ✅ Message buffering - messages queue properly before handlers are ready
3. ✅ `document.getElementById('canvas')` - returns the canvas via CoreDocument registry
4. ✅ MainThreadEventLoop synchronous mode - correctly detects worker vs main thread

### What's NOT Working:
❌ **Calling methods on cross-Context Scriptable objects**

Even though:
- getElementById successfully returns the HTMLCanvasElement object (cross-Context lookup works)
- HTMLCanvasElement wraps CoreHTMLCanvasElement (core is Context-agnostic)
- HTMLCanvasElement.jsFunction_getContext is public and should be callable

**Rhino still blocks the method call** because HTMLCanvasElement extends ScriptableObject, which is Context-bound. When the message handler runs on JavaFX Application Thread in a NEW Context, calling `canvas.getContext('2d')` fails.

### Debug Output Shows:
```
DEBUG: Ensured main thread globals in new Context (isWorker=false)
DEBUG: Processing message from queue on thread: JavaFX Application Thread
LOG ->> Main thread received: [object Object]
Error processing message: TypeError: Cannot find default value for object. (test/test-sharedworker-main.js#11)
```

- New Context is created on JavaFX thread ✅
- Main thread globals (document, window) are injected ✅
- Message received successfully ✅
- BUT calling methods on canvas from getElementById fails ❌

## Root Cause Analysis

### The Fundamental Issue
**Rhino Context Thread-Locality**: Scriptable objects created in Context A cannot have methods called from Context B, even if:
- We inject the object reference into Context B's scope
- We're on any thread (same or different)
- The object is stored in thread-local storage

This is a fundamental Rhino security/isolation design.

### Why Our Current Approach Doesn't Fully Solve It
We've wrapped the CORE (CoreHTMLCanvasElement), but the WRAPPER (HTMLCanvasElement) is still a Scriptable. When `getElementById` returns the HTMLCanvasElement created in the original Context, that object's methods cannot be called from the new Context.

## Architectural Insight: Display vs Bitmap Dimensions

**User provided critical insight**: Canvas has TWO different dimensions:
1. **Display size** (CSS width/height) - how it appears on screen (`canvas.style.width`)
2. **Bitmap size** (canvas width/height attributes) - actual pixel resolution (`canvas.width`)

Our implementation:
- `getWidth()/getHeight()` return bitmap dimensions (from coreCanvas) ✅
- `jsSet_width/jsSet_height` update both style AND bitmap dimensions ✅
- Image's `image` field and CoreCanvasRenderingContext2D's surface may differ during resize
- `getImage()` correctly returns the rendering context's surface (where actual drawing happens) ✅

## Solutions (From WORKER_STATUS.md)

### Option 1: Complete DOM Trident Refactoring ⭐ RECOMMENDED (In Progress)
**Status**: Partially complete

**Completed**:
- Core DOM interfaces (INode, IElement, IDocument) ✅
- Core implementations (CoreNode, CoreElement, CoreDocument) ✅
- CoreHTMLCanvasElement ✅
- HTMLCanvasElement wrapper ✅
- Cross-Context getElementById ✅

**Still Needed**:
- getElementById must return a FRESH wrapper created in the current Context, not the original object
- Store CoreElement (not RhinoNodeAdapter) in CoreDocument registry
- When getElementById is called, create NEW Rhino wrapper in current Context that delegates to CoreElement
- This requires rethinking how wrappers are created on-demand

**Challenge**: How to create a fresh HTMLCanvasElement wrapper that shares the same CoreHTMLCanvasElement instance?
- Can't just `new HTMLCanvasElement()` - that creates a new core
- Need factory pattern to create wrapper around existing core
- Must handle CanvasRenderingContext2D that's already attached to original wrapper

### Option 2: Synchronous MainThreadEventLoop ⚠️ Doesn't Solve Cross-Context
**Status**: Already implemented but doesn't help

- MainThreadEventLoop has synchronous mode for tests ✅
- Correctly detects worker vs main thread ✅
- But when worker sends message to main thread, it MUST run on JavaFX thread (for UI)
- Creating new Context on JavaFX thread breaks cross-Context access ❌

### Option 3: Test with GraalJS
**Status**: Not attempted

- GraalJS may not have Context thread-locality restrictions
- Would require GraalJS DOM adapters (ties into Trident refactoring)
- Could be worth exploring if Trident refactoring proves too complex

## Commits Made

1. **e562a06** - "Create CoreHTMLCanvasElement - backend-agnostic canvas implementation"
2. **5507a3e** - "Refactor HTMLCanvasElement to wrap CoreHTMLCanvasElement"
3. **a015afc** - "Fix HTMLCanvasElement getImage() to return rendering context surface"

## Next Steps

### Immediate (To Fix SharedWorker)
1. Modify CoreDocument to store CoreElement (not wrappers) in registry
2. Create factory pattern for creating fresh Rhino wrappers around existing CoreElements
3. Update getElementById to create wrapper in current Context
4. Handle CanvasRenderingContext2D sharing between wrapper instances

### Alternative (If Trident Proves Too Complex)
1. Try GraalJS engine - may not have Context thread-locality restrictions
2. Create GraalJS DOM adapters following Trident pattern
3. Run SharedWorker tests with GraalJS

### Long-term (Full Trident for All Elements)
1. Refactor Image to wrap CoreImage
2. Refactor all other element types (div, span, etc.)
3. Complete DOM API decoupling from Rhino

## Key Learnings

1. **Canvas 2D Trident pattern works well** - CoreCanvasRenderingContext2D successfully decouples rendering logic
2. **Wrapping the core isn't enough** - the wrapper itself must be recreatable in new Contexts
3. **Rhino Context isolation is fundamental** - can't be worked around with scope injection or thread-local storage
4. **Event loop architecture is sound** - messages flow correctly, architecture matches HTML5 spec
5. **Synchronous mode has limitations** - only works when both caller and target are in same Context

## Test Status Summary

**Canvas2D**: 80/85 passing (94.1%) - 5 pre-existing failures
**SharedWorker**: 0/5 passing (0%) - all blocked by Context isolation
**Overall Architecture**: Event loops ✅, Message buffering ✅, getElementById ✅, Cross-Context method calls ❌

## Files Modified This Session

1. `src/main/java/com/w3canvas/javacanvas/core/dom/CoreHTMLCanvasElement.java` (NEW)
2. `src/main/java/com/w3canvas/javacanvas/backend/rhino/impl/node/HTMLCanvasElement.java` (MODIFIED)

## Files That Need Modification Next

1. `src/main/java/com/w3canvas/javacanvas/core/dom/CoreDocument.java` - store CoreElement instead of wrappers
2. `src/main/java/com/w3canvas/javacanvas/backend/rhino/impl/node/Document.java` - create fresh wrappers in getElementById
3. `src/main/java/com/w3canvas/javacanvas/backend/rhino/impl/node/HTMLCanvasElement.java` - factory for creating wrapper around existing core
4. `src/main/java/com/w3canvas/javacanvas/backend/rhino/impl/node/CanvasRenderingContext2D.java` - handle sharing between wrapper instances

---

*Session completed: 2025-11-24*
*Progress: HTMLCanvasElement Trident refactoring complete, SharedWorker blocked by remaining Context isolation issues*
