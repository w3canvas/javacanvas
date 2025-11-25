package com.w3canvas.javacanvas.backend.graal.impl.node;

import org.graalvm.polyglot.HostAccess;

import com.w3canvas.javacanvas.core.CoreCanvasRenderingContext2D;

/**
 * GraalJS adapter for CanvasRenderingContext2D.
 * Wraps CoreCanvasRenderingContext2D and exposes Canvas 2D API to GraalJS.
 *
 * This is a thin wrapper - all logic is in CoreCanvasRenderingContext2D.
 */
public class GraalCanvasRenderingContext2D {
    private final CoreCanvasRenderingContext2D core;

    public GraalCanvasRenderingContext2D(CoreCanvasRenderingContext2D core) {
        this.core = core;
    }

    // State Management

    @HostAccess.Export
    public void save() {
        core.save();
    }

    @HostAccess.Export
    public void restore() {
        core.restore();
    }

    // Fill and Stroke Styles

    @HostAccess.Export
    public Object getFillStyle() {
        return core.getFillStyle();
    }

    @HostAccess.Export
    public void setFillStyle(Object style) {
        core.setFillStyle(style);
    }

    @HostAccess.Export
    public Object getStrokeStyle() {
        return core.getStrokeStyle();
    }

    @HostAccess.Export
    public void setStrokeStyle(Object style) {
        core.setStrokeStyle(style);
    }

    // Rectangles

    @HostAccess.Export
    public void clearRect(double x, double y, double width, double height) {
        core.clearRect(x, y, width, height);
    }

    @HostAccess.Export
    public void fillRect(double x, double y, double width, double height) {
        core.fillRect(x, y, width, height);
    }

    @HostAccess.Export
    public void strokeRect(double x, double y, double width, double height) {
        core.strokeRect(x, y, width, height);
    }

    // Paths

    @HostAccess.Export
    public void beginPath() {
        core.beginPath();
    }

    @HostAccess.Export
    public void closePath() {
        core.closePath();
    }

    @HostAccess.Export
    public void moveTo(double x, double y) {
        core.moveTo(x, y);
    }

    @HostAccess.Export
    public void lineTo(double x, double y) {
        core.lineTo(x, y);
    }

    @HostAccess.Export
    public void arc(double x, double y, double radius, double startAngle, double endAngle, boolean anticlockwise) {
        core.arc(x, y, radius, startAngle, endAngle, anticlockwise);
    }

    @HostAccess.Export
    public void arc(double x, double y, double radius, double startAngle, double endAngle) {
        core.arc(x, y, radius, startAngle, endAngle, false);
    }

    @HostAccess.Export
    public void fill() {
        core.fill();
    }

    @HostAccess.Export
    public void stroke() {
        core.stroke();
    }

    // Line Styles

    @HostAccess.Export
    public double getLineWidth() {
        return core.getLineWidth();
    }

    @HostAccess.Export
    public void setLineWidth(double width) {
        core.setLineWidth(width);
    }

    @HostAccess.Export
    public String getLineCap() {
        return core.getLineCap();
    }

    @HostAccess.Export
    public void setLineCap(String cap) {
        core.setLineCap(cap);
    }

    @HostAccess.Export
    public String getLineJoin() {
        return core.getLineJoin();
    }

    @HostAccess.Export
    public void setLineJoin(String join) {
        core.setLineJoin(join);
    }

    // Transformations

    @HostAccess.Export
    public void translate(double x, double y) {
        core.translate(x, y);
    }

    @HostAccess.Export
    public void rotate(double angle) {
        core.rotate(angle);
    }

    @HostAccess.Export
    public void scale(double x, double y) {
        core.scale(x, y);
    }

    // Text

    @HostAccess.Export
    public void fillText(String text, double x, double y) {
        core.fillText(text, x, y, Double.MAX_VALUE);
    }

    @HostAccess.Export
    public void fillText(String text, double x, double y, double maxWidth) {
        core.fillText(text, x, y, maxWidth);
    }

    @HostAccess.Export
    public void strokeText(String text, double x, double y) {
        core.strokeText(text, x, y, Double.MAX_VALUE);
    }

    @HostAccess.Export
    public void strokeText(String text, double x, double y, double maxWidth) {
        core.strokeText(text, x, y, maxWidth);
    }

    @HostAccess.Export
    public String getFont() {
        return core.getFont();
    }

    @HostAccess.Export
    public void setFont(String font) {
        core.setFont(font);
    }

    @HostAccess.Export
    public String getTextAlign() {
        return core.getTextAlign();
    }

    @HostAccess.Export
    public void setTextAlign(String align) {
        core.setTextAlign(align);
    }

    @HostAccess.Export
    public String getTextBaseline() {
        return core.getTextBaseline();
    }

    @HostAccess.Export
    public void setTextBaseline(String baseline) {
        core.setTextBaseline(baseline);
    }

    // Images

    @HostAccess.Export
    public void drawImage(Object image, double dx, double dy) {
        core.drawImage(image, dx, dy);
    }

    @HostAccess.Export
    public void drawImage(Object image, double dx, double dy, double dw, double dh) {
        core.drawImage(image, dx, dy, dw, dh);
    }

    // Compositing

    @HostAccess.Export
    public double getGlobalAlpha() {
        return core.getGlobalAlpha();
    }

    @HostAccess.Export
    public void setGlobalAlpha(double alpha) {
        core.setGlobalAlpha(alpha);
    }

    @HostAccess.Export
    public String getGlobalCompositeOperation() {
        return core.getGlobalCompositeOperation();
    }

    @HostAccess.Export
    public void setGlobalCompositeOperation(String operation) {
        core.setGlobalCompositeOperation(operation);
    }

    /**
     * Get the underlying core context.
     */
    public CoreCanvasRenderingContext2D getCore() {
        return core;
    }

    /**
     * Get the canvas surface for pixel access.
     */
    @HostAccess.Export
    public Object getSurface() {
        return core.getSurface();
    }
}
