package com.w3canvas.javacanvas.backend.rhino.impl.node;

import java.awt.geom.AffineTransform;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.Scriptable;

import com.w3canvas.javacanvas.backend.rhino.impl.gradient.CanvasGradient;
import com.w3canvas.javacanvas.backend.rhino.impl.gradient.RhinoCanvasGradient;
import com.w3canvas.javacanvas.backend.rhino.impl.node.RhinoCanvasPattern;
import com.w3canvas.javacanvas.interfaces.ICanvasGradient;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import com.w3canvas.javacanvas.interfaces.ICanvasPattern;
import com.w3canvas.javacanvas.interfaces.ICanvasRenderingContext2D;
import com.w3canvas.javacanvas.interfaces.ICanvasSurface;
import com.w3canvas.javacanvas.interfaces.IImageData;
import com.w3canvas.javacanvas.interfaces.ITextMetrics;
import com.w3canvas.javacanvas.js.CanvasText;
import com.w3canvas.javacanvas.js.ICanvas;
import com.w3canvas.javacanvas.utils.RhinoCanvasUtils;

@SuppressWarnings("serial")
public class CanvasRenderingContext2D extends ProjectScriptableObject implements CanvasText, ICanvasRenderingContext2D {

    private ICanvasRenderingContext2D core;
    private ICanvas canvas;

    // Default constructor for Rhino
    public CanvasRenderingContext2D() {
    }

    public void init(ICanvasRenderingContext2D core) {
        this.core = core;
    }

    public void initCanvas(ICanvas canvas) {
        this.canvas = canvas;
    }

    @Override
    public ICanvasSurface getSurface() {
        return core.getSurface();
    }

    @Override
    public void save() {
        core.save();
    }

    @Override
    public void restore() {
        core.restore();
    }

    @Override
    public void scale(double x, double y) {
        core.scale(x, y);
    }

    @Override
    public void rotate(double angle) {
        core.rotate(angle);
    }

    @Override
    public void translate(double x, double y) {
        core.translate(x, y);
    }

    @Override
    public void transform(double m11, double m12, double m21, double m22, double dx, double dy) {
        core.transform(m11, m12, m21, m22, dx, dy);
    }

    @Override
    public void setTransform(double m11, double m12, double m21, double m22, double dx, double dy) {
        core.setTransform(m11, m12, m21, m22, dx, dy);
    }

    @Override
    public void resetTransform() {
        core.resetTransform();
    }

    @Override
    public Object getTransform() {
        return core.getTransform();
    }

    @Override
    public double getGlobalAlpha() {
        return core.getGlobalAlpha();
    }

    @Override
    public void setGlobalAlpha(double globalAlpha) {
        core.setGlobalAlpha(globalAlpha);
    }

    @Override
    public String getGlobalCompositeOperation() {
        return core.getGlobalCompositeOperation();
    }

    @Override
    public void setGlobalCompositeOperation(String op) {
        core.setGlobalCompositeOperation(op);
    }

    @Override
    public Object getFillStyle() {
        return core.getFillStyle();
    }

    @Override
    public void setFillStyle(Object fillStyle) {
        core.setFillStyle(fillStyle);
    }

    @Override
    public Object getStrokeStyle() {
        return core.getStrokeStyle();
    }

    @Override
    public void setStrokeStyle(Object strokeStyle) {
        core.setStrokeStyle(strokeStyle);
    }

    @Override
    public ICanvasGradient createLinearGradient(double x0, double y0, double x1, double y1) {
        return core.createLinearGradient(x0, y0, x1, y1);
    }

    @Override
    public ICanvasGradient createRadialGradient(double x0, double y0, double r0, double x1, double y1, double r1) {
        return core.createRadialGradient(x0, y0, r0, x1, y1, r1);
    }

    @Override
    public ICanvasPattern createPattern(Object image, String repetition) {
        if (image instanceof HTMLCanvasElement) {
            HTMLCanvasElement canvas = (HTMLCanvasElement) image;
            ICanvasRenderingContext2D context = (ICanvasRenderingContext2D) canvas.jsFunction_getContext("2d");
            int width = canvas.getWidth();
            int height = canvas.getHeight();
            int[] pixels = context.getSurface().getPixelData(0, 0, width, height);
            WritableImage patternImage = new WritableImage(width, height);
            patternImage.getPixelWriter().setPixels(0, 0, width, height, PixelFormat.getIntArgbInstance(), pixels, 0, width);
            return core.createPattern(patternImage, repetition);
        } else if (image instanceof Image) {
            return core.createPattern(((Image) image).getImage(), repetition);
        }
        return null;
    }

    @Override
    public double getLineWidth() {
        return core.getLineWidth();
    }

    @Override
    public void setLineWidth(double lw) {
        core.setLineWidth(lw);
    }

    @Override
    public String getLineCap() {
        return core.getLineCap();
    }

    @Override
    public void setLineCap(String cap) {
        core.setLineCap(cap);
    }

    @Override
    public String getLineJoin() {
        return core.getLineJoin();
    }

    @Override
    public void setLineJoin(String join) {
        core.setLineJoin(join);
    }

    @Override
    public double getMiterLimit() {
        return core.getMiterLimit();
    }

    @Override
    public void setMiterLimit(double miterLimit) {
        core.setMiterLimit(miterLimit);
    }

    @Override
    public void setLineDash(Object dash) {
        core.setLineDash(dash);
    }

    @Override
    public Object getLineDash() {
        return core.getLineDash();
    }

    @Override
    public double getLineDashOffset() {
        return core.getLineDashOffset();
    }

    @Override
    public void setLineDashOffset(double offset) {
        core.setLineDashOffset(offset);
    }

    @Override
    public void clearRect(double x, double y, double w, double h) {
        core.clearRect(x, y, w, h);
        canvas.dirty();
    }

    @Override
    public void fillRect(double x, double y, double w, double h) {
        core.fillRect(x, y, w, h);
        canvas.dirty();
    }

    @Override
    public void strokeRect(double x, double y, double w, double h) {
        core.strokeRect(x, y, w, h);
        canvas.dirty();
    }

    @Override
    public void beginPath() {
        core.beginPath();
    }

    @Override
    public void closePath() {
        core.closePath();
    }

    @Override
    public void moveTo(double x, double y) {
        core.moveTo(x, y);
    }

    @Override
    public void lineTo(double x, double y) {
        core.lineTo(x, y);
    }

    @Override
    public void quadraticCurveTo(double cpx, double cpy, double x, double y) {
        core.quadraticCurveTo(cpx, cpy, x, y);
    }

    @Override
    public void bezierCurveTo(double cp1x, double cp1y, double cp2x, double cp2y, double x, double y) {
        core.bezierCurveTo(cp1x, cp1y, cp2x, cp2y, x, y);
    }

    @Override
    public void arcTo(double x1, double y1, double x2, double y2, double radius) {
        core.arcTo(x1, y1, x2, y2, radius);
    }

    @Override
    public void rect(double x, double y, double w, double h) {
        core.rect(x, y, w, h);
    }

    @Override
    public void arc(double x, double y, double radius, double startAngle, double endAngle, boolean counterclockwise) {
        core.arc(x, y, radius, startAngle, endAngle, counterclockwise);
    }

    @Override
    public void ellipse(double x, double y, double radiusX, double radiusY, double rotation, double startAngle, double endAngle, boolean counterclockwise) {
        core.ellipse(x, y, radiusX, radiusY, rotation, startAngle, endAngle, counterclockwise);
    }

    @Override
    public void fill() {
        core.fill();
        canvas.dirty();
    }

    @Override
    public void stroke() {
        core.stroke();
        canvas.dirty();
    }

    @Override
    public void clip() {
        core.clip();
    }

    @Override
    public boolean isPointInPath(double x, double y) {
        return core.isPointInPath(x, y);
    }

    @Override
    public boolean isPointInStroke(double x, double y) {
        return core.isPointInStroke(x, y);
    }

    @Override
    public void drawImage(Object image, double dx, double dy) {
        core.drawImage(image, dx, dy);
        canvas.dirty();
    }

    @Override
    public void drawImage(Object image, double dx, double dy, double dWidth, double dHeight) {
        core.drawImage(image, dx, dy, dWidth, dHeight);
        canvas.dirty();
    }

    @Override
    public void drawImage(Object image, double sx, double sy, double sWidth, double sHeight, double dx, double dy, double dWidth, double dHeight) {
        core.drawImage(image, sx, sy, sWidth, sHeight, dx, dy, dWidth, dHeight);
        canvas.dirty();
    }

    @Override
    public ITextMetrics measureText(String text) {
        return core.measureText(text);
    }

    @Override
    public String getFont() {
        return core.getFont();
    }

    @Override
    public void setFont(String font) {
        core.setFont(font);
    }

    @Override
    public String getTextAlign() {
        return core.getTextAlign();
    }

    @Override
    public void setTextAlign(String textAlign) {
        core.setTextAlign(textAlign);
    }

    @Override
    public String getTextBaseline() {
        return core.getTextBaseline();
    }

    @Override
    public void setTextBaseline(String textBaseline) {
        core.setTextBaseline(textBaseline);
    }

    @Override
    public void fillText(String text, double x, double y, double maxWidth) {
        core.fillText(text, x, y, maxWidth);
        canvas.dirty();
    }

    @Override
    public void strokeText(String text, double x, double y, double maxWidth) {
        core.strokeText(text, x, y, maxWidth);
        canvas.dirty();
    }

    @Override
    public IImageData createImageData(int width, int height) {
        return core.createImageData(width, height);
    }

    @Override
    public IImageData getImageData(int x, int y, int width, int height) {
        return core.getImageData(x, y, width, height);
    }

    @Override
    public void putImageData(IImageData imagedata, int dx, int dy, int dirtyX, int dirtyY, int dirtyWidth, int dirtyHeight) {
        core.putImageData(imagedata, dx, dy, dirtyX, dirtyY, dirtyWidth, dirtyHeight);
    }

    @Override
    public boolean isContextLost() {
        return core.isContextLost();
    }

    @Override
    public Scriptable getContextAttributes() {
        return core.getContextAttributes();
    }

    @Override
    public String getFilter() {
        return core.getFilter();
    }

    @Override
    public void setFilter(String filter) {
        core.setFilter(filter);
    }

    @Override
    public void reset() {
	    core.reset();
    }

    // Rhino-specific methods below

    public void jsFunction_save() {
        save();
    }

    public void jsFunction_restore() {
        restore();
    }

    public void jsFunction_scale(Double x, Double y) {
        scale(x, y);
    }

    public void jsFunction_rotate(Double angle) {
        rotate(angle);
    }

    public void jsFunction_translate(Double x, Double y) {
        translate(x, y);
    }

    public void jsFunction_transform(Double m11, Double m12, Double m21, Double m22, Double dx, Double dy) {
        transform(m11, m12, m21, m22, dx, dy);
    }

    public void jsFunction_setTransform(Double m11, Double m12, Double m21, Double m22, Double dx, Double dy) {
        setTransform(m11, m12, m21, m22, dx, dy);
    }

    public void jsFunction_resetTransform() {
        resetTransform();
    }

    public DOMMatrix jsFunction_getTransform() {
        DOMMatrix matrix = new DOMMatrix();
        matrix.init((AffineTransform) getTransform());
        return matrix;
    }

    public Double jsGet_globalAlpha() {
        return getGlobalAlpha();
    }

    public void jsSet_globalAlpha(Double globalAlpha) {
        setGlobalAlpha(globalAlpha);
    }

    public String jsGet_globalCompositeOperation() {
        return getGlobalCompositeOperation();
    }

    public void jsSet_globalCompositeOperation(String op) {
        setGlobalCompositeOperation(op);
    }

    public Object jsGet_fillStyle() {
        return getFillStyle();
    }

    public void jsSet_fillStyle(Object fillStyle) {
        if (fillStyle instanceof RhinoCanvasGradient) {
            core.setFillStyle(((RhinoCanvasGradient) fillStyle).getBackendGradient());
        } else if (fillStyle instanceof RhinoCanvasPattern) {
            core.setFillStyle(((RhinoCanvasPattern) fillStyle).getBackendPattern());
        } else {
            core.setFillStyle(fillStyle);
        }
    }

    public Object jsGet_strokeStyle() {
        return getStrokeStyle();
    }

    public void jsSet_strokeStyle(Object strokeStyle) {
        if (strokeStyle instanceof RhinoCanvasGradient) {
            core.setStrokeStyle(((RhinoCanvasGradient) strokeStyle).getBackendGradient());
        } else if (strokeStyle instanceof RhinoCanvasPattern) {
            core.setStrokeStyle(((RhinoCanvasPattern) strokeStyle).getBackendPattern());
        } else {
            core.setStrokeStyle(strokeStyle);
        }
    }

    public RhinoCanvasGradient jsFunction_createLinearGradient(Double x0, Double y0, Double x1, Double y1) {
        ICanvasGradient backendGradient = createLinearGradient(x0, y0, x1, y1);
        RhinoCanvasGradient rhinoGradient = (RhinoCanvasGradient) Context.getCurrentContext().newObject(getParentScope(), "RhinoCanvasGradient");
        rhinoGradient.init(backendGradient);
        return rhinoGradient;
    }

    public RhinoCanvasGradient jsFunction_createRadialGradient(Double x0, Double y0, Double r0, Double x1, Double y1, Double r1) {
        ICanvasGradient backendGradient = createRadialGradient(x0, y0, r0, x1, y1, r1);
        RhinoCanvasGradient rhinoGradient = (RhinoCanvasGradient) Context.getCurrentContext().newObject(getParentScope(), "RhinoCanvasGradient");
        rhinoGradient.init(backendGradient);
        return rhinoGradient;
    }

    public RhinoCanvasPattern jsFunction_createPattern(Image image, String repetition) {
        ICanvasPattern backendPattern = createPattern(image, repetition);
        RhinoCanvasPattern rhinoPattern = (RhinoCanvasPattern) Context.getCurrentContext().newObject(getParentScope(), "RhinoCanvasPattern");
        rhinoPattern.init(backendPattern);
        return rhinoPattern;
    }

    public void jsSet_lineDashOffset(Double offset) {
        setLineDashOffset(offset);
    }

    public double jsGet_lineDashOffset() {
        return getLineDashOffset();
    }

    public void jsFunction_setLineDash(NativeArray dash) {
        double[] lineDash = new double[(int) dash.getLength()];
        for (int i = 0; i < lineDash.length; i++) {
            lineDash[i] = ((Number) dash.get(i, dash)).doubleValue();
        }
        setLineDash(lineDash);
    }

    public Scriptable jsFunction_getLineDash() {
        Object lineDashObj = getLineDash();
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
        setLineWidth(lw);
    }

    public Double jsGet_lineWidth() {
        return getLineWidth();
    }

    public void jsSet_lineCap(String cap) {
        setLineCap(cap);
    }

    public String jsGet_lineCap() {
        return getLineCap();
    }

    public void jsSet_lineJoin(String join) {
        setLineJoin(join);
    }

    public String jsGet_lineJoin() {
        return getLineJoin();
    }

    public void jsSet_miterLimit(Double miterLimit) {
        setMiterLimit(miterLimit);
    }

    public Double jsGet_miterLimit() {
        return getMiterLimit();
    }

    public void jsFunction_clearRect(Double x, Double y, Double w, Double h) {
        clearRect(x, y, w, h);
    }

    public void jsFunction_fillRect(Double x, Double y, Double w, Double h) {
        fillRect(x, y, w, h);
    }

    public void jsFunction_strokeRect(Double x, Double y, Double w, Double h) {
        strokeRect(x, y, w, h);
    }

    public void jsFunction_beginPath() {
        beginPath();
    }

    public void jsFunction_closePath() {
        closePath();
    }

    public void jsFunction_moveTo(Double x, Double y) {
        moveTo(x, y);
    }

    public void jsFunction_lineTo(Double x, Double y) {
        lineTo(x, y);
    }

    public void jsFunction_quadraticCurveTo(Double cpx, Double cpy, Double x, Double y) {
        quadraticCurveTo(cpx, cpy, x, y);
    }

    public void jsFunction_bezierCurveTo(Double cp1x, Double cp1y, Double cp2x, Double cp2y, Double x, Double y) {
        bezierCurveTo(cp1x, cp1y, cp2x, cp2y, x, y);
    }

    public void jsFunction_arcTo(Double x1, Double y1, Double x2, Double y2, Double radius) {
        arcTo(x1, y1, x2, y2, radius);
    }

    public void jsFunction_rect(Double x, Double y, Double w, Double h) {
        rect(x, y, w, h);
    }

    public void jsFunction_arc(Double x, Double y, Double radius, Double startAngle, Double endAngle, boolean counterclockwise) {
        arc(x, y, radius, startAngle, endAngle, counterclockwise);
    }

    public void jsFunction_ellipse(double x, double y, double radiusX, double radiusY, double rotation, double startAngle, double endAngle, boolean counterclockwise) {
        ellipse(x, y, radiusX, radiusY, rotation, startAngle, endAngle, counterclockwise);
    }

    public void jsFunction_fill() {
        fill();
    }

    public void jsFunction_stroke() {
        stroke();
    }

    public void jsFunction_clip() {
        clip();
    }

    public boolean jsFunction_isPointInPath(Double x, Double y) {
        return isPointInPath(x, y);
    }

    public boolean jsFunction_isPointInStroke(Double x, Double y) {
        return isPointInStroke(x, y);
    }

    public void jsFunction_drawImage(Image image, int sx, int sy, int sw, int sh, int dx, int dy, int dw, int dh) {
        drawImage(image, sx, sy, sw, sh, dx, dy, dw, dh);
    }

    @Override
    public TextMetrics jsFunction_measureText(String text) {
        ITextMetrics metrics = measureText(text);
        if (metrics != null) {
            return RhinoCanvasUtils.getScriptableInstance(TextMetrics.class, new Double[] { metrics.getWidth() });
        }
        return RhinoCanvasUtils.getScriptableInstance(TextMetrics.class, new Double[] { 0.0 });
    }

    @Override
    public String jsGet_font() {
        return getFont();
    }

    @Override
    public void jsSet_font(String font) {
        setFont(font);
    }

    @Override
    public void jsFunction_fillText(String text, Double x, Double y, int maxWidth) {
        fillText(text, x, y, maxWidth);
    }

    @Override
    public void jsFunction_strokeText(String text, Double x, Double y, int maxWidth) {
        strokeText(text, x, y, maxWidth);
    }

    @Override
    public String jsGet_textAlign() {
        return getTextAlign();
    }

    @Override
    public String jsGet_textBaseline() {
        return getTextBaseline();
    }

    @Override
    public void jsSet_textAlign(String textAlign) {
        setTextAlign(textAlign);
    }

    @Override
    public void jsSet_textBaseline(String textBaseline) {
        setTextBaseline(textBaseline);
    }

    public ImageData jsFunction_createImageData(int width, int height) {
        IImageData coreImageData = createImageData(width, height);
        ImageData rhinoImageData = (ImageData) Context.getCurrentContext().newObject(getParentScope(), "ImageData");
        rhinoImageData.init(coreImageData);
        return rhinoImageData;
    }

    public ImageData jsFunction_getImageData(int x, int y, int width, int height) {
        IImageData coreImageData = getImageData(x, y, width, height);
        ImageData rhinoImageData = (ImageData) Context.getCurrentContext().newObject(getParentScope(), "ImageData");
        rhinoImageData.init(coreImageData);
        return rhinoImageData;
    }

    public void jsFunction_putImageData(ImageData imagedata, int dx, int dy, Object dirtyX, Object dirtyY, Object dirtyWidth, Object dirtyHeight) {
        int dX = (dirtyX instanceof Number) ? ((Number) dirtyX).intValue() : 0;
        int dY = (dirtyY instanceof Number) ? ((Number) dirtyY).intValue() : 0;
        int dWidth = (dirtyWidth instanceof Number) ? ((Number) dirtyWidth).intValue() : imagedata.getWidth();
        int dHeight = (dirtyHeight instanceof Number) ? ((Number) dirtyHeight).intValue() : imagedata.getHeight();
        putImageData(imagedata, dx, dy, dX, dY, dWidth, dHeight);
    }

    public void jsFunction_reset() {
        reset();
    }

    public boolean jsFunction_isContextLost() {
        return isContextLost();
    }

    public Scriptable jsFunction_getContextAttributes() {
        return getContextAttributes();
    }

    public String jsGet_filter() {
        return getFilter();
    }

    public void jsSet_filter(String filter) {
        setFilter(filter);
    }
}
