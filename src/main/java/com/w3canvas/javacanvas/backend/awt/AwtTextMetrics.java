package com.w3canvas.javacanvas.backend.awt;

import com.w3canvas.javacanvas.interfaces.ITextMetrics;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.font.LineMetrics;
import java.awt.geom.Rectangle2D;

public class AwtTextMetrics implements ITextMetrics {
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

    public AwtTextMetrics(String text, Font font, Graphics2D g2d) {
        this(text, font, g2d, 0);
    }

    public AwtTextMetrics(String text, Font font, Graphics2D g2d, double wordSpacing) {
        FontMetrics fm = g2d.getFontMetrics(font);
        LineMetrics lm = font.getLineMetrics(text, g2d.getFontRenderContext());

        // Width and Bounds calculation
        if (wordSpacing == 0) {
            this.width = fm.stringWidth(text);

            // Get the actual bounding box for the specific text
            Rectangle2D bounds = fm.getStringBounds(text, g2d);
            this.actualBoundingBoxLeft = Math.abs(bounds.getX());
            this.actualBoundingBoxRight = bounds.getX() + bounds.getWidth();
            this.actualBoundingBoxAscent = Math.abs(bounds.getY());
            this.actualBoundingBoxDescent = bounds.getY() + bounds.getHeight();
        } else {
            // Calculate width with word spacing
            String[] words = text.split(" ", -1);
            double totalWidth = 0;
            double spaceWidth = fm.getStringBounds(" ", g2d).getWidth();
            Rectangle2D unionBounds = null;
            double currentX = 0;

            for (int i = 0; i < words.length; i++) {
                String word = words[i];
                if (!word.isEmpty()) {
                    Rectangle2D wordBounds = fm.getStringBounds(word, g2d);
                    // Adjust bounds to current position
                    Rectangle2D placedBounds = new Rectangle2D.Double(
                        wordBounds.getX() + currentX,
                        wordBounds.getY(),
                        wordBounds.getWidth(),
                        wordBounds.getHeight()
                    );

                    if (unionBounds == null) {
                        unionBounds = placedBounds;
                    } else {
                        unionBounds.add(placedBounds);
                    }

                    totalWidth += wordBounds.getWidth();
                    currentX += wordBounds.getWidth();
                }

                if (i < words.length - 1) {
                    totalWidth += spaceWidth + wordSpacing;
                    currentX += spaceWidth + wordSpacing;
                }
            }

            this.width = totalWidth;

            if (unionBounds != null) {
                this.actualBoundingBoxLeft = Math.abs(unionBounds.getX());
                this.actualBoundingBoxRight = unionBounds.getX() + unionBounds.getWidth();
                this.actualBoundingBoxAscent = Math.abs(unionBounds.getY());
                this.actualBoundingBoxDescent = unionBounds.getY() + unionBounds.getHeight();
            } else {
                // Empty or just spaces
                this.actualBoundingBoxLeft = 0;
                this.actualBoundingBoxRight = 0;
                this.actualBoundingBoxAscent = 0;
                this.actualBoundingBoxDescent = 0;
            }
        }

        // Font-level metrics (maximum for the entire font)
        this.fontBoundingBoxAscent = fm.getAscent();
        this.fontBoundingBoxDescent = fm.getDescent();

        // Em height metrics (using line metrics for more accurate em-box)
        // Em height is typically the ascent + descent from LineMetrics
        this.emHeightAscent = lm.getAscent();
        this.emHeightDescent = lm.getDescent();

        // Baseline offsets
        // Alphabetic baseline is the reference point (0)
        this.alphabeticBaseline = 0;

        // Hanging baseline is typically positioned at ~80% of the ascent
        // Used for Devanagari and similar scripts
        this.hangingBaseline = lm.getAscent() * 0.8;

        // Ideographic baseline is at the bottom of the em-box
        // Used for CJK ideographic characters
        this.ideographicBaseline = -lm.getDescent();
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
