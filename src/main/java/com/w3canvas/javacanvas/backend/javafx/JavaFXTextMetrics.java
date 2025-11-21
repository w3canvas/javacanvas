package com.w3canvas.javacanvas.backend.javafx;

import com.w3canvas.javacanvas.interfaces.ITextMetrics;
import com.sun.javafx.tk.Toolkit;
import javafx.geometry.Bounds;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class JavaFXTextMetrics implements ITextMetrics {
    private final double width;
    private final double actualBoundingBoxLeft;
    private final double actualBoundingBoxRight;
    private final double actualBoundingBoxAscent;
    private final double actualBoundingBoxDescent;
    private final double fontBoundingBoxAscent;
    private final double fontBoundingBoxDescent;
    private final double emHeightAscent;
    private final double emHeightDescent;
    private final double hangingBaseline;
    private final double alphabeticBaseline;
    private final double ideographicBaseline;

    public JavaFXTextMetrics(Font font, String text) {
        this(font, text, 0, 0);
    }

    public JavaFXTextMetrics(Font font, String text, double wordSpacing, double letterSpacing) {
        // Get font-level metrics using JavaFX FontLoader
        com.sun.javafx.tk.FontMetrics fm = Toolkit.getToolkit().getFontLoader().getFontMetrics(font);

        if (wordSpacing == 0 && letterSpacing == 0) {
            Text textNode = new Text(text);
            textNode.setFont(font);
            Bounds bounds = textNode.getLayoutBounds();

            // Width and actual bounding box for the specific text
            this.width = bounds.getWidth();
            this.actualBoundingBoxLeft = -bounds.getMinX();
            this.actualBoundingBoxRight = bounds.getMaxX();
            this.actualBoundingBoxAscent = -bounds.getMinY();
            this.actualBoundingBoxDescent = bounds.getMaxY();
        } else {
            // Calculate width with spacing
            double totalWidth = 0;

            if (letterSpacing != 0) {
                // Letter spacing applies to all chars
                for (char c : text.toCharArray()) {
                    totalWidth += fm.getCharWidth(c) + letterSpacing;
                    if (c == ' ') {
                        totalWidth += wordSpacing;
                    }
                }
            } else {
                // Word spacing only
                String[] words = text.split(" ", -1);
                double spaceWidth = fm.getCharWidth(' ');
                for (int i = 0; i < words.length; i++) {
                    String word = words[i];
                    Text wordNode = new Text(word);
                    wordNode.setFont(font);
                    totalWidth += wordNode.getLayoutBounds().getWidth();

                    if (i < words.length - 1) {
                        totalWidth += spaceWidth + wordSpacing;
                    }
                }
            }
            this.width = totalWidth;

            // Approximate bounds based on font metrics since precise bounds with spacing is complex
            this.actualBoundingBoxLeft = 0;
            this.actualBoundingBoxRight = totalWidth;
            this.actualBoundingBoxAscent = fm.getAscent();
            this.actualBoundingBoxDescent = fm.getDescent();
        }

        // Font-level metrics (maximum for the entire font)
        this.fontBoundingBoxAscent = fm.getAscent();
        this.fontBoundingBoxDescent = fm.getDescent();

        // Em height metrics
        // In JavaFX, we use the font metrics ascent and descent for em height
        this.emHeightAscent = fm.getAscent();
        this.emHeightDescent = fm.getDescent();

        // Baseline offsets
        // Alphabetic baseline is the reference point (0)
        this.alphabeticBaseline = 0;

        // Hanging baseline is typically positioned at ~80% of the ascent
        // Used for Devanagari and similar scripts
        this.hangingBaseline = fm.getAscent() * 0.8;

        // Ideographic baseline is at the bottom of the em-box
        // Used for CJK ideographic characters
        this.ideographicBaseline = -fm.getDescent();
    }

    @Override
    public double getWidth() {
        return width;
    }

    @Override
    public double getActualBoundingBoxLeft() {
        return actualBoundingBoxLeft;
    }

    @Override
    public double getActualBoundingBoxRight() {
        return actualBoundingBoxRight;
    }

    @Override
    public double getActualBoundingBoxAscent() {
        return actualBoundingBoxAscent;
    }

    @Override
    public double getActualBoundingBoxDescent() {
        return actualBoundingBoxDescent;
    }

    @Override
    public double getFontBoundingBoxAscent() {
        return fontBoundingBoxAscent;
    }

    @Override
    public double getFontBoundingBoxDescent() {
        return fontBoundingBoxDescent;
    }

    @Override
    public double getEmHeightAscent() {
        return emHeightAscent;
    }

    @Override
    public double getEmHeightDescent() {
        return emHeightDescent;
    }

    @Override
    public double getHangingBaseline() {
        return hangingBaseline;
    }

    @Override
    public double getAlphabeticBaseline() {
        return alphabeticBaseline;
    }

    @Override
    public double getIdeographicBaseline() {
        return ideographicBaseline;
    }
}
