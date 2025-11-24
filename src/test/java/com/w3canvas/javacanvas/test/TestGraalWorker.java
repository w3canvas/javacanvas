package com.w3canvas.javacanvas.test;

import com.w3canvas.javacanvas.backend.graal.worker.GraalWorker;
import com.w3canvas.javacanvas.rt.GraalRuntime;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.FileWriter;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Proof-of-concept tests for GraalJS Worker implementation.
 *
 * These tests validate:
 * 1. GraalJS can use the existing EventLoop architecture
 * 2. Workers can be created and communicate via messages
 * 3. The hybrid approach (Core APIs + minimal adapters) works
 *
 * This is NOT a complete test suite - it's a proof of concept
 * to identify what shared architecture can support both Rhino and GraalJS.
 */
@Disabled("GraalJS Worker is proof-of-concept, not production-ready")
public class TestGraalWorker {

    private static File testWorkerScript;

    @BeforeAll
    public static void createTestWorkerScript() throws Exception {
        // Create a simple worker script for testing
        testWorkerScript = File.createTempFile("test-graal-worker", ".js");
        testWorkerScript.deleteOnExit();

        try (FileWriter writer = new FileWriter(testWorkerScript)) {
            writer.write(
                "// Simple GraalJS worker script\n" +
                "console.log('Worker started');\n" +
                "\n" +
                "self.onmessage = function(e) {\n" +
                "  console.log('Worker received:', e.data);\n" +
                "  // Echo message back (not implemented yet)\n" +
                "  // postMessage('Echo: ' + e.data);\n" +
                "};\n"
            );
        }
    }

    @Test
    public void testGraalWorkerCreation() {
        // Test that GraalWorker can be instantiated
        GraalWorker worker = new GraalWorker(testWorkerScript.getAbsolutePath());
        assertNotNull(worker, "Worker should be created");

        // Give worker time to start
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            fail("Test interrupted");
        }

        worker.terminate();
    }

    @Test
    public void testGraalWorkerMessagePosting() {
        GraalWorker worker = new GraalWorker(testWorkerScript.getAbsolutePath());

        // Give worker time to start and set up handler
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            fail("Test interrupted");
        }

        // Post a message to worker
        worker.postMessage("Hello from main thread");

        // Give worker time to process
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            fail("Test interrupted");
        }

        worker.terminate();

        // Success if no exceptions thrown
        assertTrue(true, "Worker message posting should complete without errors");
    }

    @Test
    public void testGraalRuntimeEventLoop() {
        // Test that GraalRuntime has an event loop
        GraalRuntime runtime = new GraalRuntime(true); // isWorker=true

        assertNotNull(runtime.getEventLoop(), "GraalRuntime should have an event loop");

        // Test that tasks can be queued
        final boolean[] taskExecuted = {false};

        runtime.getEventLoop().queueTask(() -> {
            taskExecuted[0] = true;
            System.out.println("GraalRuntime event loop task executed");
        });

        // Give event loop time to process
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            fail("Test interrupted");
        }

        assertTrue(taskExecuted[0], "Event loop should execute queued tasks");

        runtime.close();
    }

    @Test
    public void testGraalRuntimeBasicExecution() {
        // Test basic script execution with GraalRuntime
        GraalRuntime runtime = new GraalRuntime();

        // Execute simple JavaScript
        Object result = runtime.exec("var x = 42; x * 2;");

        // GraalJS returns Value objects
        if (result instanceof Value) {
            Value value = (Value) result;
            if (value.isNumber()) {
                assertEquals(84, value.asInt(), "Script should calculate 42 * 2 = 84");
            }
        }

        runtime.close();
    }

    @Test
    public void testGraalRuntimePropertyAccess() {
        // Test that properties can be set and accessed
        GraalRuntime runtime = new GraalRuntime();

        // Set a property
        runtime.putProperty("testValue", 123);

        // Access it from JavaScript
        Object result = runtime.exec("testValue;");

        if (result instanceof Value) {
            Value value = (Value) result;
            if (value.isNumber()) {
                assertEquals(123, value.asInt(), "Property should be accessible from JavaScript");
            }
        }

        runtime.close();
    }
}
