package com.w3canvas.css;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CSSParser {

    private static final Map<String, Color> COLOR_MAP = new HashMap<>();

    static {
        COLOR_MAP.put("white", Color.WHITE);
        COLOR_MAP.put("black", Color.BLACK);
        COLOR_MAP.put("red", Color.RED);
        COLOR_MAP.put("green", Color.GREEN);
        COLOR_MAP.put("blue", Color.BLUE);
        COLOR_MAP.put("yellow", Color.YELLOW);
        COLOR_MAP.put("cyan", Color.CYAN);
        COLOR_MAP.put("magenta", Color.MAGENTA);
        COLOR_MAP.put("gray", Color.GRAY);
        COLOR_MAP.put("lightgray", Color.LIGHT_GRAY);
        COLOR_MAP.put("darkgray", Color.DARK_GRAY);
        COLOR_MAP.put("orange", Color.ORANGE);
        COLOR_MAP.put("pink", Color.PINK);
    }

    public static Color parseColor(String text) {
        if (text == null) {
            return Color.BLACK;
        }
        String lowerText = text.toLowerCase();
        if (COLOR_MAP.containsKey(lowerText)) {
            return COLOR_MAP.get(lowerText);
        }
        if (lowerText.startsWith("#")) {
            try {
                return Color.decode(text);
            } catch (NumberFormatException e) {
                return Color.BLACK;
            }
        }
        if (lowerText.startsWith("rgb")) {
            String[] parts = lowerText.replaceAll("[^0-9,]", "").split(",");
            if (parts.length >= 3) {
                try {
                    int r = Integer.parseInt(parts[0]);
                    int g = Integer.parseInt(parts[1]);
                    int b = Integer.parseInt(parts[2]);
                    if (parts.length == 4) {
                        int a = Integer.parseInt(parts[3]);
                        return new Color(r, g, b, a);
                    }
                    return new Color(r, g, b);
                } catch (Exception e) {
                    return Color.BLACK;
                }
            }
        }
        return Color.BLACK;
    }
}
