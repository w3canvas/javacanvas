# Architectural Refactoring Plan for JavaCanvas

## 1. Goals

- **Decouple Core Logic from Rhino:** Isolate the essential canvas implementation from the Rhino scripting engine.
- **Decouple Core Logic from AWT/Swing:** Abstract the rendering logic so that it is not tied to a specific UI toolkit.
- **Enable Extensibility:** Allow for future support of other UI backends (e.g., JavaFX) and potentially other scripting engines.
- **Improve Code Quality:** Increase maintainability and testability by establishing clear separation of concerns.

## 2. Proposed Architecture: The "Trident" Model

A three-layered architecture is proposed:

- **Layer 1: The Core API (`com.w3canvas.javacanvas.interfaces`)**
    - A set of pure Java interfaces that define the contract for the canvas system.
    - Key Interfaces: `INode`, `ICanvasRenderingContext2D`, `IImageData`, `IStyleHolder`, `IGraphicsBackend`, `ICanvasSurface`, `IGraphicsContext`.

- **Layer 2: The Core Implementation (`com.w3canvas.javacanvas.core`)**
    - A new package containing the default, pure Java implementation of the Core API.
    - It contains the "business logic" of the canvas and has no dependency on Rhino or any specific UI toolkit. For rendering, it will operate exclusively against the `IGraphicsBackend` interface.
    - Key Classes: `CoreNode`, `CoreCanvasRenderingContext2D`.

- **Layer 3: Backends & Adapters (`com.w3canvas.javacanvas.backend`)**
    - This layer contains the concrete implementations for specific technologies.
    - **`backend.awt`**: An implementation of the `IGraphicsBackend` interface using AWT/Swing.
    - **`backend.rhino`**: The existing `js.impl` classes will be refactored into this adapter layer. Their sole purpose will be to bridge the pure Java core to the Rhino scripting environment.
    - **`backend.javafx` (Future)**: A new JavaFX backend could be added here in the future.

## 3. Step-by-Step Refactoring Plan

1.  **Setup:**
    - Create the new package structure: `interfaces`, `core`, `backend/awt`, `backend/rhino`.
    - Add the new interface files that have been designed.

2.  **AWT Backend Extraction:**
    - Create the `AwtGraphicsBackend`, `AwtCanvasSurface`, and `AwtGraphicsContext` classes in the `backend.awt` package.
    - Move all AWT-specific rendering code from the existing classes (`CanvasRenderingContext2D`, `Image`, etc.) into this new backend.

3.  **Core Logic Extraction:**
    - Create the `CoreCanvasRenderingContext2D` class in the `core` package. It will implement `ICanvasRenderingContext2D` and will use the `IGraphicsBackend` interface for all rendering operations.
    - Create the `CoreNode`, `CoreImage`, etc. classes to hold the state and logic, free of external dependencies.

4.  **Rhino Adapter Refactoring:**
    - Modify the existing classes in `js.impl.node` to become "adapters".
    - Each adapter will hold an instance of its corresponding `Core` object (e.g., `RhinoNodeAdapter` will hold a `CoreNode`).
    - All core logic methods will be delegated to the `Core` object. The adapter's only job is to expose this logic to Rhino.

5.  **Final Integration:**
    - Modify the application's entry point (`JavaCanvas.java`) to wire the new architecture together. It will instantiate a specific backend (e.g., `AwtGraphicsBackend`) and inject it into the core implementation, which is then wrapped by the Rhino adapter layer.
