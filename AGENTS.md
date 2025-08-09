# Agent Instructions for JavaCanvas

This document provides instructions for developers and agents working on the JavaCanvas project.

## Project State

This project is an implementation of the HTML5 Canvas API in Java. The Java source code is present, but the corresponding JavaScript files, which appear to contain the main application logic, are missing from the repository.

To make the project buildable, a stub class for `com.w3canvas.javacanvas.js.impl.node.CanvasRenderingContext2D` was created. Additionally, the call to load the JavaScript files in `JavaCanvas.java` has been commented out.

## Dependencies

To build and test this project in a headless environment, you will need to install `xvfb`:

```bash
sudo apt-get update
sudo apt-get install xvfb
```

## Building the Project

A build script, `build.sh`, has been provided to compile the Java source code.

To build the project, run the following command:

```bash
./build.sh
```

The compiled class files will be placed in the `bin` directory.

## Running the Application

A run script, `run.sh`, has been provided to run the application. This script uses `xvfb-run` to handle the AWT/Swing GUI in a headless environment.

To run the application, use the following command:

```bash
./run.sh
```

Note that the application will not have its intended functionality due to the missing JavaScript files. It will simply open a blank window.

## Running the Tests

A simple smoke test has been created to verify that the application can be initialized without crashing. The test is located in `com/w3canvas/javacanvas/test/TestCanvas.java`.

A script, `test.sh`, has been provided to build and run the test.

To run the test, use the following command:

```bash
./test.sh
```

The script will launch the application, wait for 5 seconds, and then terminate it. A successful test run indicates that the application can start up without immediately crashing.
