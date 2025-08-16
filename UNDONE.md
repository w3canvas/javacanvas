# UNDONE

1.  **DONE** ~~Set up the environment by installing `xvfb`~~
2.  **DONE** ~~Figure out why the tests are failing and fix them.~~
3.  **TODO** The `isPointInStroke()` test is still failing. The conversion from a JavaFX `ArcTo` element to an AWT `Arc2D` is complex and likely has a bug. I have tried to fix it by correcting the center calculation of the arc, but it is still not working. I have pushed the current state of the code so that we can collaborate on this issue. The failing test is `testIsPointInStrokeWithArcTo` in `TestCanvas2D.java`. There are also 10 other failing tests that might be related.
4.  **TODO** Run the tests and make sure they pass.
