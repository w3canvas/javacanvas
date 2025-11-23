package com.w3canvas.javacanvas.rt;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;

public class GraalRuntime implements JSRuntime {
    private Context context;

    public GraalRuntime() {
        this.context = Context.newBuilder("js")
                .allowHostAccess(HostAccess.newBuilder(HostAccess.ALL)
                        .targetTypeMapping(Double.class, Float.class, null, x -> x.floatValue())
                        .build())
                .build();
    }

    @Override
    public Object exec(String script) {
        try {
            return context.eval("js", script);
        } catch (Exception e) {
            throw new RuntimeException("Script execution failed", e);
        }
    }

    @Override
    public Object exec(java.io.Reader reader, String sourceName) {
        try {
            // Graal Context.eval accepts a Source object which can be built from a Reader
            org.graalvm.polyglot.Source source = org.graalvm.polyglot.Source.newBuilder("js", reader, sourceName)
                    .build();
            return context.eval(source);
        } catch (Exception e) {
            throw new RuntimeException("Script execution failed", e);
        }
    }

    @Override
    public void putProperty(String name, Object value) {
        context.getBindings("js").putMember(name, value);
    }

    @Override
    public void close() {
        context.close();
    }

    @Override
    public Object getScope() {
        return context.getBindings("js");
    }
}
