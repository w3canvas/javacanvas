package com.w3canvas.javacanvas.core;

import com.w3canvas.javacanvas.backend.rhino.impl.node.CanvasPixelArray;
import com.w3canvas.javacanvas.backend.rhino.impl.node.Document;
import com.w3canvas.javacanvas.interfaces.*;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import java.awt.image.BufferedImage;
import java.util.Stack;
import com.w3canvas.javacanvas.core.Path2D;

/**
 * Backend-agnostic implementation of the Canvas 2D rendering context.
 *
 * <p>This class provides the core implementation of {@link ICanvasRenderingContext2D},
 * delegating actual rendering operations to a backend-specific {@link IGraphicsContext}.
 * It maintains the rendering state (fill/stroke styles, line properties, text properties,
 * shadows, filters, etc.) and manages the state stack for save/restore operations.
 *
 * <p>The separation between this class and the backend allows JavaCanvas to support
 * multiple rendering backends (AWT, JavaFX, etc.) while providing a consistent API.
 *
 * <p>Key responsibilities:
 * <ul>
 *   <li>Maintaining drawing state (styles, compositing, line properties, etc.)</li>
 *   <li>Managing the state stack via {@link #save()} and {@link #restore()}</li>
 *   <li>Delegating low-level rendering to the backend-specific graphics context</li>
 *   <li>Handling Path2D objects for reusable paths</li>
 *   <li>Applying global state (alpha, composite operations, filters) before drawing</li>
 * </ul>
 *
 * @see ICanvasRenderingContext2D
 * @see IGraphicsContext
 * @since 1.0
 */
public class CoreCanvasRenderingContext2D implements ICanvasRenderingContext2D {

    private final IGraphicsBackend backend;
    private final ICanvasSurface surface;
    private IGraphicsContext gc;
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

    // Filter property
    private String filter;

    public CoreCanvasRenderingContext2D(Document document, IGraphicsBackend backend, int width, int height) {
        this.document = document;
        this.backend = backend;
        this.surface = backend.createCanvasSurface(width, height);
        this.gc = surface.getGraphicsContext();
        // Initialize state without calling surface.reset() to avoid disposing the graphics context
        initializeState();
    }

    @Override
    public ICanvasSurface getSurface() {
        return surface;
    }

    /**
     * Initialize the rendering context state without resetting the graphics surface.
     * Used during construction to avoid disposing the graphics context.
     */
    private void initializeState() {
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

        // Initialize filter
        filter = "none";

        setFont("10px sans-serif");
        setTextAlign("start");
        setTextBaseline("alphabetic");
        gc.resetTransform();
    }

    @Override
    public void reset() {
        surface.reset();
        // CRITICAL: After surface.reset() creates a new Graphics2D, we must update our reference
        // Otherwise, all drawing operations will use the old Graphics2D and won't appear in the image
        this.gc = surface.getGraphicsContext();

        // Initialize state using the same logic as constructor
        initializeState();
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
        if (lw <= 0) {
            throw new IllegalArgumentException("Line width must be positive, got: " + lw);
        }
        this.lineWidth = lw;
        gc.setLineWidth(lw);
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
        if (radius < 0) {
            throw new IllegalArgumentException("Radius must be non-negative, got: " + radius);
        }
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
        if (radius < 0) {
            throw new IllegalArgumentException("Radius must be non-negative, got: " + radius);
        }
        gc.arc(x, y, radius, startAngle, endAngle, counterclockwise);
    }

    @Override
    public void ellipse(double x, double y, double radiusX, double radiusY, double rotation, double startAngle, double endAngle, boolean counterclockwise) {
        if (radiusX < 0) {
            throw new IllegalArgumentException("radiusX must be non-negative, got: " + radiusX);
        }
        if (radiusY < 0) {
            throw new IllegalArgumentException("radiusY must be non-negative, got: " + radiusY);
        }
        gc.ellipse(x,y,radiusX, radiusY, rotation, startAngle, endAngle, counterclockwise);
    }

    private void applyCurrentState() {
        IComposite composite = CompositeFactory.createComposite(this.globalCompositeOperation, this.globalAlpha, this.backend);
        if (composite != null) {
            gc.setComposite(composite);
        }
        gc.setGlobalAlpha(this.globalAlpha);

        // Apply shadow properties
        gc.setShadowBlur(this.shadowBlur);
        gc.setShadowColor(this.shadowColor);
        gc.setShadowOffsetX(this.shadowOffsetX);
        gc.setShadowOffsetY(this.shadowOffsetY);

        // Apply image smoothing
        gc.setImageSmoothingEnabled(this.imageSmoothingEnabled);
        gc.setImageSmoothingQuality(this.imageSmoothingQuality);

        // Apply filter
        gc.setFilter(this.filter);

        // Apply modern text properties
        gc.setDirection(this.direction);
        gc.setLetterSpacing(this.letterSpacing);
        gc.setWordSpacing(this.wordSpacing);
    }

    @Override
    public void fill() {
        fill("nonzero");
    }

    @Override
    public void fill(String fillRule) {
        applyCurrentState();
        if (fillStyle instanceof String) {
            gc.setFillPaint(ColorParser.parse((String) fillStyle, backend));
        } else if (fillStyle instanceof IPaint) {
            gc.setFillPaint((IPaint) fillStyle);
        } else if (fillStyle instanceof ICanvasPattern) {
            // The backend needs to handle this.
        }
        if (fillRule != null && ("evenodd".equals(fillRule) || "nonzero".equals(fillRule))) {
            gc.setFillRule(fillRule);
        }
        gc.fill(gc.getPath());
    }

    @Override
    public void fill(IPath2D path) {
        fill(path, "nonzero");
    }

    @Override
    public void fill(IPath2D path, String fillRule) {
        if (path == null) {
            return;
        }

        // Apply current state (including fill style, global alpha, etc.)
        applyCurrentState();
        if (fillStyle instanceof String) {
            gc.setFillPaint(ColorParser.parse((String) fillStyle, backend));
        } else if (fillStyle instanceof IPaint) {
            gc.setFillPaint((IPaint) fillStyle);
        }

        // Set fill rule if specified
        if (fillRule != null && ("evenodd".equals(fillRule) || "nonzero".equals(fillRule))) {
            gc.setFillRule(fillRule);
        }

        // WORKAROUND: JavaFX's GraphicsContext doesn't properly handle multiple rect() calls
        // in the same path. Detect if the path contains multiple RECT elements and fill them
        // individually using fillRect() instead.
        java.util.List<IPath2D.PathElement> elements = path.getElements();
        if (elements != null && elements.size() > 0) {
            // Check if path contains only RECT elements (and maybe MOVE_TO before each)
            boolean allRects = true;
            int rectCount = 0;
            for (IPath2D.PathElement element : elements) {
                if (element.getType() == IPath2D.PathElement.Type.RECT) {
                    rectCount++;
                } else if (element.getType() != IPath2D.PathElement.Type.MOVE_TO &&
                          element.getType() != IPath2D.PathElement.Type.CLOSE_PATH) {
                    allRects = false;
                    break;
                }
            }

            // WORKAROUND: If we have multiple rectangles, use fillRectDirect
            // to bypass JavaFX's path rendering limitation. JavaFX's GraphicsContext
            // doesn't properly render multiple rect() calls within the same path.
            // By using fillRectDirect, we render each rectangle separately which works correctly.
            if (allRects && rectCount > 1) {
                for (IPath2D.PathElement element : elements) {
                    if (element.getType() == IPath2D.PathElement.Type.RECT) {
                        double[] params = element.getParams();
                        // Ensure fill paint is set before each rectangle
                        if (fillStyle instanceof String) {
                            gc.setFillPaint(ColorParser.parse((String) fillStyle, backend));
                        } else if (fillStyle instanceof IPaint) {
                            gc.setFillPaint((IPaint) fillStyle);
                        }
                        // Attempt to use direct fillRect method
                        gc.fillRectDirect(params[0], params[1], params[2], params[3]);
                    }
                }
                return;
            }
        }

        // For other paths, use the normal approach
        gc.beginPath();
        if (path instanceof Path2D) {
            ((Path2D) path).replayOn(gc);
        } else if (path instanceof com.w3canvas.javacanvas.backend.rhino.impl.node.RhinoPath2D) {
            ((com.w3canvas.javacanvas.backend.rhino.impl.node.RhinoPath2D) path).getCorePath().replayOn(gc);
        }

        // Fill the path with the current transform applied
        gc.fill();
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
    public void stroke(IPath2D path) {
        if (path == null) {
            return;
        }

        // Apply current state (including stroke style, line width, etc.)
        applyCurrentState();
        if (strokeStyle instanceof String) {
            gc.setStrokePaint(ColorParser.parse((String) strokeStyle, backend));
        } else if (strokeStyle instanceof IPaint) {
            gc.setStrokePaint((IPaint) strokeStyle);
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

        // Begin a new path and replay the Path2D elements
        gc.beginPath();
        if (path instanceof Path2D) {
            ((Path2D) path).replayOn(gc);
        } else if (path instanceof com.w3canvas.javacanvas.backend.rhino.impl.node.RhinoPath2D) {
            ((com.w3canvas.javacanvas.backend.rhino.impl.node.RhinoPath2D) path).getCorePath().replayOn(gc);
        }

        // Stroke the path with the current transform applied
        gc.stroke();
    }

    @Override
    public void clip() {
        clip("nonzero");
    }

    @Override
    public void clip(String fillRule) {
        if (fillRule != null && ("evenodd".equals(fillRule) || "nonzero".equals(fillRule))) {
            gc.setFillRule(fillRule);
        }
        gc.clip();
    }

    @Override
    public void clip(IPath2D path) {
        clip(path, "nonzero");
    }

    @Override
    public void clip(IPath2D path, String fillRule) {
        if (path == null) {
            return;
        }

        // Set fill rule if specified
        if (fillRule != null && ("evenodd".equals(fillRule) || "nonzero".equals(fillRule))) {
            gc.setFillRule(fillRule);
        }

        // Begin a new path and replay the Path2D elements
        gc.beginPath();
        if (path instanceof Path2D) {
            ((Path2D) path).replayOn(gc);
        } else if (path instanceof com.w3canvas.javacanvas.backend.rhino.impl.node.RhinoPath2D) {
            ((com.w3canvas.javacanvas.backend.rhino.impl.node.RhinoPath2D) path).getCorePath().replayOn(gc);
        }

        // Clip the path
        gc.clip();
    }

    @Override
    public boolean isPointInPath(double x, double y) {
        return gc.isPointInPath(x, y);
    }

    @Override
    public boolean isPointInPath(IPath2D path, double x, double y) {
        if (path == null) {
            return false;
        }
        // Save the current path
        IShape savedPath = gc.getPath();

        // Replay the provided path onto the graphics context
        gc.beginPath();
        if (path instanceof Path2D) {
            ((Path2D) path).replayOn(gc);
        } else if (path instanceof com.w3canvas.javacanvas.backend.rhino.impl.node.RhinoPath2D) {
            ((com.w3canvas.javacanvas.backend.rhino.impl.node.RhinoPath2D) path).getCorePath().replayOn(gc);
        }

        // Check if point is in the path
        boolean result = gc.isPointInPath(x, y);

        // Restore the saved path
        gc.setPath(savedPath);

        return result;
    }

    @Override
    public boolean isPointInStroke(double x, double y) {
        return gc.isPointInStroke(x, y);
    }

    @Override
    public boolean isPointInStroke(IPath2D path, double x, double y) {
        if (path == null) {
            return false;
        }
        // Save the current path
        IShape savedPath = gc.getPath();

        // Replay the provided path onto the graphics context
        gc.beginPath();
        if (path instanceof Path2D) {
            ((Path2D) path).replayOn(gc);
        } else if (path instanceof com.w3canvas.javacanvas.backend.rhino.impl.node.RhinoPath2D) {
            ((com.w3canvas.javacanvas.backend.rhino.impl.node.RhinoPath2D) path).getCorePath().replayOn(gc);
        }

        // Check if point is in the stroke
        boolean result = gc.isPointInStroke(x, y);

        // Restore the saved path
        gc.setPath(savedPath);

        return result;
    }

    private Object getNativeImage(Object image) {
        if (image instanceof com.w3canvas.javacanvas.backend.rhino.impl.node.HTMLCanvasElement) {
            com.w3canvas.javacanvas.backend.rhino.impl.node.HTMLCanvasElement canvas = (com.w3canvas.javacanvas.backend.rhino.impl.node.HTMLCanvasElement) image;
            ICanvasRenderingContext2D ctx = (ICanvasRenderingContext2D) canvas.jsFunction_getContext("2d");
            return ctx.getSurface().getNativeImage();
        } else if (image instanceof com.w3canvas.javacanvas.backend.rhino.impl.node.Image) {
            return ((com.w3canvas.javacanvas.backend.rhino.impl.node.Image) image).getImage();
        } else if (image instanceof IImageBitmap) {
            return ((IImageBitmap) image).getNativeImage();
        } else if (image instanceof BufferedImage) {
            // Handle BufferedImage directly (e.g., from ImageBitmap.getNativeImage())
            return image;
        }
        return null;
    }

    private int getImageWidth(Object image) {
        if (image instanceof com.w3canvas.javacanvas.backend.rhino.impl.node.HTMLCanvasElement) {
            return ((com.w3canvas.javacanvas.backend.rhino.impl.node.HTMLCanvasElement) image).getWidth();
        } else if (image instanceof com.w3canvas.javacanvas.backend.rhino.impl.node.Image) {
            return ((com.w3canvas.javacanvas.backend.rhino.impl.node.Image) image).getRealWidth();
        } else if (image instanceof IImageBitmap) {
            return ((IImageBitmap) image).getWidth();
        } else if (image instanceof BufferedImage) {
            return ((BufferedImage) image).getWidth();
        }
        return 0;
    }

    private int getImageHeight(Object image) {
        if (image instanceof com.w3canvas.javacanvas.backend.rhino.impl.node.HTMLCanvasElement) {
            return ((com.w3canvas.javacanvas.backend.rhino.impl.node.HTMLCanvasElement) image).getHeight();
        } else if (image instanceof com.w3canvas.javacanvas.backend.rhino.impl.node.Image) {
            return ((com.w3canvas.javacanvas.backend.rhino.impl.node.Image) image).getRealHeight();
        } else if (image instanceof IImageBitmap) {
            return ((IImageBitmap) image).getHeight();
        } else if (image instanceof BufferedImage) {
            return ((BufferedImage) image).getHeight();
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

    /**
     * Gets the text direction setting.
     *
     * <p><strong>Implementation Status:</strong> This property is implemented in the AWT backend
     * using TextLayout for bidirectional text support. The JavaFX backend currently stores the
     * value but does not affect rendering.
     *
     * @return Current direction setting: "ltr", "rtl", or "inherit"
     */
    @Override
    public String getDirection() {
        return direction;
    }

    /**
     * Sets the text direction for rendering.
     *
     * <p><strong>Implementation Status:</strong> This property is implemented in the AWT backend.
     *
     * <p><strong>Valid Values:</strong>
     * <ul>
     *   <li>"ltr" - Left-to-right text direction</li>
     *   <li>"rtl" - Right-to-left text direction</li>
     *   <li>"inherit" - Inherit from canvas element's CSS direction (default)</li>
     * </ul>
     *
     * @param direction The text direction ("ltr", "rtl", or "inherit"). Invalid values are ignored.
     */
    @Override
    public void setDirection(String direction) {
        // Validate direction: "ltr", "rtl", "inherit"
        if ("ltr".equals(direction) || "rtl".equals(direction) || "inherit".equals(direction)) {
            this.direction = direction;
            gc.setDirection(direction);
        }
    }

    /**
     * Gets the letter spacing for text rendering.
     *
     * <p><strong>Implementation Status:</strong> This property is implemented in the AWT backend
     * using TextAttributes. The JavaFX backend currently stores the value but does not affect rendering.
     *
     * @return Current letter spacing value in pixels (default: 0.0)
     */
    @Override
    public double getLetterSpacing() {
        return letterSpacing;
    }

    /**
     * Sets the letter spacing (tracking) for text rendering.
     *
     * <p><strong>Implementation Status:</strong> This property is implemented in the AWT backend.
     *
     * <p>This property adds the specified spacing between each character,
     * similar to CSS letter-spacing property.
     *
     * @param spacing Additional spacing between characters in pixels. Can be negative.
     */
    @Override
    public void setLetterSpacing(double spacing) {
        this.letterSpacing = spacing;
        gc.setLetterSpacing(spacing);
    }

    /**
     * Gets the word spacing for text rendering.
     *
     * <p><strong>Implementation Status:</strong> This property is stored but not currently implemented
     * in either backend (AWT/JavaFX).
     *
     * @return Current word spacing value in pixels (default: 0.0)
     */
    @Override
    public double getWordSpacing() {
        return wordSpacing;
    }

    /**
     * Sets the word spacing for text rendering.
     *
     * <p><strong>Implementation Status:</strong> This property is stored but not currently implemented
     * in either backend.
     *
     * @param spacing Additional spacing between words in pixels. Can be negative.
     */
    @Override
    public void setWordSpacing(double spacing) {
        this.wordSpacing = spacing;
        gc.setWordSpacing(spacing);
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
        if (width <= 0) {
            throw new IllegalArgumentException("Width must be positive, got: " + width);
        }
        if (height <= 0) {
            throw new IllegalArgumentException("Height must be positive, got: " + height);
        }
        return gc.createImageData(width, height);
    }

    @Override
    public IImageData getImageData(int x, int y, int width, int height) {
        if (width <= 0) {
            throw new IllegalArgumentException("Width must be positive, got: " + width);
        }
        if (height <= 0) {
            throw new IllegalArgumentException("Height must be positive, got: " + height);
        }

        // Validate bounds are at least partially within canvas
        int canvasWidth = surface.getWidth();
        int canvasHeight = surface.getHeight();

        if (x >= canvasWidth || y >= canvasHeight || x + width <= 0 || y + height <= 0) {
            throw new IllegalArgumentException(
                "The requested region (x=" + x + ", y=" + y + ", width=" + width + ", height=" + height +
                ") is completely outside the canvas bounds (width=" + canvasWidth + ", height=" + canvasHeight + ")"
            );
        }

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
        return filter;
    }

    @Override
    public void setFilter(String filter) {
        if (filter == null || filter.trim().isEmpty()) {
            this.filter = "none";
        } else {
            this.filter = filter;
        }
    }

    @Override
    public void drawFocusIfNeeded(Object element) {
        // Draw focus ring around current path if element has focus
        // For now, implement a simplified version that checks if element has focus
        // and draws a focus ring using system colors
        if (element != null && hasElementFocus(element)) {
            // Save current state
            save();

            // Set focus ring style (typically a dashed line in system accent color)
            gc.setLineWidth(2.0);
            gc.setLineDash(new double[]{2, 2});
            gc.setStrokePaint(ColorParser.parse("#0078D4", backend)); // System accent color approximation

            // Stroke the current path
            gc.stroke();

            // Restore state
            restore();
        }
    }

    @Override
    public void drawFocusIfNeeded(IPath2D path, Object element) {
        // Draw focus ring around specified path if element has focus
        if (element != null && hasElementFocus(element) && path != null) {
            // Save current state
            save();

            // Set focus ring style
            gc.setLineWidth(2.0);
            gc.setLineDash(new double[]{2, 2});
            gc.setStrokePaint(ColorParser.parse("#0078D4", backend)); // System accent color approximation

            // Stroke the specified path
            stroke(path);

            // Restore state
            restore();
        }
    }

    /**
     * Check if an element has focus.
     * This is a simplified implementation - in a full implementation,
     * this would check the DOM focus state.
     */
    private boolean hasElementFocus(Object element) {
        // For Rhino objects, try to check if they have a 'focused' property
        if (element instanceof org.mozilla.javascript.ScriptableObject) {
            org.mozilla.javascript.ScriptableObject scriptable = (org.mozilla.javascript.ScriptableObject) element;
            Object focused = scriptable.get("focused", scriptable);
            if (focused != org.mozilla.javascript.Scriptable.NOT_FOUND) {
                return Boolean.TRUE.equals(focused);
            }
        }
        // Default: assume not focused
        return false;
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

        // Filter property
        private final String filter;

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

            // Save filter property
            this.filter = ctx.filter;
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

            // Restore filter property
            ctx.filter = this.filter;
        }
    }
}
