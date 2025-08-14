package com.w3canvas.javacanvas.backend.awt;

import com.w3canvas.javacanvas.interfaces.IFont;
import java.awt.Font;

public class AwtFont implements IFont {
    private final Font font;
    private final String family;
    private final float size;
    private final String style;
    private final String weight;

    public AwtFont(String family, float size, String style, String weight) {
        this.family = family;
        this.size = size;
        this.style = style;
        this.weight = weight;
        this.font = createFont();
    }

    private Font createFont() {
        int awtStyle = parseStyleAndWeight(style, weight);
        return new Font(family, awtStyle, (int) size);
    }

    private int parseStyleAndWeight(String style, String weight) {
        int awtStyle = Font.PLAIN;
        if (style != null && (style.equalsIgnoreCase("italic") || style.equalsIgnoreCase("oblique"))) {
            awtStyle |= Font.ITALIC;
        }
        if (weight != null && (weight.equalsIgnoreCase("bold") || isBoldWeight(weight))) {
            awtStyle |= Font.BOLD;
        }
        return awtStyle;
    }

    private boolean isBoldWeight(String weight) {
        if (weight.equalsIgnoreCase("bolder")) return true;
        try {
            int w = Integer.parseInt(weight);
            return w >= 700;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public Font getFont() {
        return font;
    }

    public String getFamily() {
        return family;
    }

    public float getSize() {
        return size;
    }

    public String getStyle() {
        return style;
    }

    public String getWeight() {
        return weight;
    }
}
