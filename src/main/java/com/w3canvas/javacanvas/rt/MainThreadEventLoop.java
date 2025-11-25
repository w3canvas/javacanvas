package com.w3canvas.javacanvas.rt;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class MainThreadEventLoop implements EventLoop {

    private final BlockingQueue<Runnable> taskQueue = new LinkedBlockingQueue<>();
    private Thread eventLoopThread;
    private volatile boolean running = false;
    private final boolean synchronousMode;
    private final IUIToolkit uiToolkit;

    public MainThreadEventLoop() {
        this(false, null);
    }

    public MainThreadEventLoop(boolean synchronousMode) {
        this(synchronousMode, null);
    }

    public MainThreadEventLoop(boolean synchronousMode, IUIToolkit uiToolkit) {
        this.synchronousMode = synchronousMode;
        if (uiToolkit == null) {
            this.uiToolkit = determineBestUIToolkit();
        } else {
            this.uiToolkit = uiToolkit;
        }
    }

    private IUIToolkit determineBestUIToolkit() {
        try {
            Class.forName("javafx.application.Platform");
            return (IUIToolkit) Class.forName("com.w3canvas.javacanvas.backend.javafx.JavaFXUIToolkit").getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            try {
                Class.forName("javax.swing.SwingUtilities");
                return new SwingUIToolkit();
            } catch (ClassNotFoundException e2) {
                return null;
            }
        }
    }

    @Override
    public void queueTask(Runnable task) {
        if (synchronousMode) {
            org.mozilla.javascript.Context cx = org.mozilla.javascript.Context.getCurrentContext();
            if (cx != null) {
                Object runtime = cx.getThreadLocal("runtime");
                if (runtime instanceof RhinoRuntime && !((RhinoRuntime) runtime).isWorker()) {
                    task.run();
                    return;
                }
            }
        }

        if (uiToolkit != null) {
            uiToolkit.invokeLater(task);
        } else {
            taskQueue.offer(task);
        }
    }

    @Override
    public synchronized void start() {
        if (running) {
            return;  // Already started
        }

        // Only start our own thread if not using UI toolkit
        if (uiToolkit == null) {
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
