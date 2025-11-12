# UNDONE

1.  **DONE** ~~Set up the environment by installing `xvfb`~~
2.  **DONE** ~~Figure out why the tests are failing and fix them.~~
3.  **FIXED** ~~The `isPointInStroke()` test is still failing. The conversion from a JavaFX `ArcTo` element to an AWT `Arc2D` is complex and likely has a bug.~~
    - **Root Cause Identified:** The `convertFxPathToAwtPath()` method was not applying the correct sweep direction from the ArcTo `sweepFlag` parameter
    - **Fix Applied:** Added sweep direction adjustment in `JavaFXGraphicsContext.java:609-615`
    - **See:** `ARCTO_BUG_ANALYSIS.md` for detailed analysis
    - **Status:** Fix implemented, awaiting test verification
4.  **FIXED** ~~TestCanvas2D state management issues~~
    - **Root Cause Identified:** Thread-local Rhino Context confusion - setUp() entered Context on JUnit thread while tests used JavaFX thread
    - **Fix Applied:** Removed Context.enter/exit from setUp/tearDown in `TestCanvas2D.java`
    - **See:** `STATE_MANAGEMENT_BUG_ANALYSIS.md` for detailed analysis
    - **Status:** TestCanvas2D re-enabled (35 tests)
5.  **TODO** Run the tests and make sure they pass (requires Maven dependency resolution).
