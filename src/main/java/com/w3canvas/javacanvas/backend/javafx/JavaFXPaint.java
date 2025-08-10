package com.w3canvas.javacanvas.backend.javafx;

import com.w3canvas.javacanvas.interfaces.IPaint;
import javafx.scene.paint.Paint;

public class JavaFXPaint implements IPaint {
    private final Paint paint;

    public JavaFXPaint(Paint paint) {
        this.paint = paint;
    }

    public Paint getPaint() {
        return paint;
    }
}
