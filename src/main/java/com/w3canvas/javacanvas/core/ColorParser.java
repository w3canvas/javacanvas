package com.w3canvas.javacanvas.core;

import com.w3canvas.javacanvas.backend.awt.AwtPaint;
import com.w3canvas.javacanvas.interfaces.IPaint;
import java.awt.Color;

public class ColorParser {
    public static IPaint parse(String colorString) {
        if (colorString.startsWith("#")) {
            return new AwtPaint(Color.decode(colorString));
        } else {
            try {
                return new AwtPaint((Color) Color.class.getField(colorString.toUpperCase()).get(null));
            } catch (Exception e) {
                return new AwtPaint(Color.BLACK);
            }
        }
    }
}
