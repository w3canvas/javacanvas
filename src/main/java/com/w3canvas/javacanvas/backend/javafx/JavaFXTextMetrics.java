package com.w3canvas.javacanvas.backend.javafx;

import com.w3canvas.javacanvas.interfaces.ITextMetrics;
import javafx.geometry.Bounds;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class JavaFXTextMetrics implements ITextMetrics {
    private final double width;
    private final double actualBoundingBoxLeft;
    private final double actualBoundingBoxRight;
    private final double actualBoundingBoxAscent;
    private final double actualBoundingBoxDescent;

    public JavaFXTextMetrics(Font font, String text) {
        Text textNode = new Text(text);
        textNode.setFont(font);
        Bounds bounds = textNode.getLayoutBounds();

        this.width = bounds.getWidth();
        this.actualBoundingBoxLeft = -bounds.getMinX();
        this.actualBoundingBoxRight = bounds.getMaxX();
        this.actualBoundingBoxAscent = -bounds.getMinY();
        this.actualBoundingBoxDescent = bounds.getMaxY();
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
        // TODO: Implement this properly
        return 0;
    }

    @Override
    public double getFontBoundingBoxDescent() {
        // TODO: Implement this properly
        return 0;
    }

    @Override
    public double getEmHeightAscent() {
        // TODO: Implement this properly
        return 0;
    }

    @Override
    public double getEmHeightDescent() {
        // TODO: Implement this properly
        return 0;
    }

    @Override
    public double getHangingBaseline() {
        // TODO: Implement this properly
        return 0;
    }

    @Override
    public double getAlphabeticBaseline() {
        // TODO: Implement this properly
        return 0;
    }

    @Override
    public double getIdeographicBaseline() {
        // TODO: Implement this properly
        return 0;
    }
}
