package com.w3canvas.javacanvas.backend.javafx;

import com.w3canvas.javacanvas.interfaces.IFont;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

public class JavaFXFont implements IFont {
    private final Font font;
    private final String family;
    private final double size;
    private final String style;
    private final String weight;

    public JavaFXFont(String family, double size) {
        this(family, size, "normal", "normal");
    }

    public JavaFXFont(String family, double size, String style, String weight) {
        this.family = family;
        this.size = size;
        this.style = style;
        this.weight = weight;
        this.font = createFont();
    }

    public JavaFXFont(Font font, double size, String style, String weight) {
        this.font = font;
        this.family = font.getFamily();
        this.size = size;
        this.style = style;
        this.weight = weight;
    }

    private Font createFont() {
        FontWeight fw = parseWeight(weight);
        FontPosture fp = parseStyle(style);
        return Font.font(family, fw, fp, size);
    }

    private FontWeight parseWeight(String weight) {
        if (weight == null) {
            return FontWeight.NORMAL;
        }
        switch (weight.toLowerCase()) {
            case "normal":
                return FontWeight.NORMAL;
            case "bold":
                return FontWeight.BOLD;
            case "bolder":
                return FontWeight.EXTRA_BOLD;
            case "lighter":
                return FontWeight.LIGHT;
            case "100":
                return FontWeight.THIN;
            case "200":
                return FontWeight.EXTRA_LIGHT;
            case "300":
                return FontWeight.LIGHT;
            case "400":
                return FontWeight.NORMAL;
            case "500":
                return FontWeight.MEDIUM;
            case "600":
                return FontWeight.SEMI_BOLD;
            case "700":
                return FontWeight.BOLD;
            case "800":
                return FontWeight.EXTRA_BOLD;
            case "900":
                return FontWeight.BLACK;
            default:
                return FontWeight.NORMAL;
        }
    }

    private FontPosture parseStyle(String style) {
        if (style == null) {
            return FontPosture.REGULAR;
        }
        switch (style.toLowerCase()) {
            case "normal":
                return FontPosture.REGULAR;
            case "italic":
            case "oblique":
                return FontPosture.ITALIC;
            default:
                return FontPosture.REGULAR;
        }
    }

    public Font getFont() {
        return font;
    }

    public String getFamily() {
        return family;
    }

    public double getSize() {
        return size;
    }

    public String getStyle() {
        return style;
    }

    public String getWeight() {
        return weight;
    }
}
