package com.w3canvas.javacanvas.rt;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Main thread event loop implementation.
 *
 * This event loop integrates with the UI thread (JavaFX or Swing) if available,
 * otherwise runs its own event loop thread. Uses BlockingQueue to queue tasks
 * and ensures no busy-waiting.
 *
 * Tasks queued via queueTask() are executed on the UI thread if JavaFX/Swing
 * is available, otherwise on a dedicated event loop thread.
 */
public class MainThreadEventLoop implements EventLoop {

    private final BlockingQueue<Runnable> taskQueue = new LinkedBlockingQueue<>();
    private Thread eventLoopThread;
    private volatile boolean running = false;
    private final boolean synchronousMode;
    private static final boolean useJavaFX = isJavaFXAvailable();
    private static final boolean useSwing = !useJavaFX && isSwingAvailable();

    /**
     * Create MainThreadEventLoop with default async mode.
     */
    public MainThreadEventLoop() {
        this(false);
    }

    /**
     * Create MainThreadEventLoop with optional synchronous mode.
     *
     * In synchronous mode, tasks are executed immediately on the calling thread
     * instead of being delegated to Platform.runLater() or a separate event loop.
     * This solves Rhino Context thread-locality issues in tests where all code
     * must execute in the same Context.
     *
     * @param synchronousMode if true, tasks run synchronously on calling thread (for tests)
     */
    public MainThreadEventLoop(boolean synchronousMode) {
        this.synchronousMode = synchronousMode;
    }

    /**
     * Check if JavaFX Platform is available.
     */
    private static boolean isJavaFXAvailable() {
        try {
            Class.forName("javafx.application.Platform");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * Check if Swing is available.
     */
    private static boolean isSwingAvailable() {
        try {
            Class.forName("javax.swing.SwingUtilities");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    @Override
    public void queueTask(Runnable task) {
        System.out.println("DEBUG: MainThreadEventLoop.queueTask() called, synchronousMode=" + synchronousMode + ", useJavaFX=" + useJavaFX + ", useSwing=" + useSwing + ", running=" + running);

        // In synchronous mode, execute immediately on calling thread ONLY if we're on the MAIN thread
        // This keeps everything in the same Rhino Context, solving thread-locality issues
        // But if we're called from a WORKER thread, we MUST NOT run synchronously because
        // the task needs the main thread's Context, not the worker's Context
        if (synchronousMode) {
            org.mozilla.javascript.Context cx = org.mozilla.javascript.Context.getCurrentContext();
            if (cx != null) {
                Object runtime = cx.getThreadLocal("runtime");
                // Only run synchronously if the calling thread's runtime is a MAIN THREAD runtime
                if (runtime instanceof RhinoRuntime && !((RhinoRuntime) runtime).isWorker()) {
                    System.out.println("DEBUG: Executing task synchronously on main thread: " + Thread.currentThread().getName());
                    task.run();
                    return;
                } else {
                    System.out.println("DEBUG: Called from worker thread, queueing to avoid Context mismatch");
                    // Fall through to normal queuing - task must run in main thread Context
                }
            } else {
                System.out.println("DEBUG: No Context on calling thread, queueing task");
                // Fall through to normal queuing
            }
        }

        if (useJavaFX) {
            // Delegate to JavaFX Platform.runLater()
            try {
                javafx.application.Platform.runLater(task);
                System.out.println("DEBUG: Task queued to JavaFX Platform.runLater()");
            } catch (IllegalStateException e) {
                // JavaFX not initialized, fall back to queue
                System.out.println("DEBUG: JavaFX not initialized, queueing to internal queue");
                taskQueue.offer(task);
            }
        } else if (useSwing) {
            // Delegate to Swing SwingUtilities.invokeLater()
            javax.swing.SwingUtilities.invokeLater(task);
            System.out.println("DEBUG: Task queued to Swing invokeLater()");
        } else {
            // Queue for our own event loop thread
            System.out.println("DEBUG: Task queued to internal queue, thread running=" + (eventLoopThread != null && eventLoopThread.isAlive()));
            taskQueue.offer(task);
        }
    }

    @Override
    public synchronized void start() {
        if (running) {
            return;  // Already started
        }

        // Only start our own thread if not using UI toolkit
        if (!useJavaFX && !useSwing) {
            running = true;
            eventLoopThread = new Thread(() -> {
                while (!Thread.currentThread().isInterrupted() && running) {
                    try {
                        // Block until a task is available - NO busy-waiting!
                        Runnable task = taskQueue.take();
                        task.run();  // Execute the task
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }, "MainEventLoop");
            eventLoopThread.setDaemon(true);
            eventLoopThread.start();
        } else {
            // Using UI toolkit, just mark as running
            running = true;
        }
    }

    @Override
    public synchronized void stop() {
        running = false;
        if (eventLoopThread != null) {
            eventLoopThread.interrupt();
            eventLoopThread = null;
        }
    }

    @Override
    public boolean isRunning() {
        return running;
    }
}
