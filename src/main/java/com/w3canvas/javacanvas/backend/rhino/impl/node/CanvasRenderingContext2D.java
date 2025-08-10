package com.w3canvas.javacanvas.backend.rhino.impl.node;

import java.awt.geom.AffineTransform;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.Scriptable;

import com.w3canvas.javacanvas.backend.rhino.impl.gradient.CanvasGradient;
import com.w3canvas.javacanvas.interfaces.ICanvasGradient;
import com.w3canvas.javacanvas.interfaces.ICanvasPattern;
import com.w3canvas.javacanvas.interfaces.ICanvasRenderingContext2D;
import com.w3canvas.javacanvas.interfaces.IImageData;
import com.w3canvas.javacanvas.interfaces.ITextMetrics;
import com.w3canvas.javacanvas.js.CanvasText;
import com.w3canvas.javacanvas.js.ICanvas;
import com.w3canvas.javacanvas.utils.RhinoCanvasUtils;

@SuppressWarnings("serial")
public class CanvasRenderingContext2D extends ProjectScriptableObject implements CanvasText {

    private ICanvasRenderingContext2D core;
    private ICanvas canvas;

    CanvasRenderingContext2D(ICanvasRenderingContext2D core) {
        this.core = core;
    }

    // Default constructor for Rhino
    public CanvasRenderingContext2D() {
    }

    public void initCanvas(ICanvas canvas) {
        this.canvas = canvas;
    }

    public void reset() {
	core.reset();
    }

    public void jsFunction_save() {
        core.save();
    }

    public void jsFunction_restore() {
        core.restore();
    }

    public void jsFunction_scale(Double x, Double y) {
        core.scale(x, y);
    }

    public void jsFunction_rotate(Double angle) {
        core.rotate(angle);
    }

    public void jsFunction_translate(Double x, Double y) {
        core.translate(x, y);
    }

    public void jsFunction_transform(Double m11, Double m12, Double m21, Double m22, Double dx, Double dy) {
        core.transform(m11, m12, m21, m22, dx, dy);
    }

    public void jsFunction_setTransform(Double m11, Double m12, Double m21, Double m22, Double dx, Double dy) {
        core.setTransform(m11, m12, m21, m22, dx, dy);
    }

    public void jsFunction_resetTransform() {
        core.resetTransform();
    }

    public DOMMatrix jsFunction_getTransform() {
        return new DOMMatrix((AffineTransform) core.getTransform());
    }

    public Double jsGet_globalAlpha() {
        return core.getGlobalAlpha();
    }

    public void jsSet_globalAlpha(Double globalAlpha) {
        core.setGlobalAlpha(globalAlpha);
    }

    public String jsGet_globalCompositeOperation() {
        return core.getGlobalCompositeOperation();
    }

    public void jsSet_globalCompositeOperation(String op) {
        core.setGlobalCompositeOperation(op);
    }

    public Object jsGet_fillStyle() {
        return core.getFillStyle();
    }

    public void jsSet_fillStyle(Object fillStyle) {
        core.setFillStyle(fillStyle);
    }

    public Object jsGet_strokeStyle() {
        return core.getStrokeStyle();
    }

    public void jsSet_strokeStyle(Object strokeStyle) {
        core.setStrokeStyle(strokeStyle);
    }

    public CanvasGradient jsFunction_createLinearGradient(Double x0, Double y0, Double x1, Double y1) {
        ICanvasGradient gradient = core.createLinearGradient(x0, y0, x1, y1);
        return (CanvasGradient) gradient;
    }

    public CanvasGradient jsFunction_createRadialGradient(Double x0, Double y0, Double r0, Double x1, Double y1, Double r1) {
        ICanvasGradient gradient = core.createRadialGradient(x0, y0, r0, x1, y1, r1);
        return (CanvasGradient) gradient;
    }

    public CanvasPattern jsFunction_createPattern(Image image, String repetition) {
        ICanvasPattern pattern = core.createPattern(image.getImage(), repetition);
        return (CanvasPattern) pattern;
    }

    public void jsSet_lineDashOffset(Double offset) {
        core.setLineDashOffset(offset);
    }

    public double jsGet_lineDashOffset() {
        return core.getLineDashOffset();
    }

    public void jsFunction_setLineDash(NativeArray dash) {
        double[] lineDash = new double[(int) dash.getLength()];
        for (int i = 0; i < lineDash.length; i++) {
            lineDash[i] = ((Number) dash.get(i, dash)).doubleValue();
        }
        core.setLineDash(lineDash);
    }

    public Scriptable jsFunction_getLineDash() {
        Object lineDashObj = core.getLineDash();
        if (lineDashObj instanceof double[]) {
            double[] lineDash = (double[]) lineDashObj;
            Object[] arr = new Object[lineDash.length];
            for (int i = 0; i < lineDash.length; i++) {
                arr[i] = lineDash[i];
            }
            return Context.getCurrentContext().newArray(getParentScope(), arr);
        }
        return Context.getCurrentContext().newArray(getParentScope(), 0);
    }

    public void jsSet_lineWidth(Double lw) {
        core.setLineWidth(lw);
    }

    public Double jsGet_lineWidth() {
        return core.getLineWidth();
    }

    public void jsSet_lineCap(String cap) {
        core.setLineCap(cap);
    }

    public String jsGet_lineCap() {
        return core.getLineCap();
    }

    public void jsSet_lineJoin(String join) {
        core.setLineJoin(join);
    }

    public String jsGet_lineJoin() {
        return core.getLineJoin();
    }

    public void jsSet_miterLimit(Double miterLimit) {
        core.setMiterLimit(miterLimit);
    }

    public Double jsGet_miterLimit() {
        return core.getMiterLimit();
    }

    public void jsFunction_clearRect(Double x, Double y, Double w, Double h) {
        core.clearRect(x, y, w, h);
        canvas.dirty();
    }

    public void jsFunction_fillRect(Double x, Double y, Double w, Double h) {
        core.fillRect(x, y, w, h);
        canvas.dirty();
    }

    public void jsFunction_strokeRect(Double x, Double y, Double w, Double h) {
        core.strokeRect(x, y, w, h);
        canvas.dirty();
    }

    public void jsFunction_beginPath() {
        core.beginPath();
    }

    public void jsFunction_closePath() {
        core.closePath();
    }

    public void jsFunction_moveTo(Double x, Double y) {
        core.moveTo(x, y);
    }

    public void jsFunction_lineTo(Double x, Double y) {
        core.lineTo(x, y);
    }

    public void jsFunction_quadraticCurveTo(Double cpx, Double cpy, Double x, Double y) {
        core.quadraticCurveTo(cpx, cpy, x, y);
    }

    public void jsFunction_bezierCurveTo(Double cp1x, Double cp1y, Double cp2x, Double cp2y, Double x, Double y) {
        core.bezierCurveTo(cp1x, cp1y, cp2x, cp2y, x, y);
    }

    public void jsFunction_arcTo(Double x1, Double y1, Double x2, Double y2, Double radius) {
        core.arcTo(x1, y1, x2, y2, radius);
    }

    public void jsFunction_rect(Double x, Double y, Double w, Double h) {
        core.rect(x, y, w, h);
    }

    public void jsFunction_arc(Double x, Double y, Double radius, Double startAngle, Double endAngle, boolean counterclockwise) {
        core.arc(x, y, radius, startAngle, endAngle, counterclockwise);
    }

    public void jsFunction_ellipse(double x, double y, double radiusX, double radiusY, double rotation, double startAngle, double endAngle, boolean counterclockwise) {
        core.ellipse(x, y, radiusX, radiusY, rotation, startAngle, endAngle, counterclockwise);
    }

    public void jsFunction_fill() {
        core.fill();
        canvas.dirty();
    }

    public void jsFunction_stroke() {
        core.stroke();
        canvas.dirty();
    }

    public void jsFunction_clip() {
        core.clip();
    }

    public boolean jsFunction_isPointInPath(Double x, Double y) {
        return core.isPointInPath(x, y);
    }

    public boolean jsFunction_isPointInStroke(Double x, Double y) {
        return core.isPointInStroke(x, y);
    }

    public void jsFunction_drawImage(Image image, int sx, int sy, int sw, int sh, int dx, int dy, int dw, int dh) {
        core.drawImage(image.getImage(), sx, sy, sw, sh, dx, dy, dw, dh);
        canvas.dirty();
    }

    @Override
    public TextMetrics jsFunction_measureText(String text) {
        ITextMetrics metrics = core.measureText(text);
        if (metrics != null) {
            return RhinoCanvasUtils.getScriptableInstance(TextMetrics.class, new Double[] { metrics.getWidth() });
        }
        return RhinoCanvasUtils.getScriptableInstance(TextMetrics.class, new Double[] { 0.0 });
    }

    @Override
    public String jsGet_font() {
        return core.getFont();
    }

    @Override
    public void jsSet_font(String font) {
        core.setFont(font);
    }

    @Override
    public void jsFunction_fillText(String text, Double x, Double y, int maxWidth) {
        core.fillText(text, x, y, maxWidth);
        canvas.dirty();
    }

    @Override
    public void jsFunction_strokeText(String text, Double x, Double y, int maxWidth) {
        core.strokeText(text, x, y, maxWidth);
        canvas.dirty();
    }

    @Override
    public String jsGet_textAlign() {
        return core.getTextAlign();
    }

    @Override
    public String jsGet_textBaseline() {
        return core.getTextBaseline();
    }

    @Override
    public void jsSet_textAlign(String textAlign) {
        core.setTextAlign(textAlign);
    }

    @Override
    public void jsSet_textBaseline(String textBaseline) {
        core.setTextBaseline(textBaseline);
    }

    public ImageData jsFunction_createImageData(int width, int height) {
        IImageData data = core.createImageData(width, height);
        return (ImageData) data;
    }

    public ImageData jsFunction_getImageData(int x, int y, int width, int height) {
        IImageData data = core.getImageData(x, y, width, height);
        return (ImageData) data;
    }

    public void jsFunction_putImageData(ImageData imagedata, int dx, int dy, int dirtyX, int dirtyY, int dirtyWidth, int dirtyHeight) {
    }

    public void jsFunction_reset() {
        core.reset();
    }

    public boolean jsFunction_isContextLost() {
        return core.isContextLost();
    }

    public Scriptable jsFunction_getContextAttributes() {
        return core.getContextAttributes();
    }

    public String jsGet_filter() {
        return core.getFilter();
    }

    public void jsSet_filter(String filter) {
        core.setFilter(filter);
    }
}
