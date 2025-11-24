# DOM Trident Refactoring Plan

## Goal
Decouple DOM from Rhino's Context to enable cross-thread/cross-Context access for Worker/SharedWorker communication.

## Problem
- DOM nodes (Node, HTMLCanvasElement, etc.) extend ScriptableObject (Rhino-specific)
- Rhino Contexts are thread-local and isolated
- Objects created in Context A cannot have methods called on them from Context B
- This prevents Worker->Window communication where handlers run on different threads

## Solution: Follow Canvas 2D's "Trident" Architecture

### Phase 1: Core DOM Layer ✅ COMPLETE
- [x] Create interfaces (INode, IElement, IDocument)
- [x] Implement core classes (CoreNode, CoreElement, CoreDocument, CoreTextNode)
- [x] ElementFactory pattern for backend-specific element creation

### Phase 2: Partial Rhino Adapter ✅ PARTIAL
- [x] Document wraps CoreDocument
- [x] getElementById delegates to CoreDocument (works cross-Context)
- [x] RhinoNodeAdapter for registering Nodes in CoreDocument
- [ ] HTMLCanvasElement wrap CoreElement
- [ ] Image wrap CoreElement
- [ ] Other element types wrap CoreElement

### Phase 3: Complete Node Refactoring ⏳ IN PROGRESS
**Current blocker**: Node still extends ScriptableObject directly. Need to:

1. **Create CoreHTMLCanvasElement** extending CoreElement
   - Move canvas-specific logic from HTMLCanvasElement to core
   - Width, height, getContext, etc.

2. **Refactor HTMLCanvasElement** to wrap CoreHTMLCanvasElement
   - Thin Rhino wrapper extending ProjectScriptableObject
   - Delegates all operations to CoreHTMLCanvasElement
   - Similar to how CanvasRenderingContext2D wraps CoreCanvasRenderingContext2D

3. **Refactor Image** to wrap CoreImage
   - Move image loading logic to core
   - Rhino wrapper for JavaScript bindings

4. **Generic Element Wrapper**
   - For unknown element types
   - Wraps CoreElement with basic attribute/style support

## Why This Solves the Problem

After refactoring:
```
Thread A (Test):        Creates Window/document in Context A
Thread B (JavaFX):      Runs message handler in Context B

// Context B code:
var canvas = document.getElementById('canvas');  // ✅ Returns RhinoCanvasAdapter
var ctx = canvas.getContext('2d');               // ✅ Delegates to CoreHTMLCanvasElement (plain Java)
ctx.putImageData(data, 0, 0);                    // ✅ Works - core is not Context-bound
```

The RhinoCanvasAdapter is a Scriptable created in Context B, but it delegates to
CoreHTMLCanvasElement (plain Java), which is not Context-bound and works from any thread.

## Implementation Order

1. ✅ CoreHTMLCanvasElement (high priority - needed for test)
2. CoreImage (used by canvas)
3. Generic CoreElement wrapper for unknown types
4. Update NodeType factory to create wrappers
5. Test with all 149 existing tests
6. Test SharedWorker

## Files to Modify

- `src/main/java/com/w3canvas/javacanvas/core/dom/CoreHTMLCanvasElement.java` (NEW)
- `src/main/java/com/w3canvas/javacanvas/backend/rhino/impl/node/HTMLCanvasElement.java` (REFACTOR)
- `src/main/java/com/w3canvas/javacanvas/core/dom/CoreImage.java` (NEW)
- `src/main/java/com/w3canvas/javacanvas/backend/rhino/impl/node/Image.java` (REFACTOR)
- `src/main/java/com/w3canvas/javacanvas/backend/rhino/impl/node/NodeType.java` (UPDATE)

## Testing Strategy

1. Compile after each core class
2. Run Canvas2D tests after each Rhino adapter
3. Ensure all 149 tests still pass
4. Run SharedWorker test to verify cross-Context access
5. Commit frequently (after each working piece)
