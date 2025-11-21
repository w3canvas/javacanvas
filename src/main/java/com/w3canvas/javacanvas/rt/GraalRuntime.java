package com.w3canvas.javacanvas.rt;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;

public class GraalRuntime {
    private Context context;

    public GraalRuntime() {
        this.context = Context.newBuilder("js")
            .allowHostAccess(HostAccess.newBuilder(HostAccess.ALL)
                .targetTypeMapping(Double.class, Float.class, null, x -> x.floatValue())
                .build())
            .build();
    }

    public Object exec(String script) {
        try {
            return context.eval("js", script);
        } catch (Exception e) {
            throw new RuntimeException("Script execution failed", e);
        }
    }

    public void putProperty(String name, Object value) {
        context.getBindings("js").putMember(name, value);
    }

    public void close() {
        context.close();
    }
}
