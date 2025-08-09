package com.w3canvas.css;

import java.awt.Color;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Value {
    private String text;
    private double numValue;
    private String unit;

    public Value(String text) {
        this.text = text;
        // Basic parsing for font size
        Pattern pattern = Pattern.compile("([0-9.]+)(px|in|cm|pt|pc|mm)?");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            try {
                this.numValue = Double.parseDouble(matcher.group(1));
                this.unit = matcher.group(2);
            } catch (NumberFormatException e) {
                // Ignore if not a number
            }
        }
    }

    public Color getColor() {
        return CSSParser.parseColor(text);
    }

    public double getNumValue() {
        return numValue;
    }

    public String getUnit() {
        return unit;
    }

    public String getValue() {
        return text;
    }

    public int getRGB() {
        return getColor().getRGB();
    }
}
