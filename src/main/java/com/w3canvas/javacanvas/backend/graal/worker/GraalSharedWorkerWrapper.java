package com.w3canvas.javacanvas.backend.graal.worker;

import org.graalvm.polyglot.HostAccess;

import com.w3canvas.javacanvas.backend.graal.GraalRuntime;

/**
 * Wrapper for GraalSharedWorker to enable JavaScript 'new' operator usage.
 * GraalJS doesn't directly support 'new' on Java constructors, so we use
 * a static factory method that can be called from JavaScript.
 */
public class GraalSharedWorkerWrapper {

    /**
     * Create a new GraalSharedWorker instance.
     * Called from JavaScript via: new SharedWorker('script.js')
     *
     * @param scriptUrl The worker script URL
     * @param runtime The GraalRuntime instance
     * @return A new GraalSharedWorker
     */
    @HostAccess.Export
    public static GraalSharedWorker create(String scriptUrl, GraalRuntime runtime) {
        return new GraalSharedWorker(scriptUrl, runtime);
    }
}
