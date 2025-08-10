package com.w3canvas.javacanvas.backend.awt;

import com.w3canvas.javacanvas.interfaces.IStroke;
import java.awt.Stroke;

public class AwtStroke implements IStroke {
    private final Stroke stroke;

    public AwtStroke(Stroke stroke) {
        this.stroke = stroke;
    }

    public Stroke getStroke() {
        return stroke;
    }
}
