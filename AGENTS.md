# Agent Instructions for JavaCanvas

This document provides instructions for developers and agents working on the JavaCanvas project.

## Project State

This project is a **complete** Java implementation of the HTML5 Canvas API, supporting both AWT and JavaFX backends.
While some original JavaScript application logic files are missing, the Canvas 2D API implementation is fully functional and verified.

## Dependencies

To build and test this project in a headless environment (e.g. CI/CD), you will need to install `xvfb` to support JavaFX/AWT testing:

```bash
sudo apt-get update
sudo apt-get install xvfb
```

## Building and Testing

Use Maven wrapper:

```bash
./mvnw clean test
```

For headless environments, use `run-tests.sh`:

```bash
./run-tests.sh
```

Note: Tests include warmup logic to handle JavaFX initialization latency in headless environments.
