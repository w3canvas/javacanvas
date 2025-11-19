package com.w3canvas.javacanvas.js.worker;

import java.awt.EventQueue;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.mozilla.javascript.Callable;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import com.w3canvas.javacanvas.backend.rhino.impl.node.ProjectScriptableObject;
import com.w3canvas.javacanvas.rt.RhinoRuntime;

@SuppressWarnings("serial")
public class Worker extends ProjectScriptableObject {

    private Function onmessage;
    private WorkerThread workerThread;

    public void jsConstructor(String scriptUrl) {
        RhinoRuntime mainRuntime = (RhinoRuntime) Context.getCurrentContext().getThreadLocal("runtime");
        this.workerThread = new WorkerThread(mainRuntime, scriptUrl);
        this.workerThread.start();

        // Start a thread to poll for messages from the worker
        new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Object data = workerThread.getFromWorkerQueue().take();
                    // Post the onmessage event to the AWT event queue
                    EventQueue.invokeLater(() -> {
                        if (onmessage != null) {
                            Context cx = Context.enter();
                            try {
                                Scriptable scope = getParentScope();
                                Scriptable event = cx.newObject(scope);
                                event.put("data", event, data);
                                onmessage.call(cx, scope, this, new Object[]{event});
                            } finally {
                                Context.exit();
                            }
                        }
                    });
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }).start();
    }

    @Override
    public String getClassName() {
        return "Worker";
    }

    public void jsFunction_postMessage(Object data) {
        if (workerThread != null) {
            try {
                workerThread.getToWorkerQueue().put(data);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public void jsSet_onmessage(Function onmessage) {
        this.onmessage = onmessage;
    }

    public Function jsGet_onmessage() {
        return onmessage;
    }

    private class WorkerThread extends Thread {
        private final BlockingQueue<Object> toWorkerQueue = new LinkedBlockingQueue<>();
        private final BlockingQueue<Object> fromWorkerQueue = new LinkedBlockingQueue<>();
        private final RhinoRuntime mainRuntime;
        private final String scriptUrl;

        public WorkerThread(RhinoRuntime mainRuntime, String scriptUrl) {
            this.mainRuntime = mainRuntime;
            this.scriptUrl = scriptUrl;
        }

        public BlockingQueue<Object> getToWorkerQueue() {
            return toWorkerQueue;
        }

        public BlockingQueue<Object> getFromWorkerQueue() {
            return fromWorkerQueue;
        }

        @Override
        public void run() {
            // Set context classloader to ensure inner classes can be loaded
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());

            RhinoRuntime workerRuntime = new RhinoRuntime();
            Context workerContext = Context.enter();
            workerContext.putThreadLocal("runtime", workerRuntime);
            Scriptable workerScope = workerRuntime.getScope();

            try {
                ScriptableObject.defineClass(workerScope, OffscreenCanvas.class);
                ScriptableObject.defineClass(workerScope, com.w3canvas.javacanvas.backend.rhino.impl.node.CanvasRenderingContext2D.class);
                ScriptableObject.defineClass(workerScope, com.w3canvas.javacanvas.backend.rhino.impl.node.ImageData.class);
                ScriptableObject.defineClass(workerScope, com.w3canvas.javacanvas.backend.rhino.impl.node.TextMetrics.class);
                ScriptableObject.defineClass(workerScope, com.w3canvas.javacanvas.backend.rhino.impl.node.DOMMatrix.class);
                ScriptableObject.defineClass(workerScope, com.w3canvas.javacanvas.backend.rhino.impl.node.Blob.class);
                ScriptableObject.defineClass(workerScope, com.w3canvas.javacanvas.backend.rhino.impl.node.ImageBitmap.class);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            ScriptableObject.putProperty(workerScope, "console", new com.w3canvas.javacanvas.utils.ScriptLogger());

            // Define postMessage in the worker's global scope
            ScriptableObject.putProperty(workerScope, "postMessage", new Callable() {
                @Override
                public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
                    if (args.length > 0) {
                        try {
                            fromWorkerQueue.put(args[0]);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                    return null;
                }
            });

            // Poll for messages from the main thread
            new Thread(() -> {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        Object data = toWorkerQueue.take();
                        Function onmessage = (Function) workerScope.get("onmessage", workerScope);
                        if (onmessage != null) {
                            Scriptable event = workerContext.newObject(workerScope);
                            event.put("data", event, data);
                            onmessage.call(workerContext, workerScope, workerScope, new Object[]{event});
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }).start();

            try {
                // Load the script from the classpath
                InputStreamReader reader = new InputStreamReader(Worker.class.getClassLoader().getResourceAsStream(scriptUrl));
                workerRuntime.exec(reader, scriptUrl);
            } catch (Exception e) {
                System.err.println("ERROR: Worker failed to load script '" + scriptUrl + "': " + e.getMessage());
            } finally {
                Context.exit();
            }
        }
    }
}
