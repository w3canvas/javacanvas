package com.w3canvas.javacanvas.backend.rhino.impl.node;

import com.w3canvas.javacanvas.interfaces.ICanvasPixelArray;
import com.w3canvas.javacanvas.interfaces.IImageData;

public class ImageData extends ProjectScriptableObject implements IImageData
{

    private IImageData core;

    public ImageData()
    {
    }

    public void init(IImageData core)
    {
        this.core = core;
    }

    public Integer jsGet_width()
    {
        return core.getWidth();
    }

    public Integer jsGet_height()
    {
        return core.getHeight();
    }

    public ICanvasPixelArray jsGet_data()
    {
        return core.getData();
    }

    @Override
    public int getWidth() {
        return core.getWidth();
    }

    @Override
    public int getHeight() {
        return core.getHeight();
    }

    @Override
    public ICanvasPixelArray getData() {
        return core.getData();
    }
}
