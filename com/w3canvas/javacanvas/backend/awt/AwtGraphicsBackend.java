package com.w3canvas.javacanvas.backend.awt;

import com.w3canvas.javacanvas.interfaces.ICanvasSurface;
import com.w3canvas.javacanvas.interfaces.IGraphicsBackend;

public class AwtGraphicsBackend implements IGraphicsBackend {

    @Override
    public ICanvasSurface createCanvasSurface(int width, int height) {
        return new AwtCanvasSurface(width, height);
    }
}
