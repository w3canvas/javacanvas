package com.w3canvas.css;

import com.w3canvas.javacanvas.backend.rhino.impl.node.Document;
import com.w3canvas.javacanvas.dom.FontFace;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CSSParser {

    private static final Pattern FONT_FACE_RULE = Pattern.compile("@font-face\\s*\\{([^}]+)\\}");
    private static final Pattern FONT_PROPERTY = Pattern.compile("font-family:\\s*['\"]?([^;'\"\\n]+)['\"]?;|src:\\s*url\\((['\"]?([^)]+?)['\"]?)\\);|font-style:\\s*(\\w+);|font-weight:\\s*(\\w+);");
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
    public static java.util.Map<String, Object> parseFont(String text) {
        java.util.Map<String, Object> font = new java.util.HashMap<>();
        font.put("style", "normal");
        font.put("variant", "normal");
        font.put("weight", "normal");
        font.put("size", 10.0f);
        font.put("family", "sans-serif");

        if (text == null) {
            return font;
        }

        String[] parts = text.split("\\s+");
        if (parts.length == 0) {
            return font;
        }

        int sizeIndex = -1;
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (part.matches("\\d+(\\.\\d+)?px")) {
                sizeIndex = i;
                break;
            }
        }

        if (sizeIndex != -1) {
            String sizeStr = parts[sizeIndex].replace("px", "");
            font.put("size", Float.parseFloat(sizeStr));

            StringBuilder familyBuilder = new StringBuilder();
            for (int i = sizeIndex + 1; i < parts.length; i++) {
                familyBuilder.append(parts[i]).append(" ");
            }
            if (familyBuilder.length() > 0) {
                font.put("family", familyBuilder.toString().trim());
            }

            for (int i = 0; i < sizeIndex; i++) {
                String part = parts[i].toLowerCase();
                switch (part) {
                    case "italic":
                    case "oblique":
                        font.put("style", part);
                        break;
                    case "small-caps":
                        font.put("variant", part);
                        break;
                    case "bold":
                    case "bolder":
                    case "lighter":
                    case "100":
                    case "200":
                    case "300":
                    case "400":
                    case "500":
                    case "600":
                    case "700":
                    case "800":
                    case "900":
                        font.put("weight", part);
                        break;
                }
            }
        }
        return font;
    }

    public static void parseStyleSheet(String sheet, Document document) {
        Matcher ruleMatcher = FONT_FACE_RULE.matcher(sheet);
        while (ruleMatcher.find()) {
            String rule = ruleMatcher.group(1);
            Matcher propMatcher = FONT_PROPERTY.matcher(rule);
            String family = null, src = null, style = "normal", weight = "normal";
            while (propMatcher.find()) {
                if (propMatcher.group(1) != null) {
                    family = propMatcher.group(1);
                } else if (propMatcher.group(3) != null) {
                    src = propMatcher.group(3);
                } else if (propMatcher.group(4) != null) {
                    style = propMatcher.group(4);
                } else if (propMatcher.group(5) != null) {
                    weight = propMatcher.group(5);
                }
            }
            if (family != null && src != null) {
                com.w3canvas.javacanvas.backend.rhino.impl.font.RhinoFontFace rf = new com.w3canvas.javacanvas.backend.rhino.impl.font.RhinoFontFace();
                rf.jsConstructor(family, src, null);
                document.jsGet_fonts().jsFunction_add(rf);
            }
        }
    }
}
