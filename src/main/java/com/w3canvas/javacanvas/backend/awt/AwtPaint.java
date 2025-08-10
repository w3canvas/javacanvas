package com.w3canvas.javacanvas.backend.awt;

import com.w3canvas.javacanvas.interfaces.IPaint;
import java.awt.Paint;

public class AwtPaint implements IPaint {
    private final Paint paint;

    public AwtPaint(Paint paint) {
        this.paint = paint;
    }

    public Paint getPaint() {
        return paint;
    }
}
