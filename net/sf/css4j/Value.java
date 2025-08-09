package net.sf.css4j;

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
        // This is a stub. It doesn't actually parse all color formats.
        if (text == null) return Color.BLACK;
        String lowerText = text.toLowerCase();
        if (lowerText.equals("white")) return Color.WHITE;
        if (lowerText.equals("black")) return Color.BLACK;
        if (lowerText.equals("red")) return Color.RED;
        if (lowerText.equals("green")) return Color.GREEN;
        if (lowerText.equals("blue")) return Color.BLUE;
        if (lowerText.equals("yellow")) return Color.YELLOW;
        if (lowerText.startsWith("#")) {
            try {
                return Color.decode(text);
            } catch (NumberFormatException e) {
                return Color.BLACK;
            }
        }
        // Very basic rgb/rgba parsing
        if(lowerText.startsWith("rgb")) {
            String[] parts = lowerText.replaceAll("[^0-9,]", "").split(",");
            if(parts.length >= 3) {
                try {
                    int r = Integer.parseInt(parts[0]);
                    int g = Integer.parseInt(parts[1]);
                    int b = Integer.parseInt(parts[2]);
                    if (parts.length == 4) {
                        int a = Integer.parseInt(parts[3]);
                        return new Color(r,g,b,a);
                    }
                    return new Color(r,g,b);
                } catch (Exception e) {
                    return Color.BLACK;
                }
            }
        }
        return Color.BLACK;
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
