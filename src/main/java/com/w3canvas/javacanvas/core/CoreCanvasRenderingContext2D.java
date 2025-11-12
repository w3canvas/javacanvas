package com.w3canvas.javacanvas.core;

import com.w3canvas.javacanvas.backend.rhino.impl.node.CanvasPixelArray;
import com.w3canvas.javacanvas.backend.rhino.impl.node.Document;
import com.w3canvas.javacanvas.interfaces.*;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import java.util.Stack;

public class CoreCanvasRenderingContext2D implements ICanvasRenderingContext2D {

    private final IGraphicsBackend backend;
    private final ICanvasSurface surface;
    private final IGraphicsContext gc;
    private Document document;

    private Stack<ContextState> stack;
    private Object fillStyle;
    private Object strokeStyle;
    private double globalAlpha;
    private String globalCompositeOperation;
    private double lineWidth;
    private String lineJoin;
    private String lineCap;
    private double miterLimit;
    private Object lineDash;
    private double lineDashOffset;

    // Shadow properties
    private double shadowBlur;
    private String shadowColor;
    private double shadowOffsetX;
    private double shadowOffsetY;

    // Image smoothing
    private boolean imageSmoothingEnabled;
    private String imageSmoothingQuality;

    // Modern text properties
    private String direction;
    private double letterSpacing;
    private double wordSpacing;

    public CoreCanvasRenderingContext2D(Document document, IGraphicsBackend backend, int width, int height) {
        this.document = document;
        this.backend = backend;
        this.surface = backend.createCanvasSurface(width, height);
        this.gc = surface.getGraphicsContext();
        reset();
    }

    @Override
    public ICanvasSurface getSurface() {
        return surface;
    }

    @Override
    public void reset() {
        surface.reset();
        stack = new Stack<>();
        fillStyle = "#000000";
        strokeStyle = "#000000";
        globalAlpha = 1.0;
        globalCompositeOperation = "source-over";
        lineWidth = 1.0;
        lineJoin = "miter";
        lineCap = "butt";
        miterLimit = 10.0;
        lineDash = new double[0];
        lineDashOffset = 0.0;

        // Initialize shadow properties
        shadowBlur = 0.0;
        shadowColor = "rgba(0, 0, 0, 0)"; // transparent black
        shadowOffsetX = 0.0;
        shadowOffsetY = 0.0;

        // Initialize image smoothing
        imageSmoothingEnabled = true;
        imageSmoothingQuality = "low";

        // Initialize modern text properties
        direction = "inherit";
        letterSpacing = 0.0;
        wordSpacing = 0.0;

        setFont("10px sans-serif");
        setTextAlign("start");
        setTextBaseline("alphabetic");
        gc.resetTransform();
    }

    @Override
    public void save() {
        stack.push(new ContextState(this));
    }

    @Override
    public void restore() {
        if (!stack.isEmpty()) {
            ContextState state = stack.pop();
            state.apply(this);
        }
    }

    @Override
    public void scale(double x, double y) {
        gc.scale(x, y);
    }

    @Override
    public void rotate(double angle) {
        gc.rotate(angle);
    }

    @Override
    public void translate(double x, double y) {
        gc.translate(x, y);
    }

    @Override
    public void transform(double m11, double m12, double m21, double m22, double dx, double dy) {
        gc.transform(m11, m12, m21, m22, dx, dy);
    }

    @Override
    public void setTransform(double m11, double m12, double m21, double m22, double dx, double dy) {
        gc.setTransform(m11, m12, m21, m22, dx, dy);
    }

    @Override
    public void resetTransform() {
        gc.resetTransform();
    }

    @Override
    public Object getTransform() {
        return gc.getTransform();
    }

    @Override
    public double getGlobalAlpha() {
        return globalAlpha;
    }

    @Override
    public void setGlobalAlpha(double globalAlpha) {
        this.globalAlpha = globalAlpha;
    }

    @Override
    public String getGlobalCompositeOperation() {
        return globalCompositeOperation;
    }

    @Override
    public void setGlobalCompositeOperation(String op) {
        this.globalCompositeOperation = op;
    }

    @Override
    public Object getFillStyle() {
        return fillStyle;
    }

    @Override
    public void setFillStyle(Object fillStyle) {
        this.fillStyle = fillStyle;
    }

    @Override
    public Object getStrokeStyle() {
        return strokeStyle;
    }

    @Override
    public void setStrokeStyle(Object strokeStyle) {
        this.strokeStyle = strokeStyle;
    }

    @Override
    public ICanvasGradient createLinearGradient(double x0, double y0, double x1, double y1) {
        return backend.createLinearGradient(x0, y0, x1, y1);
    }

    @Override
    public ICanvasGradient createRadialGradient(double x0, double y0, double r0, double x1, double y1, double r1) {
        return backend.createRadialGradient(x0, y0, r0, x1, y1, r1);
    }

    @Override
    public ICanvasGradient createConicGradient(double startAngle, double x, double y) {
        return backend.createConicGradient(startAngle, x, y);
    }

    @Override
    public ICanvasPattern createPattern(Object image, String repetition) {
        return backend.createPattern(image, repetition);
    }

    @Override
    public double getLineWidth() {
        return lineWidth;
    }

    @Override
    public void setLineWidth(double lw) {
        this.lineWidth = lw;
    }

    @Override
    public String getLineCap() {
        return lineCap;
    }

    @Override
    public void setLineCap(String cap) {
        this.lineCap = cap;
    }

    @Override
    public String getLineJoin() {
        return lineJoin;
    }

    @Override
    public void setLineJoin(String join) {
        this.lineJoin = join;
    }

    @Override
    public double getMiterLimit() {
        return miterLimit;
    }

    @Override
    public void setMiterLimit(double miterLimit) {
        this.miterLimit = miterLimit;
    }

    @Override
    public void setLineDash(Object dash) {
        this.lineDash = dash;
    }

    @Override
    public Object getLineDash() {
        return lineDash;
    }

    @Override
    public double getLineDashOffset() {
        return lineDashOffset;
    }

    @Override
    public void setLineDashOffset(double offset) {
        this.lineDashOffset = offset;
    }

    // Shadow properties implementation
    @Override
    public double getShadowBlur() {
        return shadowBlur;
    }

    @Override
    public void setShadowBlur(double blur) {
        this.shadowBlur = Math.max(0, blur); // Ensure non-negative
    }

    @Override
    public String getShadowColor() {
        return shadowColor;
    }

    @Override
    public void setShadowColor(String color) {
        this.shadowColor = color != null ? color : "rgba(0, 0, 0, 0)";
    }

    @Override
    public double getShadowOffsetX() {
        return shadowOffsetX;
    }

    @Override
    public void setShadowOffsetX(double offsetX) {
        this.shadowOffsetX = offsetX;
    }

    @Override
    public double getShadowOffsetY() {
        return shadowOffsetY;
    }

    @Override
    public void setShadowOffsetY(double offsetY) {
        this.shadowOffsetY = offsetY;
    }

    // Image smoothing implementation
    @Override
    public boolean getImageSmoothingEnabled() {
        return imageSmoothingEnabled;
    }

    @Override
    public void setImageSmoothingEnabled(boolean enabled) {
        this.imageSmoothingEnabled = enabled;
    }

    @Override
    public String getImageSmoothingQuality() {
        return imageSmoothingQuality;
    }

    @Override
    public void setImageSmoothingQuality(String quality) {
        // Validate quality: "low", "medium", "high"
        if ("low".equals(quality) || "medium".equals(quality) || "high".equals(quality)) {
            this.imageSmoothingQuality = quality;
        }
    }

    @Override
    public void clearRect(double x, double y, double w, double h) {
        gc.clearRect(x, y, w, h);
    }

    @Override
    public void fillRect(double x, double y, double w, double h) {
        if ("copy".equals(this.globalCompositeOperation)) {
            gc.clearRect(x, y, w, h);
        }
        gc.beginPath();
        gc.rect(x, y, w, h);
        fill();
    }

    @Override
    public void strokeRect(double x, double y, double w, double h) {
        gc.beginPath();
        gc.rect(x, y, w, h);
        stroke();
    }

    @Override
    public void beginPath() {
        gc.beginPath();
    }

    @Override
    public void closePath() {
        gc.closePath();
    }

    @Override
    public void moveTo(double x, double y) {
        gc.moveTo(x, y);
    }

    @Override
    public void lineTo(double x, double y) {
        gc.lineTo(x, y);
    }

    @Override
    public void quadraticCurveTo(double cpx, double cpy, double x, double y) {
        gc.quadraticCurveTo(cpx, cpy, x, y);
    }

    @Override
    public void bezierCurveTo(double cp1x, double cp1y, double cp2x, double cp2y, double x, double y) {
        gc.bezierCurveTo(cp1x, cp1y, cp2x, cp2y, x, y);
    }

    @Override
    public void arcTo(double x1, double y1, double x2, double y2, double radius) {
        gc.arcTo(x1, y1, x2, y2, radius);
    }

    @Override
    public void rect(double x, double y, double w, double h) {
        gc.rect(x, y, w, h);
    }

    @Override
    public void roundRect(double x, double y, double w, double h, Object radii) {
        gc.roundRect(x, y, w, h, radii);
    }

    @Override
    public void arc(double x, double y, double radius, double startAngle, double endAngle, boolean counterclockwise) {
        gc.arc(x, y, radius, startAngle, endAngle, counterclockwise);
    }

    @Override
    public void ellipse(double x, double y, double radiusX, double radiusY, double rotation, double startAngle, double endAngle, boolean counterclockwise) {
        gc.ellipse(x,y,radiusX, radiusY, rotation, startAngle, endAngle, counterclockwise);
    }

    private void applyCurrentState() {
        IComposite composite = CompositeFactory.createComposite(this.globalCompositeOperation, this.globalAlpha, this.backend);
        if (composite != null) {
            gc.setComposite(composite);
        }
        gc.setGlobalAlpha(this.globalAlpha);
    }

    @Override
    public void fill() {
        applyCurrentState();
        if (fillStyle instanceof String) {
            gc.setFillPaint(ColorParser.parse((String) fillStyle, backend));
        } else if (fillStyle instanceof IPaint) {
            gc.setFillPaint((IPaint) fillStyle);
        } else if (fillStyle instanceof ICanvasPattern) {
            // The backend needs to handle this.
        }
        gc.fill(gc.getPath());
    }

    @Override
    public void stroke() {
        applyCurrentState();
        if (strokeStyle instanceof String) {
            gc.setStrokePaint(ColorParser.parse((String) strokeStyle, backend));
        } else if (strokeStyle instanceof IPaint) {
            gc.setStrokePaint((IPaint) strokeStyle);
        } else if (strokeStyle instanceof ICanvasPattern) {
            // The backend needs to handle this.
        }

        gc.setLineWidth(this.lineWidth);
        gc.setLineCap(this.lineCap);
        gc.setLineJoin(this.lineJoin);
        gc.setMiterLimit(this.miterLimit);
        if (this.lineDash instanceof double[]) {
            gc.setLineDash((double[]) this.lineDash);
        } else {
            gc.setLineDash(null);
        }
        gc.setLineDashOffset(this.lineDashOffset);

        gc.stroke();
    }

    @Override
    public void clip() {
        gc.clip();
    }

    @Override
    public boolean isPointInPath(double x, double y) {
        return gc.isPointInPath(x, y);
    }

    @Override
    public boolean isPointInStroke(double x, double y) {
        return gc.isPointInStroke(x, y);
    }

    private Object getNativeImage(Object image) {
        if (image instanceof com.w3canvas.javacanvas.backend.rhino.impl.node.HTMLCanvasElement) {
            com.w3canvas.javacanvas.backend.rhino.impl.node.HTMLCanvasElement canvas = (com.w3canvas.javacanvas.backend.rhino.impl.node.HTMLCanvasElement) image;
            ICanvasRenderingContext2D ctx = (ICanvasRenderingContext2D) canvas.jsFunction_getContext("2d");
            return ctx.getSurface().getNativeImage();
        } else if (image instanceof com.w3canvas.javacanvas.backend.rhino.impl.node.Image) {
            return ((com.w3canvas.javacanvas.backend.rhino.impl.node.Image) image).getImage();
        }
        return null;
    }

    private int getImageWidth(Object image) {
        if (image instanceof com.w3canvas.javacanvas.backend.rhino.impl.node.HTMLCanvasElement) {
            return ((com.w3canvas.javacanvas.backend.rhino.impl.node.HTMLCanvasElement) image).getWidth();
        } else if (image instanceof com.w3canvas.javacanvas.backend.rhino.impl.node.Image) {
            return ((com.w3canvas.javacanvas.backend.rhino.impl.node.Image) image).getRealWidth();
        }
        return 0;
    }

    private int getImageHeight(Object image) {
        if (image instanceof com.w3canvas.javacanvas.backend.rhino.impl.node.HTMLCanvasElement) {
            return ((com.w3canvas.javacanvas.backend.rhino.impl.node.HTMLCanvasElement) image).getHeight();
        } else if (image instanceof com.w3canvas.javacanvas.backend.rhino.impl.node.Image) {
            return ((com.w3canvas.javacanvas.backend.rhino.impl.node.Image) image).getRealHeight();
        }
        return 0;
    }

    @Override
    public void drawImage(Object image, double dx, double dy) {
        int width = getImageWidth(image);
        int height = getImageHeight(image);
        drawImage(image, dx, dy, width, height);
    }

    @Override
    public void drawImage(Object image, double dx, double dy, double dWidth, double dHeight) {
        int sWidth = getImageWidth(image);
        int sHeight = getImageHeight(image);
        drawImage(image, 0, 0, sWidth, sHeight, dx, dy, dWidth, dHeight);
    }

    @Override
    public void drawImage(Object image, double sx, double sy, double sWidth, double sHeight, double dx, double dy, double dWidth, double dHeight) {
        Object nativeImage = getNativeImage(image);
        if (nativeImage != null) {
            gc.drawImage(nativeImage, (int) sx, (int) sy, (int) sWidth, (int) sHeight, (int) dx, (int) dy, (int) dWidth, (int) dHeight);
        }
    }

    @Override
    public ITextMetrics measureText(String text) {
        return gc.measureText(text);
    }

    private String font;
    private String textAlign;
    private String textBaseline;

    @Override
    public String getFont() {
        return font;
    }

    @Override
    public void setFont(String font) {
        this.font = font;

        if (document != null) {
            for (com.w3canvas.javacanvas.dom.FontFace face : document.jsGet_fonts().getFaces()) {
                if (font.contains(face.getFamily())) {
                    if ("loaded".equals(face.getStatus())) {
                        java.util.Map<String, Object> fontInfo = com.w3canvas.css.CSSParser.parseFont(font);
                        float size = (Float) fontInfo.get("size");
                        IFont newFont = backend.createFont(face.getFontData(), size, face.getStyle(), face.getWeight());
                        if (newFont != null) {
                            face.setFont(newFont);
                            gc.setFont(newFont);
                            return;
                        }
                    } else {
                        face.load();
                    }
                }
            }
        }

        java.util.Map<String, Object> fontInfo = com.w3canvas.css.CSSParser.parseFont(font);
        String style = (String) fontInfo.get("style");
        String weight = (String) fontInfo.get("weight");
        float size = (Float) fontInfo.get("size");
        String family = (String) fontInfo.get("family");
        IFont newFont = backend.createFont(family, size, style, weight);
        gc.setFont(newFont);
    }

    @Override
    public String getTextAlign() {
        return textAlign;
    }

    @Override
    public void setTextAlign(String textAlign) {
        this.textAlign = textAlign;
    }

    @Override
    public String getTextBaseline() {
        return textBaseline;
    }

    @Override
    public void setTextBaseline(String textBaseline) {
        this.textBaseline = textBaseline;
    }

    // Modern text properties implementation
    @Override
    public String getDirection() {
        return direction;
    }

    @Override
    public void setDirection(String direction) {
        // Validate direction: "ltr", "rtl", "inherit"
        if ("ltr".equals(direction) || "rtl".equals(direction) || "inherit".equals(direction)) {
            this.direction = direction;
        }
    }

    @Override
    public double getLetterSpacing() {
        return letterSpacing;
    }

    @Override
    public void setLetterSpacing(double spacing) {
        this.letterSpacing = spacing;
    }

    @Override
    public double getWordSpacing() {
        return wordSpacing;
    }

    @Override
    public void setWordSpacing(double spacing) {
        this.wordSpacing = spacing;
    }

    @Override
    public void fillText(String text, double x, double y, double maxWidth) {
        applyCurrentState();
        if (fillStyle instanceof String) {
            gc.setFillPaint(ColorParser.parse((String) fillStyle, backend));
        } else if (fillStyle instanceof IPaint) {
            gc.setFillPaint((IPaint) fillStyle);
        }
        gc.setTextAlign(textAlign);
        gc.setTextBaseline(textBaseline);
        gc.fillText(text, x, y, maxWidth);
    }

    @Override
    public void strokeText(String text, double x, double y, double maxWidth) {
        applyCurrentState();
        if (strokeStyle instanceof String) {
            gc.setStrokePaint(ColorParser.parse((String) strokeStyle, backend));
        } else if (strokeStyle instanceof IPaint) {
            gc.setStrokePaint((IPaint) strokeStyle);
        }
        gc.setTextAlign(textAlign);
        gc.setTextBaseline(textBaseline);
        gc.strokeText(text, x, y, maxWidth);
    }

    @Override
    public IImageData createImageData(int width, int height) {
        return gc.createImageData(width, height);
    }

    @Override
    public IImageData getImageData(int x, int y, int width, int height) {
        return gc.getImageData(x, y, width, height);
    }

    @Override
    public void putImageData(IImageData imagedata, int dx, int dy, int dirtyX, int dirtyY, int dirtyWidth, int dirtyHeight) {
        ICanvasPixelArray pixelArray;
        if (imagedata instanceof com.w3canvas.javacanvas.backend.rhino.impl.node.ImageData) {
            pixelArray = ((com.w3canvas.javacanvas.backend.rhino.impl.node.ImageData) imagedata).getData();
        } else {
            pixelArray = imagedata.getData();
        }
        int[] pixels = pixelArray.getPixels(dirtyX, dirtyY, dirtyWidth, dirtyHeight);
        surface.getGraphicsContext().drawImage(pixels, dx, dy, dirtyWidth, dirtyHeight);
    }

    @Override
    public boolean isContextLost() {
        return false;
    }

    @Override
    public Scriptable getContextAttributes() {
        return null;
    }

    @Override
    public String getFilter() {
        return "none";
    }

    @Override
    public void setFilter(String filter) {
    }

    private static class ContextState {
        private final Object fillStyle;
        private final Object strokeStyle;
        private final double globalAlpha;
        private final String globalCompositeOperation;
        private final double lineWidth;
        private final String lineJoin;
        private final String lineCap;
        private final double miterLimit;
        private final Object lineDash;
        private final double lineDashOffset;
        private final Object transform;

        // Shadow properties
        private final double shadowBlur;
        private final String shadowColor;
        private final double shadowOffsetX;
        private final double shadowOffsetY;

        // Image smoothing
        private final boolean imageSmoothingEnabled;
        private final String imageSmoothingQuality;

        // Modern text properties
        private final String direction;
        private final double letterSpacing;
        private final double wordSpacing;

        ContextState(CoreCanvasRenderingContext2D ctx) {
            this.fillStyle = ctx.fillStyle;
            this.strokeStyle = ctx.strokeStyle;
            this.globalAlpha = ctx.globalAlpha;
            this.globalCompositeOperation = ctx.globalCompositeOperation;
            this.lineWidth = ctx.lineWidth;
            this.lineJoin = ctx.lineJoin;
            this.lineCap = ctx.lineCap;
            this.miterLimit = ctx.miterLimit;
            this.lineDash = ctx.lineDash;
            this.lineDashOffset = ctx.lineDashOffset;
            this.transform = ctx.gc.getTransform();

            // Save shadow properties
            this.shadowBlur = ctx.shadowBlur;
            this.shadowColor = ctx.shadowColor;
            this.shadowOffsetX = ctx.shadowOffsetX;
            this.shadowOffsetY = ctx.shadowOffsetY;

            // Save image smoothing
            this.imageSmoothingEnabled = ctx.imageSmoothingEnabled;
            this.imageSmoothingQuality = ctx.imageSmoothingQuality;

            // Save modern text properties
            this.direction = ctx.direction;
            this.letterSpacing = ctx.letterSpacing;
            this.wordSpacing = ctx.wordSpacing;
        }

        void apply(CoreCanvasRenderingContext2D ctx) {
            ctx.fillStyle = this.fillStyle;
            ctx.strokeStyle = this.strokeStyle;
            ctx.globalAlpha = this.globalAlpha;
            ctx.globalCompositeOperation = this.globalCompositeOperation;
            ctx.lineWidth = this.lineWidth;
            ctx.lineJoin = this.lineJoin;
            ctx.lineCap = this.lineCap;
            ctx.miterLimit = this.miterLimit;
            ctx.lineDash = this.lineDash;
            ctx.lineDashOffset = this.lineDashOffset;
            ctx.gc.setTransform(this.transform);

            // Restore shadow properties
            ctx.shadowBlur = this.shadowBlur;
            ctx.shadowColor = this.shadowColor;
            ctx.shadowOffsetX = this.shadowOffsetX;
            ctx.shadowOffsetY = this.shadowOffsetY;

            // Restore image smoothing
            ctx.imageSmoothingEnabled = this.imageSmoothingEnabled;
            ctx.imageSmoothingQuality = this.imageSmoothingQuality;

            // Restore modern text properties
            ctx.direction = this.direction;
            ctx.letterSpacing = this.letterSpacing;
            ctx.wordSpacing = this.wordSpacing;
        }
    }
}
