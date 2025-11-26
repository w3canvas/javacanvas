package com.w3canvas.javacanvas.rt;

import java.io.Reader;

public interface JSRuntime {
    Object exec(String script);

    Object exec(Reader reader, String sourceName);

    void putProperty(String name, Object value);

    Object getProperty(String name);

    void close();

    void injectWebGPU();

    Object getScope(); // This might be tricky as Rhino returns Scriptable and Graal returns
                       // Context/Bindings
}
