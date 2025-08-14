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

    public double getActualBoundingBoxLeft() {
        return actualBoundingBoxLeft;
    }

    public double getActualBoundingBoxRight() {
        return actualBoundingBoxRight;
    }

    public double getActualBoundingBoxAscent() {
        return actualBoundingBoxAscent;
    }

    public double getActualBoundingBoxDescent() {
        return actualBoundingBoxDescent;
    }
}
