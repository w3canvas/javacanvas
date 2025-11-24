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
    private static final boolean useJavaFX = isJavaFXAvailable();
    private static final boolean useSwing = !useJavaFX && isSwingAvailable();

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
        System.out.println("DEBUG: MainThreadEventLoop.queueTask() called, useJavaFX=" + useJavaFX + ", useSwing=" + useSwing + ", running=" + running);
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
