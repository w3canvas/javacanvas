package com.w3canvas.javacanvas.backend.javafx;

import com.w3canvas.javacanvas.interfaces.IFont;
import javafx.scene.text.Font;

public class JavaFXFont implements IFont {
    private final Font font;

    public JavaFXFont(String family, double size) {
        this.font = new Font(family, size);
    }

    public Font getFont() {
        return font;
    }
}
