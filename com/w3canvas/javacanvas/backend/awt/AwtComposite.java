package com.w3canvas.javacanvas.backend.awt;

import com.w3canvas.javacanvas.interfaces.IComposite;
import java.awt.Composite;

public class AwtComposite implements IComposite {
    private final Composite composite;

    public AwtComposite(Composite composite) {
        this.composite = composite;
    }

    public Composite getComposite() {
        return composite;
    }
}
