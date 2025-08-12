package com.w3canvas.javacanvas.backend.rhino.impl.node;

import com.w3canvas.javacanvas.interfaces.ICanvasPixelArray;
import org.mozilla.javascript.Scriptable;

public class CanvasPixelArray extends ProjectScriptableObject implements ICanvasPixelArray {

    private ICanvasPixelArray core;

    public CanvasPixelArray() {
    }

    public void init(ICanvasPixelArray core) {
        this.core = core;
    }

    public int[] getPixels(int dirtyX, int dirtyY, int dirtyWidth,
            int dirtyHeight) {
        return core.getPixels(dirtyX, dirtyY, dirtyWidth, dirtyHeight);
    }

    public Object get(int index, Scriptable start) {
        // This is not ideal, but it's a simple way to expose the array to Rhino
        return core.getPixels(0, 0, getWidth(), getHeight())[index];
    }

    public void put(int index, Scriptable start, Object value) {
        // This is not ideal, but it's a simple way to expose the array to Rhino
        int result = 0;
        if (value instanceof Double) {
            result = (((Double) value).intValue() & 0xFF);
        } else if (value instanceof Integer) {
            result = ((Integer) value & 0xFF);
        }
        core.getPixels(0, 0, getWidth(), getHeight())[index] = result;
    }

    public int jsGet_length() {
        return getWidth() * getHeight() * 4;
    }

    public int getWidth() {
        return core.getWidth();
    }

    public int getHeight() {
        return core.getHeight();
    }

}
