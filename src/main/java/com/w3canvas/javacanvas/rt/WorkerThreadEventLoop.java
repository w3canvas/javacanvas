package com.w3canvas.javacanvas.rt;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Worker thread event loop implementation.
 *
 * This event loop runs on a dedicated worker thread and processes tasks from a
 * BlockingQueue. The event loop blocks on queue.take() until work arrives,
 * ensuring no busy-waiting or Thread.sleep() loops.
 *
 * Each Worker/SharedWorker gets its own WorkerThreadEventLoop that processes
 * messages and other async tasks in FIFO order.
 */
public class WorkerThreadEventLoop implements EventLoop {

    private final BlockingQueue<Runnable> taskQueue = new LinkedBlockingQueue<>();
    private Thread eventLoopThread;
    private volatile boolean running = false;

    @Override
    public void queueTask(Runnable task) {
        taskQueue.offer(task);
    }

    @Override
    public synchronized void start() {
        if (running) {
            return;  // Already started
        }

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
        }, "WorkerEventLoop");
        eventLoopThread.setDaemon(true);
        eventLoopThread.start();
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
