package com.w3canvas.javacanvas.rt;

/**
 * EventLoop abstraction for processing asynchronous tasks and messages.
 *
 * Each JavaScript execution context (main thread, Worker, SharedWorker) has
 * its own event loop. The event loop uses a BlockingQueue to queue tasks and
 * processes them in FIFO order.
 *
 * This matches the browser event loop model where:
 * - Main thread has an event loop that processes UI events and messages
 * - Each Worker has its own independent event loop
 * - Tasks are queued and the event loop blocks until work is available
 * - NO busy-waiting with Thread.sleep()
 */
public interface EventLoop {

    /**
     * Queue a task to be executed on this event loop.
     * The task will be executed asynchronously on the event loop's thread.
     *
     * @param task The task to execute
     */
    void queueTask(Runnable task);

    /**
     * Start the event loop thread.
     * The loop will block on the task queue until work arrives.
     */
    void start();

    /**
     * Stop the event loop thread.
     */
    void stop();

    /**
     * Check if the event loop is running.
     *
     * @return true if the event loop is running
     */
    boolean isRunning();
}
