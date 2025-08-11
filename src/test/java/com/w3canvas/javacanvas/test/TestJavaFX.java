package com.w3canvas.javacanvas.test;

import com.w3canvas.javacanvas.backend.rhino.impl.node.HTMLCanvasElement;
import com.w3canvas.javacanvas.interfaces.ICanvasRenderingContext2D;
import com.w3canvas.javacanvas.rt.RhinoRuntime;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import javax.swing.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestJavaFX {

    private static RhinoRuntime runtime;

    @BeforeAll
    public static void init() {
        System.setProperty("w3canvas.backend", "javafx");
        runtime = new RhinoRuntime();
        Context.enter();
    }

    @Test
    public void testFillRect() throws Exception {
        Scriptable scope = runtime.getScope();
        HTMLCanvasElement canvas = (HTMLCanvasElement) Context.javaToJS(new HTMLCanvasElement(), scope);
        ScriptableObject.putProperty(scope, "canvas", canvas);

        ICanvasRenderingContext2D ctx = (ICanvasRenderingContext2D) canvas.jsFunction_getContext("2d");

        ctx.setFillStyle("red");
        ctx.fillRect(10, 10, 100, 100);

        int[] pixelData = ctx.getSurface().getPixelData(15, 15, 1, 1);
        assertEquals(0xFFFF0000, pixelData[0]);
    }
}
