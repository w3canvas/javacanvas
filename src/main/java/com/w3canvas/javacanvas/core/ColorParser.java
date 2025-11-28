package com.w3canvas.javacanvas.core;

import com.w3canvas.javacanvas.interfaces.IGraphicsBackend;
import com.w3canvas.javacanvas.interfaces.IPaint;

public class ColorParser {
    public static IPaint parse(String colorString, IGraphicsBackend backend) {
        if (backend != null) {
            return backend.createPaint(colorString);
        }
        return null;
    }
}
