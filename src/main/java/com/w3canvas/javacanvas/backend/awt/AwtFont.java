package com.w3canvas.javacanvas.backend.awt;

import com.w3canvas.javacanvas.interfaces.IFont;
import java.awt.Font;

public class AwtFont implements IFont {
    private final Font font;

    public AwtFont(Font font) {
        this.font = font;
    }

    public Font getFont() {
        return font;
    }
}
