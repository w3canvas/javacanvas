package com.w3canvas.javacanvas.backend.javafx;

import com.w3canvas.javacanvas.interfaces.IComposite;
import javafx.scene.effect.BlendMode;

public class JavaFXComposite implements IComposite {
    private final BlendMode blendMode;

    public JavaFXComposite(BlendMode blendMode) {
        this.blendMode = blendMode;
    }

    public BlendMode getBlendMode() {
        return blendMode;
    }
}
