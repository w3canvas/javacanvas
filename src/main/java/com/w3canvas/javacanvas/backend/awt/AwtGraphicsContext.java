package com.w3canvas.javacanvas.backend.awt;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ByteLookupTable;
import java.awt.image.ColorConvertOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.awt.image.LookupOp;
import java.awt.image.RescaleOp;
import java.text.AttributedString;
import java.awt.font.TextAttribute;
import java.util.Map;
import java.util.HashMap;

import com.w3canvas.javacanvas.interfaces.*;

/**
 * AWT/Swing backend implementation of the graphics context.
 *
 * <p>This class implements {@link IGraphicsContext} using Java's AWT (Abstract Window Toolkit)
 * and Swing Graphics2D for rendering. It translates Canvas 2D API calls into corresponding
 * AWT graphics operations.
 *
 * <p>The AWT backend provides:
 * <ul>
 *   <li>Broad platform compatibility (works on all Java-supported platforms)</li>
 *   <li>Headless rendering support (useful for server-side image generation)</li>
 *   <li>Integration with existing AWT/Swing applications</li>
 *   <li>Support for advanced features like shadows, filters, and compositing</li>
 * </ul>
 *
 * <p><strong>Important implementation details:</strong>
 * <ul>
 *   <li>Paths are transformed during construction (moveTo, lineTo, etc.) rather than at draw time,
 *       preventing double-transformation issues</li>
 *   <li>Shadow effects are approximated using multiple drawing passes with varying opacity</li>
 *   <li>CSS filters are implemented using BufferedImageOp operations</li>
 *   <li>Rendering hints are configured differently for headless vs. GUI mode for optimal results</li>
 * </ul>
 *
 * @see IGraphicsContext
 * @see java.awt.Graphics2D
 * @since 1.0
 */
public class AwtGraphicsContext implements IGraphicsContext {

    private final Graphics2D g2d;
    private final AwtCanvasSurface surface;
    private java.awt.geom.Path2D.Double path;
    private double[] lastPoint = new double[2];
    private IPaint fillPaint;
    private IPaint strokePaint;

    // Shadow properties
    private double shadowBlur = 0;
    private String shadowColor = "rgba(0, 0, 0, 0)";
    private double shadowOffsetX = 0;
    private double shadowOffsetY = 0;

    // Image smoothing
    private boolean imageSmoothingEnabled = true;
    private String imageSmoothingQuality = "low";

    // Filter
    private String filter = "none";

    // Fill rule
    private String fillRule = "nonzero";

    // Text properties
    private String textAlign = "start";
    private String textBaseline = "alphabetic";
    private String direction = "ltr";  // Default direction for start/end handling
    private double letterSpacing = 0;

    // Shadow blur constants
    private static final float ALPHA_CHANNEL_MAX = 255.0f;
    private static final double BLUR_DIVISOR = 2.0;
    private static final int MAX_BLUR_STEPS = 10;
    private static final double GAUSSIAN_SIGMA_RATIO = 3.0; // radius / sigma ratio for Gaussian blur

    public AwtGraphicsContext(Graphics2D g2d, AwtCanvasSurface surface) {
        this.g2d = g2d;
        this.surface = surface;

        // Configure rendering hints for consistent behavior across environments
        // In headless mode, use more predictable rendering settings for test consistency
        boolean isHeadless = "true".equals(System.getProperty("java.awt.headless"));

        if (isHeadless) {
            // Headless mode: prioritize consistency over quality for pixel-perfect tests
            this.g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            this.g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            this.g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            this.g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            this.g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        } else {
            // GUI mode: prioritize visual quality
            this.g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            this.g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            this.g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            this.g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
            this.g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
            this.g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            this.g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        }

        this.path = new java.awt.geom.Path2D.Double();
        this.fillPaint = new AwtPaint(java.awt.Color.BLACK);
        this.strokePaint = new AwtPaint(java.awt.Color.BLACK);
    }

    // Transformations
    @Override
    public void scale(double x, double y) {
        g2d.scale(x, y);
    }

    @Override
    public void fillText(String text, double x, double y, double maxWidth) {
        // Adjust x-coordinate based on textAlign setting
        double adjustedX = adjustXForTextAlign(text, x);
        // Adjust y-coordinate based on textBaseline setting
        double adjustedY = adjustYForTextBaseline(y);

        java.awt.font.TextLayout tl = createTextLayout(text);
        if (tl == null) return;

        // Handle maxWidth by scaling text horizontally when it exceeds the limit
        if (maxWidth > 0) {
            double textWidth = tl.getAdvance();
            if (textWidth > maxWidth) {
                // Scale text to fit maxWidth
                double scale = maxWidth / textWidth;
                AffineTransform oldTransform = g2d.getTransform();
                AffineTransform scaleTransform = new AffineTransform(oldTransform);
                scaleTransform.translate(adjustedX, adjustedY);
                scaleTransform.scale(scale, 1.0); // Only scale X, not Y
                scaleTransform.translate(-adjustedX, -adjustedY);
                g2d.setTransform(scaleTransform);
                // Draw text
                tl.draw(g2d, (float)adjustedX, (float)adjustedY);
                g2d.setTransform(oldTransform); // Restore
                return; // Don't draw again
            }
        }
        // Normal drawing if maxWidth not exceeded or not specified
        tl.draw(g2d, (float)adjustedX, (float)adjustedY);
    }

    @Override
    public void strokeText(String text, double x, double y, double maxWidth) {
        // Adjust x-coordinate based on textAlign setting
        double adjustedX = adjustXForTextAlign(text, x);
        // Adjust y-coordinate based on textBaseline setting
        double adjustedY = adjustYForTextBaseline(y);

        // Create TextLayout using helper (handles direction/attributes)
        java.awt.font.TextLayout tl = createTextLayout(text);
        if (tl == null) return;

        // Handle maxWidth by scaling text horizontally when it exceeds the limit
        if (maxWidth > 0) {
            double textWidth = tl.getAdvance();
            if (textWidth > maxWidth) {
                // Scale text to fit maxWidth
                double scale = maxWidth / textWidth;
                AffineTransform oldTransform = g2d.getTransform();
                AffineTransform scaleTransform = new AffineTransform(oldTransform);
                scaleTransform.translate(adjustedX, adjustedY);
                scaleTransform.scale(scale, 1.0); // Only scale X, not Y
                scaleTransform.translate(-adjustedX, -adjustedY);
                g2d.setTransform(scaleTransform);

                // Draw text outline
                Shape shape = tl.getOutline(AffineTransform.getTranslateInstance(adjustedX, adjustedY));
                if (shouldApplyFilters()) {
                    // Need to apply current g2d transform (which includes scale)
                    Shape deviceShape = g2d.getTransform().createTransformedShape(shape);
                    AffineTransform saved = g2d.getTransform();
                    g2d.setTransform(new AffineTransform());
                    strokeShapeWithFilters(deviceShape);
                    g2d.setTransform(saved);
                } else {
                    g2d.draw(shape);
                }

                g2d.setTransform(oldTransform); // Restore
                return; // Don't draw again
            }
        }
        // Normal drawing if maxWidth not exceeded or not specified
        Shape shape = tl.getOutline(AffineTransform.getTranslateInstance(adjustedX, adjustedY));
        if (shouldApplyFilters()) {
            // Need to apply current g2d transform
            Shape deviceShape = g2d.getTransform().createTransformedShape(shape);
            AffineTransform saved = g2d.getTransform();
            g2d.setTransform(new AffineTransform());
            strokeShapeWithFilters(deviceShape);
            g2d.setTransform(saved);
        } else {
            g2d.draw(shape);
        }
    }

    @Override
    public void rotate(double theta) {
        g2d.rotate(theta);
    }

    @Override
    public void translate(double tx, double ty) {
        g2d.translate(tx, ty);
    }

    @Override
    public void transform(double m11, double m12, double m21, double m22, double dx, double dy) {
        g2d.transform(new AffineTransform(m11, m12, m21, m22, dx, dy));
    }

    @Override
    public void setTransform(double m11, double m12, double m21, double m22, double dx, double dy) {
        g2d.setTransform(new AffineTransform(m11, m12, m21, m22, dx, dy));
    }

    @Override
    public void setTransform(Object transform) {
        if (transform instanceof AffineTransform) {
            g2d.setTransform((AffineTransform) transform);
        }
    }

    @Override
    public void resetTransform() {
        g2d.setTransform(new AffineTransform());
    }

    @Override
    public Object getTransform() {
        return g2d.getTransform();
    }

    // Drawing properties
    @Override
    public ICanvasPattern createPattern(Object image, String repetition) {
        if (image instanceof BufferedImage) {
            return new AwtPattern((BufferedImage) image, repetition);
        }
        return null;
    }

    @Override
    public void setFillPaint(IPaint paint) {
        this.fillPaint = paint;
        if (paint instanceof AwtPaint) {
            g2d.setPaint(((AwtPaint) paint).getPaint());
        } else if (paint instanceof AwtLinearGradient) {
            g2d.setPaint(((AwtLinearGradient) paint).getPaint());
        } else if (paint instanceof AwtRadialGradient) {
            g2d.setPaint(((AwtRadialGradient) paint).getPaint());
        } else if (paint instanceof AwtConicGradient) {
            g2d.setPaint(((AwtConicGradient) paint).getPaint());
        } else if (paint instanceof AwtPattern) {
            g2d.setPaint(((AwtPattern) paint).getPaint());
        }
    }

    @Override
    public void setStrokePaint(IPaint paint) {
        this.strokePaint = paint;
        if (paint instanceof AwtPaint) {
            g2d.setPaint(((AwtPaint) paint).getPaint());
        } else if (paint instanceof AwtLinearGradient) {
            g2d.setPaint(((AwtLinearGradient) paint).getPaint());
        } else if (paint instanceof AwtRadialGradient) {
            g2d.setPaint(((AwtRadialGradient) paint).getPaint());
        } else if (paint instanceof AwtConicGradient) {
            g2d.setPaint(((AwtConicGradient) paint).getPaint());
        } else if (paint instanceof AwtPattern) {
            g2d.setPaint(((AwtPattern) paint).getPaint());
        }
    }

    private float lineWidth = 1.0f;
    private int lineCap = BasicStroke.CAP_BUTT;
    private int lineJoin = BasicStroke.JOIN_MITER;
    private float miterLimit = 10.0f;
    private float[] lineDash = null;
    private float lineDashOffset = 0.0f;

    private void updateStroke() {
        if (lineDash != null && lineDash.length > 0) {
            g2d.setStroke(new BasicStroke(lineWidth, lineCap, lineJoin, miterLimit, lineDash, lineDashOffset));
        } else {
            g2d.setStroke(new BasicStroke(lineWidth, lineCap, lineJoin, miterLimit));
        }
    }

    @Override
    public void setLineWidth(double width) {
        this.lineWidth = (float) width;
        updateStroke();
    }

    @Override
    public void setLineCap(String cap) {
        if ("round".equals(cap)) this.lineCap = BasicStroke.CAP_ROUND;
        else if ("square".equals(cap)) this.lineCap = BasicStroke.CAP_SQUARE;
        else this.lineCap = BasicStroke.CAP_BUTT;
        updateStroke();
    }

    @Override
    public void setLineJoin(String join) {
        if ("round".equals(join)) this.lineJoin = BasicStroke.JOIN_ROUND;
        else if ("bevel".equals(join)) this.lineJoin = BasicStroke.JOIN_BEVEL;
        else this.lineJoin = BasicStroke.JOIN_MITER;
        updateStroke();
    }

    @Override
    public void setMiterLimit(double limit) {
        this.miterLimit = (float) limit;
        updateStroke();
    }

    @Override
    public void setLineDash(double[] dash) {
        if (dash == null) {
            this.lineDash = null;
        } else {
            this.lineDash = new float[dash.length];
            for (int i = 0; i < dash.length; i++) {
                this.lineDash[i] = (float) dash[i];
            }
        }
        updateStroke();
    }

    @Override
    public void setLineDashOffset(double offset) {
        this.lineDashOffset = (float) offset;
        updateStroke();
    }

    @Override
    public void setComposite(IComposite comp) {
        if (comp instanceof AwtComposite) {
            g2d.setComposite(((AwtComposite) comp).getComposite());
        }
    }

    @Override
    public void setGlobalAlpha(double alpha) {
        // In AWT, alpha is part of the AlphaComposite, which is handled by setComposite.
        // This method is a no-op for the AWT backend.
    }

    @Override
    public void setFont(IFont font) {
        if (font instanceof AwtFont) {
            g2d.setFont(((AwtFont) font).getFont());
            updateFontWithAttributes();
        }
    }

    @Override
    public void setDirection(String direction) {
        this.direction = direction;
    }

    @Override
    public void setLetterSpacing(double spacing) {
        this.letterSpacing = spacing;
        updateFontWithAttributes();
    }

    @Override
    public void setWordSpacing(double spacing) {
        // Not supported in AWT TextLayout standard attributes
    }

    private void updateFontWithAttributes() {
        Font f = g2d.getFont();
        if (f == null) return;

        Map<TextAttribute, Object> attributes = new HashMap<>();

        // Apply tracking (letter spacing)
        if (letterSpacing != 0) {
            float size = f.getSize2D();
            if (size > 0) {
                double tracking = letterSpacing / size;
                attributes.put(TextAttribute.TRACKING, tracking);
            }
        } else {
            attributes.put(TextAttribute.TRACKING, 0.0);
        }

        g2d.setFont(f.deriveFont(attributes));
    }

    private java.awt.font.TextLayout createTextLayout(String text) {
        if (text == null || text.isEmpty()) return null;

        java.awt.font.FontRenderContext frc = g2d.getFontRenderContext();
        Font font = g2d.getFont();

        AttributedString as = new AttributedString(text);
        as.addAttribute(TextAttribute.FONT, font);

        if ("rtl".equals(direction)) {
            as.addAttribute(TextAttribute.RUN_DIRECTION, TextAttribute.RUN_DIRECTION_RTL);
        } else if ("ltr".equals(direction)) {
            as.addAttribute(TextAttribute.RUN_DIRECTION, TextAttribute.RUN_DIRECTION_LTR);
        }

        return new java.awt.font.TextLayout(as.getIterator(), frc);
    }

    /**
     * Sets the text alignment for subsequent text rendering operations.
     *
     * <p>This implementation adjusts the x-coordinate before text rendering to match the specified
     * alignment mode. Java AWT's {@link Graphics2D#drawString(String, float, float)} method treats
     * the x-coordinate as the left edge of the text by default.
     *
     * <p><strong>Alignment modes:</strong>
     * <ul>
     *   <li>"left" - align text left (default AWT behavior)</li>
     *   <li>"right" - align text right (x - textWidth)</li>
     *   <li>"center" - center text (x - textWidth/2)</li>
     *   <li>"start" - depends on direction: left for ltr, right for rtl (currently assumes ltr)</li>
     *   <li>"end" - depends on direction: right for ltr, left for rtl (currently assumes ltr)</li>
     * </ul>
     *
     * <p><strong>Valid values:</strong> "left", "right", "center", "start", "end" (default: "start")
     *
     * @param textAlign The text alignment mode to use for rendering text
     * @see java.awt.FontMetrics
     * @see #adjustXForTextAlign(String, double)
     */
    @Override
    public void setTextAlign(String textAlign) {
        this.textAlign = textAlign != null ? textAlign : "start";
    }

    /**
     * Sets the text baseline alignment for subsequent text rendering operations.
     *
     * <p>This implementation adjusts the y-coordinate before text rendering to match the specified
     * baseline mode. Java AWT's {@link Graphics2D#drawString(String, float, float)} method treats
     * the y-coordinate as the alphabetic baseline by default.
     *
     * <p><strong>Baseline modes:</strong>
     * <ul>
     *   <li>"alphabetic" - baseline for normal text (default, no adjustment)</li>
     *   <li>"top" - top of the em square (y + ascent)</li>
     *   <li>"hanging" - hanging baseline, typically 80% of ascent (y + ascent * 0.8)</li>
     *   <li>"middle" - middle of the em square (y + (ascent - descent) / 2)</li>
     *   <li>"ideographic" - ideographic baseline, at the bottom (y - descent)</li>
     *   <li>"bottom" - bottom of the em square (y - descent)</li>
     * </ul>
     *
     * <p><strong>Valid values:</strong> "top", "hanging", "middle", "alphabetic", "ideographic", "bottom"
     * (default: "alphabetic")
     *
     * @param textBaseline The text baseline mode to use for rendering text
     * @see java.awt.FontMetrics
     * @see #adjustYForTextBaseline(double)
     */
    @Override
    public void setTextBaseline(String textBaseline) {
        this.textBaseline = textBaseline != null ? textBaseline : "alphabetic";
    }

    /**
     * Adjusts the y-coordinate for text rendering based on the current textBaseline setting.
     * Graphics2D treats y as the alphabetic baseline by default, so we adjust for other baseline modes.
     *
     * @param y The original y-coordinate
     * @return The adjusted y-coordinate based on textBaseline
     */
    private double adjustYForTextBaseline(double y) {
        if ("alphabetic".equals(textBaseline)) {
            return y; // Default AWT behavior - no adjustment needed
        }

        java.awt.FontMetrics fm = g2d.getFontMetrics();
        int ascent = fm.getAscent();
        int descent = fm.getDescent();

        if ("top".equals(textBaseline)) {
            return y + ascent;
        } else if ("hanging".equals(textBaseline)) {
            return y + (ascent * 0.8);
        } else if ("middle".equals(textBaseline)) {
            return y + ((ascent - descent) / 2.0);
        } else if ("ideographic".equals(textBaseline) || "bottom".equals(textBaseline)) {
            return y - descent;
        }

        return y; // Default to alphabetic if unknown value
    }

    /**
     * Adjusts the x-coordinate for text rendering based on the current textAlign setting.
     * Graphics2D treats x as the left edge of the text by default, so we adjust for other alignment modes.
     *
     * @param text The text string to be rendered
     * @param x The original x-coordinate
     * @return The adjusted x-coordinate based on textAlign
     */
    private double adjustXForTextAlign(String text, double x) {
        if ("left".equals(textAlign)) {
            return x; // Default AWT behavior - no adjustment needed
        }

        // For "start" and "end", we need to consider the direction property
        // Currently defaulting to ltr behavior since direction is not yet passed from context
        if ("start".equals(textAlign)) {
            // In ltr mode, "start" means left; in rtl mode, "start" means right
            if ("ltr".equals(direction)) {
                return x; // No adjustment for ltr start
            } else {
                // rtl: start means right-aligned
                int textWidth = g2d.getFontMetrics().stringWidth(text);
                return x - textWidth;
            }
        }

        if ("end".equals(textAlign)) {
            // In ltr mode, "end" means right; in rtl mode, "end" means left
            if ("ltr".equals(direction)) {
                int textWidth = g2d.getFontMetrics().stringWidth(text);
                return x - textWidth;
            } else {
                // rtl: end means left-aligned
                return x; // No adjustment for rtl end
            }
        }

        // Measure text width for right and center alignment
        int textWidth = g2d.getFontMetrics().stringWidth(text);

        if ("right".equals(textAlign)) {
            return x - textWidth;
        } else if ("center".equals(textAlign)) {
            return x - (textWidth / 2.0);
        }

        return x; // Default to left if unknown value
    }

    // Drawing operations
    @Override
    public void clearRect(double x, double y, double w, double h) {
        Composite old = g2d.getComposite();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR));
        g2d.fillRect((int) x, (int) y, (int) w, (int) h);
        g2d.setComposite(old);
    }

    @Override
    public void fillRectDirect(double x, double y, double w, double h) {
        // Use AWT's native fillRect method
        g2d.fillRect((int) x, (int) y, (int) w, (int) h);
    }

    @Override
    public void draw(IShape shape) {
        if (shape instanceof AwtShape) {
            g2d.draw(((AwtShape) shape).getShape());
        }
    }

    @Override
    public void fill(IShape shape) {
        if (shape instanceof AwtShape) {
            g2d.fill(((AwtShape) shape).getShape());
        }
    }

    @Override
    public void drawImage(Object img, int x, int y) {
        if (img instanceof BufferedImage) {
            BufferedImage image = (BufferedImage) img;

            // Apply CSS filters if active
            if (shouldApplyFilters()) {
                image = applyFiltersToImage(image);
            }

            g2d.drawImage(image, x, y, null);
        }
    }

    @Override
    public void drawImage(int[] pixels, int x, int y, int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        image.setRGB(0, 0, width, height, pixels, 0, width);

        // Apply CSS filters if active
        if (shouldApplyFilters()) {
            image = applyFiltersToImage(image);
        }

        g2d.drawImage(image, x, y, null);
    }

    /**
     * Draws an image scaled to the specified width and height.
     * This is the 5-parameter Canvas 2D drawImage variant for scaling.
     *
     * @param img the image to draw
     * @param x the x-coordinate of the destination position
     * @param y the y-coordinate of the destination position
     * @param w the width to scale the image to
     * @param h the height to scale the image to
     */
    public void drawImage(Object img, int x, int y, int w, int h) {
        BufferedImage buffImg = toBufferedImage(img);
        if (buffImg == null) {
            return;
        }

        // Apply CSS filters if active
        if (shouldApplyFilters()) {
            buffImg = applyFiltersToImage(buffImg);
        }

        g2d.drawImage(buffImg, x, y, w, h, null);
    }

    @Override
    public void drawImage(Object img, int sx, int sy, int sw, int sh, int dx, int dy, int dw, int dh) {
        BufferedImage buffImg = toBufferedImage(img);
        if (buffImg == null) {
            return;
        }

        // Apply CSS filters if active
        if (shouldApplyFilters()) {
            buffImg = applyFiltersToImage(buffImg);
        }

        g2d.drawImage(buffImg, dx, dy, dx + dw, dy + dh, sx, sy, sx + sw, sy + sh, null);
    }

    @Override
    public void drawString(String str, int x, int y) {
        g2d.drawString(str, x, y);
    }

    @Override
    public ITextMetrics measureText(String text) {
        return new AwtTextMetrics(text, g2d.getFont(), g2d);
    }

    @Override
    public IImageData createImageData(int width, int height) {
        int[] data = new int[width * height];
        return new com.w3canvas.javacanvas.core.ImageData(width, height, new com.w3canvas.javacanvas.core.CanvasPixelArray(data, width, height));
    }

    @Override
    public IImageData getImageData(int x, int y, int width, int height) {
        BufferedImage image = (BufferedImage) surface.getNativeImage();
        int[] pixels = new int[width * height];
        image.getRGB(x, y, width, height, pixels, 0, width);
        return new com.w3canvas.javacanvas.core.ImageData(width, height, new com.w3canvas.javacanvas.core.CanvasPixelArray(pixels, width, height));
    }

    // Clipping
    @Override
    public void clip() {
        g2d.clip(this.path);
    }

    // Path methods
    @Override
    public void beginPath() {
        path = new java.awt.geom.Path2D.Double();
    }

    @Override
    public void closePath() {
        path.closePath();
    }

    @Override
    public void moveTo(double x, double y) {
        lastPoint[0] = x;
        lastPoint[1] = y;
        Point2D p = new Point2D.Double(x, y);
        g2d.getTransform().transform(p, p);
        path.moveTo(p.getX(), p.getY());
    }

    @Override
    public void lineTo(double x, double y) {
        lastPoint[0] = x;
        lastPoint[1] = y;
        Point2D p = new Point2D.Double(x, y);
        g2d.getTransform().transform(p, p);
        path.lineTo(p.getX(), p.getY());
    }

    @Override
    public void quadraticCurveTo(double cpx, double cpy, double x, double y) {
        lastPoint[0] = x;
        lastPoint[1] = y;
        double[] xy = { cpx, cpy, x, y };
        g2d.getTransform().transform(xy, 0, xy, 0, 2);
        path.quadTo(xy[0], xy[1], xy[2], xy[3]);
    }

    @Override
    public void bezierCurveTo(double cp1x, double cp1y, double cp2x, double cp2y, double x, double y) {
        lastPoint[0] = x;
        lastPoint[1] = y;
        float[] xy = { (float)cp1x, (float)cp1y, (float)cp2x, (float)cp2y, (float)x, (float)y };
        g2d.getTransform().transform(xy, 0, xy, 0, 3);
        path.curveTo(xy[0], xy[1], xy[2], xy[3], xy[4], xy[5]);
    }

    @Override
    public void arcTo(double x1, double y1, double x2, double y2, double radius) {
        Point2D p0 = path.getCurrentPoint();
        if (p0 == null) {
            return;
        }

        double x0 = p0.getX();
        double y0 = p0.getY();

        if (radius == 0 || (x0 == x1 && y0 == y1) || (x1 == x2 && y1 == y2)) {
            lineTo(x1, y1);
            return;
        }

        double dx01 = x1 - x0;
        double dy01 = y1 - y0;
        double len01 = Math.sqrt(dx01 * dx01 + dy01 * dy01);
        dx01 /= len01;
        dy01 /= len01;

        double dx12 = x2 - x1;
        double dy12 = y2 - y1;
        double len12 = Math.sqrt(dx12 * dx12 + dy12 * dy12);
        dx12 /= len12;
        dy12 /= len12;

        double angle = Math.acos(dx01 * dx12 + dy01 * dy12);

        if (Math.abs(angle) < 1e-6 || Math.abs(angle - Math.PI) < 1e-6) {
            lineTo(x1, y1);
            return;
        }

        double tangent = radius / Math.tan(angle / 2.0);

        double t1x = x1 - tangent * dx01;
        double t1y = y1 - tangent * dy01;
        double t2x = x1 + tangent * dx12;
        double t2y = y1 + tangent * dy12;

        lineTo(t1x, t1y);

        double cross_product = dx01 * dy12 - dy01 * dx12;
        double normal_x, normal_y;

        if (cross_product < 0) { // Clockwise
            normal_x = dy01;
            normal_y = -dx01;
        } else { // Counter-clockwise
            normal_x = -dy01;
            normal_y = dx01;
        }

        double cx = t1x + normal_x * radius;
        double cy = t1y + normal_y * radius;

        double startAngle = Math.toDegrees(Math.atan2(t1y - cy, t1x - cx));
        double endAngle = Math.toDegrees(Math.atan2(t2y - cy, t2x - cx));
        double sweepAngle = endAngle - startAngle;

        if (cross_product < 0) { // Clockwise
            if (sweepAngle > 0) {
                sweepAngle -= 360;
            }
        } else { // Counter-clockwise
            if (sweepAngle < 0) {
                sweepAngle += 360;
            }
        }

        path.append(new Arc2D.Double(cx - radius, cy - radius, 2 * radius, 2 * radius, startAngle, sweepAngle, Arc2D.OPEN), true);

        lastPoint[0] = t2x;
        lastPoint[1] = t2y;
    }

    @Override
    public void rect(double x, double y, double w, double h) {
        // Transform the corner coordinates
        Point2D p1 = g2d.getTransform().transform(new Point2D.Double(x, y), null);
        Point2D p2 = g2d.getTransform().transform(new Point2D.Double(x + w, y), null);
        Point2D p3 = g2d.getTransform().transform(new Point2D.Double(x + w, y + h), null);
        Point2D p4 = g2d.getTransform().transform(new Point2D.Double(x, y + h), null);

        // Add as explicit path commands (creates clean subpath)
        path.moveTo(p1.getX(), p1.getY());
        path.lineTo(p2.getX(), p2.getY());
        path.lineTo(p3.getX(), p3.getY());
        path.lineTo(p4.getX(), p4.getY());
        path.closePath();
    }

    @Override
    public void roundRect(double x, double y, double w, double h, Object radii) {
        double[] cornerRadii = parseRoundRectRadii(radii);

        // Extract individual corner radii (CSS order: TL, TR, BR, BL)
        double tlRadius = cornerRadii[0];
        double trRadius = cornerRadii[1];
        double brRadius = cornerRadii[2];
        double blRadius = cornerRadii[3];

        // Clamp radii to not exceed half of width or height
        double maxRadius = Math.min(Math.abs(w) / 2, Math.abs(h) / 2);
        tlRadius = Math.min(tlRadius, maxRadius);
        trRadius = Math.min(trRadius, maxRadius);
        brRadius = Math.min(brRadius, maxRadius);
        blRadius = Math.min(blRadius, maxRadius);

        // If all radii are zero, just draw a regular rect
        if (tlRadius == 0 && trRadius == 0 && brRadius == 0 && blRadius == 0) {
            rect(x, y, w, h);
            return;
        }

        // Build the rounded rectangle path manually
        java.awt.geom.Path2D.Double roundedRect = new java.awt.geom.Path2D.Double();

        // Start at top-left corner (after the radius)
        roundedRect.moveTo(x + tlRadius, y);

        // Top edge and top-right corner
        roundedRect.lineTo(x + w - trRadius, y);
        if (trRadius > 0) {
            roundedRect.quadTo(x + w, y, x + w, y + trRadius);
        }

        // Right edge and bottom-right corner
        roundedRect.lineTo(x + w, y + h - brRadius);
        if (brRadius > 0) {
            roundedRect.quadTo(x + w, y + h, x + w - brRadius, y + h);
        }

        // Bottom edge and bottom-left corner
        roundedRect.lineTo(x + blRadius, y + h);
        if (blRadius > 0) {
            roundedRect.quadTo(x, y + h, x, y + h - blRadius);
        }

        // Left edge and top-left corner
        roundedRect.lineTo(x, y + tlRadius);
        if (tlRadius > 0) {
            roundedRect.quadTo(x, y, x + tlRadius, y);
        }

        roundedRect.closePath();

        // Transform and append to path
        path.append(g2d.getTransform().createTransformedShape(roundedRect), true);
    }

    /**
     * Parse roundRect radii parameter according to Canvas 2D spec.
     * Returns array of 4 corner radii: [top-left, top-right, bottom-right, bottom-left]
     */
    private double[] parseRoundRectRadii(Object radii) {
        if (radii == null) {
            return new double[]{0, 0, 0, 0};
        }

        // Handle single number
        if (radii instanceof Number) {
            double r = ((Number) radii).doubleValue();
            return new double[]{r, r, r, r};
        }

        // Handle arrays (both Java arrays and Rhino NativeArray)
        double[] values = null;

        if (radii instanceof double[]) {
            values = (double[]) radii;
        } else if (radii instanceof Object[]) {
            Object[] arr = (Object[]) radii;
            values = new double[arr.length];
            for (int i = 0; i < arr.length; i++) {
                if (arr[i] instanceof Number) {
                    values[i] = ((Number) arr[i]).doubleValue();
                }
            }
        } else if (radii instanceof org.mozilla.javascript.NativeArray) {
            org.mozilla.javascript.NativeArray arr = (org.mozilla.javascript.NativeArray) radii;
            int len = (int) arr.getLength();
            values = new double[len];
            for (int i = 0; i < len; i++) {
                Object val = arr.get(i);
                if (val instanceof Number) {
                    values[i] = ((Number) val).doubleValue();
                }
            }
        }

        if (values != null && values.length > 0) {
            // CSS-style corner radius specification
            switch (values.length) {
                case 1:
                    // All corners
                    return new double[]{values[0], values[0], values[0], values[0]};
                case 2:
                    // [top-left & bottom-right, top-right & bottom-left]
                    return new double[]{values[0], values[1], values[0], values[1]};
                case 3:
                    // [top-left, top-right & bottom-left, bottom-right]
                    return new double[]{values[0], values[1], values[2], values[1]};
                case 4:
                default:
                    // [top-left, top-right, bottom-right, bottom-left]
                    return new double[]{values[0], values[1], values[2], values[3]};
            }
        }

        // Default: no rounding
        return new double[]{0, 0, 0, 0};
    }

    @Override
    public void arc(double x, double y, double radius, double startAngle, double endAngle, boolean counterclockwise) {
        double ang = endAngle - startAngle;
        if (counterclockwise) {
            if (ang < 0) ang += 2 * Math.PI;
        } else {
            if (ang > 0) ang -= 2 * Math.PI;
        }
        path.append(g2d.getTransform().createTransformedShape(
                new Arc2D.Double(x - radius, y - radius, 2 * radius, 2 * radius,
                        Math.toDegrees(startAngle), Math.toDegrees(ang), Arc2D.OPEN)), true);
        double endAngleRad = startAngle + ang;
        lastPoint[0] = x + radius * Math.cos(endAngleRad);
        lastPoint[1] = y + radius * Math.sin(endAngleRad);
    }

    @Override
    public void ellipse(double x, double y, double radiusX, double radiusY, double rotation, double startAngle, double endAngle, boolean counterclockwise) {
        double ang = endAngle - startAngle;
        if (counterclockwise) {
            if (ang < 0) {
                ang += 2 * Math.PI;
            }
        } else {
            if (ang > 0) {
                ang -= 2 * Math.PI;
            }
        }

        AffineTransform tx = AffineTransform.getRotateInstance(rotation, x, y);
        Shape ellipse = new Arc2D.Double(x - radiusX, y - radiusY, radiusX * 2, radiusY * 2,
                Math.toDegrees(startAngle), Math.toDegrees(ang), Arc2D.OPEN);
        path.append(tx.createTransformedShape(ellipse), true);
    }

    @Override
    public void fill() {
        fill(this.fillRule);
    }

    @Override
    public void fill(String fillRule) {
        // Set the winding rule on the path based on the fillRule
        if ("evenodd".equals(fillRule)) {
            this.path.setWindingRule(java.awt.geom.Path2D.WIND_EVEN_ODD);
        } else {
            this.path.setWindingRule(java.awt.geom.Path2D.WIND_NON_ZERO);
        }

        // Check if we need to apply filters
        if (shouldApplyFilters()) {
            fillWithFilters();
        } else {
            // Normal fill without filters
            // IMPORTANT: The path has already been built with transformed coordinates
            // (see moveTo, lineTo, rect, etc. which apply g2d.getTransform() manually).
            // We must temporarily reset the transform before filling to avoid double transformation.
            AffineTransform savedTransform = g2d.getTransform();
            g2d.setTransform(new AffineTransform());  // Identity transform

            // Apply shadow first
            applyShadow(g2d, this.path, true);
            // Then draw the actual shape
            g2d.fill(this.path);

            // Restore the transform
            g2d.setTransform(savedTransform);
        }
    }

    @Override
    public void setFillRule(String fillRule) {
        this.fillRule = fillRule != null ? fillRule : "nonzero";
    }


    /**
     * Perform fill operation with filters applied.
     * This method renders the fill operation to an off-screen buffer, applies CSS filters,
     * and then composites the filtered result back to the main canvas.
     */
    private void fillWithFilters() {
        // Calculate bounds of the path (already in device coordinates)
        java.awt.Rectangle bounds = this.path.getBounds();

        // Expand bounds to account for filter effects (e.g., blur)
        int expansion = calculateFilterExpansion();
        bounds.x -= expansion;
        bounds.y -= expansion;
        bounds.width += expansion * 2;
        bounds.height += expansion * 2;

        // Ensure bounds are within canvas and non-empty
        BufferedImage canvasImage = (BufferedImage) surface.getNativeImage();
        bounds = bounds.intersection(new java.awt.Rectangle(0, 0, canvasImage.getWidth(), canvasImage.getHeight()));

        if (bounds.width <= 0 || bounds.height <= 0) {
            return; // Nothing to render
        }

        // Create off-screen buffer with alpha channel
        BufferedImage offscreen = new BufferedImage(bounds.width, bounds.height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D offscreenG2d = offscreen.createGraphics();

        try {
            // Copy rendering hints from main graphics context
            offscreenG2d.setRenderingHints(g2d.getRenderingHints());

            // Translate graphics context to account for bounds offset
            offscreenG2d.translate(-bounds.x, -bounds.y);

            // Copy relevant graphics state (composite, paint, stroke)
            offscreenG2d.setComposite(g2d.getComposite());
            offscreenG2d.setPaint(g2d.getPaint());
            offscreenG2d.setStroke(g2d.getStroke());

            // Apply shadow on the offscreen buffer
            applyShadow(offscreenG2d, this.path, true);

            // Perform fill on offscreen buffer
            offscreenG2d.fill(this.path);

            // Apply filters to the rendered result
            BufferedImage filtered = applyFiltersToImage(offscreen);

            // Composite filtered result back to main canvas
            AffineTransform savedTransform = g2d.getTransform();
            g2d.setTransform(new AffineTransform()); // Identity transform
            g2d.drawImage(filtered, bounds.x, bounds.y, null);
            g2d.setTransform(savedTransform);

        } finally {
            // Always dispose the offscreen graphics to release resources
            offscreenG2d.dispose();
        }
    }

    /**
     * Calculate the expansion needed for filter effects.
     * This is used to expand the rendering bounds to account for effects like blur
     * that extend beyond the original shape boundaries.
     *
     * @return the number of pixels to expand bounds in each direction
     */
    private int calculateFilterExpansion() {
        if (filter == null || "none".equals(filter)) {
            return 0;
        }

        int maxExpansion = 0;

        // Parse filter string to find blur radius (simplified parsing)
        // A blur filter typically needs 3x the radius for proper rendering
        if (filter.contains("blur(")) {
            try {
                int start = filter.indexOf("blur(") + 5;
                int end = filter.indexOf("px", start);
                if (end > start) {
                    String radiusStr = filter.substring(start, end).trim();
                    double radius = Double.parseDouble(radiusStr);
                    maxExpansion = Math.max(maxExpansion, (int) Math.ceil(radius * 3));
                }
            } catch (Exception e) {
                // If parsing fails, use default expansion for blur
                maxExpansion = Math.max(maxExpansion, 20);
            }
        }

        // For other filters, use a smaller expansion to account for edge effects
        if (maxExpansion == 0 && shouldApplyFilters()) {
            maxExpansion = 5;
        }

        return maxExpansion;
    }

    /**
     * Perform stroke operation with filters applied.
     */
    private void strokeShapeWithFilters(Shape shape) {
        // Calculate bounds of the shape
        Rectangle2D bounds2D = shape.getBounds2D();
        java.awt.Rectangle bounds = bounds2D.getBounds();

        // Expand bounds for line width
        int strokeExpansion = (int) Math.ceil(this.lineWidth / 2.0);
        // grow() expands in all directions (subtracts from x,y and adds to w,h)
        bounds.grow(strokeExpansion, strokeExpansion);

        // Expand bounds to account for filter effects (e.g., blur)
        int expansion = calculateFilterExpansion();
        bounds.grow(expansion, expansion);

        // Ensure bounds are within canvas and non-empty
        BufferedImage canvasImage = (BufferedImage) surface.getNativeImage();
        bounds = bounds.intersection(new java.awt.Rectangle(0, 0, canvasImage.getWidth(), canvasImage.getHeight()));

        if (bounds.width <= 0 || bounds.height <= 0) {
            return; // Nothing to render
        }

        // Create off-screen buffer with alpha channel
        BufferedImage offscreen = new BufferedImage(bounds.width, bounds.height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D offscreenG2d = offscreen.createGraphics();

        try {
            // Copy rendering hints
            offscreenG2d.setRenderingHints(g2d.getRenderingHints());

            // Translate graphics context to account for bounds offset
            offscreenG2d.translate(-bounds.x, -bounds.y);

            // Copy graphics state
            offscreenG2d.setComposite(g2d.getComposite());
            offscreenG2d.setPaint(g2d.getPaint());
            offscreenG2d.setStroke(g2d.getStroke());

            // Apply shadow on the offscreen buffer
            applyShadow(offscreenG2d, shape, false);

            // Perform stroke on offscreen buffer
            offscreenG2d.draw(shape);

            // Apply filters
            BufferedImage filtered = applyFiltersToImage(offscreen);

            // Composite filtered result back to main canvas
            // Assumes g2d is currently Identity transform (handled by caller)
            g2d.drawImage(filtered, bounds.x, bounds.y, null);

        } finally {
            offscreenG2d.dispose();
        }
    }

    @Override
    public void stroke() {
        // IMPORTANT: The path has already been built with transformed coordinates
        // (see moveTo, lineTo, rect, etc. which apply g2d.getTransform() manually).
        // We must temporarily reset the transform before stroking to avoid double transformation.
        AffineTransform savedTransform = g2d.getTransform();
        g2d.setTransform(new AffineTransform());  // Identity transform

        if (shouldApplyFilters()) {
            strokeShapeWithFilters(this.path);
        } else {
            // Apply shadow first
            applyShadow(g2d, this.path, false);
            // Then draw the actual shape
            g2d.draw(this.path);
        }

        // Restore the transform
        g2d.setTransform(savedTransform);
    }

    @Override
    public IShape getPath() {
        return new AwtShape(path);
    }

    @Override
    public void setPath(IShape shape) {
        if (shape instanceof AwtShape) {
            java.awt.Shape nativeShape = ((AwtShape) shape).getShape();
            if (nativeShape instanceof java.awt.geom.Path2D.Double) {
                this.path = (java.awt.geom.Path2D.Double) nativeShape;
            } else {
                // Create a new Path2D from the shape
                this.path = new java.awt.geom.Path2D.Double(nativeShape);
            }
        }
    }

    @Override
    public boolean isPointInPath(double x, double y) {
        return path.contains(x, y);
    }

    @Override
    public boolean isPointInStroke(double x, double y) {
        return g2d.getStroke().createStrokedShape(path).contains(x, y);
    }

    @Override
    public double[] getLastPoint() {
        return lastPoint;
    }

    // Shadow property setters
    @Override
    public void setShadowBlur(double blur) {
        this.shadowBlur = Math.max(0, blur);
    }

    @Override
    public void setShadowColor(String color) {
        this.shadowColor = color != null ? color : "rgba(0, 0, 0, 0)";
    }

    @Override
    public void setShadowOffsetX(double offsetX) {
        this.shadowOffsetX = offsetX;
    }

    @Override
    public void setShadowOffsetY(double offsetY) {
        this.shadowOffsetY = offsetY;
    }

    // Image smoothing setters
    @Override
    public void setImageSmoothingEnabled(boolean enabled) {
        this.imageSmoothingEnabled = enabled;
        updateImageSmoothingHints();
    }

    @Override
    public void setImageSmoothingQuality(String quality) {
        this.imageSmoothingQuality = quality;
        updateImageSmoothingHints();
    }

    private void updateImageSmoothingHints() {
        if (!imageSmoothingEnabled) {
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        } else {
            if ("low".equals(imageSmoothingQuality)) {
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            } else if ("medium".equals(imageSmoothingQuality) || "high".equals(imageSmoothingQuality)) {
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            }
        }
    }

    // Helper method to apply shadow effect
    private void applyShadow(Graphics2D g, Shape shape, boolean isFill) {
        // Check if shadow is active (non-zero offset or blur, and non-transparent color)
        boolean hasShadow = (shadowBlur > 0 || shadowOffsetX != 0 || shadowOffsetY != 0)
                          && shadowColor != null && !shadowColor.equals("rgba(0, 0, 0, 0)");

        if (!hasShadow) {
            return;
        }

        // Parse shadow color
        java.awt.Color shadowCol = parseColor(shadowColor);
        if (shadowCol.getAlpha() == 0) {
            return; // Fully transparent shadow
        }

        // Save current state
        Paint oldPaint = g.getPaint();
        Composite oldComposite = g.getComposite();

        // Create transformed shape for shadow
        AffineTransform shadowTransform = AffineTransform.getTranslateInstance(shadowOffsetX, shadowOffsetY);
        Shape shadowShape = shadowTransform.createTransformedShape(shape);

        // Apply shadow with Gaussian blur using ConvolveOp
        if (shadowBlur > 0) {
            // Calculate bounds for the shadow with blur expansion
            Rectangle2D bounds = shadowShape.getBounds2D();
            int blurRadius = (int) Math.ceil(shadowBlur / GAUSSIAN_SIGMA_RATIO);
            int expansion = blurRadius * 3; // 3x radius for proper Gaussian coverage

            int x = (int) Math.floor(bounds.getX()) - expansion;
            int y = (int) Math.floor(bounds.getY()) - expansion;
            int width = (int) Math.ceil(bounds.getWidth()) + expansion * 2;
            int height = (int) Math.ceil(bounds.getHeight()) + expansion * 2;

            // Ensure bounds are valid
            if (width <= 0 || height <= 0) {
                return;
            }

            // Create off-screen buffer for shadow
            BufferedImage shadowImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D shadowG2d = shadowImage.createGraphics();

            try {
                // Configure rendering hints
                shadowG2d.setRenderingHints(g.getRenderingHints());

                // Translate to account for bounds offset
                shadowG2d.translate(-x, -y);

                // Render shadow shape
                shadowG2d.setPaint(new java.awt.Color(shadowCol.getRed(), shadowCol.getGreen(),
                                                       shadowCol.getBlue(), shadowCol.getAlpha()));
                if (isFill) {
                    shadowG2d.fill(shadowShape);
                } else {
                    shadowG2d.setStroke(g.getStroke());
                    shadowG2d.draw(shadowShape);
                }

                shadowG2d.dispose();

                // Apply Gaussian blur using separable convolution
                if (blurRadius > 0) {
                    float[] kernel = createGaussianKernelForShadow(blurRadius);

                    // Horizontal blur
                    ConvolveOp hBlur = new ConvolveOp(
                        new Kernel(kernel.length, 1, kernel),
                        ConvolveOp.EDGE_NO_OP,
                        null
                    );
                    shadowImage = hBlur.filter(shadowImage, null);

                    // Vertical blur
                    ConvolveOp vBlur = new ConvolveOp(
                        new Kernel(1, kernel.length, kernel),
                        ConvolveOp.EDGE_NO_OP,
                        null
                    );
                    shadowImage = vBlur.filter(shadowImage, null);
                }

                // Composite blurred shadow onto main canvas
                g.drawImage(shadowImage, x, y, null);

            } finally {
                // Always dispose graphics resources
                if (shadowG2d != null && !shadowG2d.equals(g)) {
                    shadowG2d.dispose();
                }
            }
        } else {
            // No blur, just draw shadow once
            g.setPaint(shadowCol);
            if (isFill) {
                g.fill(shadowShape);
            } else {
                g.draw(shadowShape);
            }
        }

        // Restore state
        g.setPaint(oldPaint);
        g.setComposite(oldComposite);
    }

    /**
     * Create a 1D Gaussian kernel for shadow blur.
     * Uses a simpler calculation optimized for shadow rendering.
     *
     * @param radius Blur radius
     * @return Normalized Gaussian kernel
     */
    private float[] createGaussianKernelForShadow(int radius) {
        if (radius < 1) {
            return new float[]{1.0f};
        }

        int size = radius * 2 + 1;
        float[] kernel = new float[size];
        float sigma = radius / (float) GAUSSIAN_SIGMA_RATIO;
        float twoSigmaSquare = 2.0f * sigma * sigma;
        float sum = 0;

        for (int i = 0; i < size; i++) {
            int x = i - radius;
            kernel[i] = (float) Math.exp(-(x * x) / twoSigmaSquare);
            sum += kernel[i];
        }

        // Normalize
        for (int i = 0; i < size; i++) {
            kernel[i] /= sum;
        }

        return kernel;
    }

    private java.awt.Color parseColor(String color) {
        try {
            // Use existing ColorParser if available, otherwise parse basic colors
            if (color.startsWith("rgba(")) {
                String[] parts = color.substring(5, color.length() - 1).split(",");
                int r = Integer.parseInt(parts[0].trim());
                int g = Integer.parseInt(parts[1].trim());
                int b = Integer.parseInt(parts[2].trim());
                float a = Float.parseFloat(parts[3].trim());
                return new java.awt.Color(r, g, b, (int) (a * 255));
            } else if (color.startsWith("rgb(")) {
                String[] parts = color.substring(4, color.length() - 1).split(",");
                int r = Integer.parseInt(parts[0].trim());
                int g = Integer.parseInt(parts[1].trim());
                int b = Integer.parseInt(parts[2].trim());
                return new java.awt.Color(r, g, b);
            } else if (color.startsWith("#")) {
                return java.awt.Color.decode(color);
            }
        } catch (Exception e) {
            // Fallback to black
        }
        return java.awt.Color.BLACK;
    }

    // Filter methods
    @Override
    public void setFilter(String filter) {
        this.filter = (filter == null || filter.trim().isEmpty()) ? "none" : filter;
    }

    @Override
    public String getFilter() {
        return this.filter;
    }

    /**
     * Apply CSS filters to an image using AWT BufferedImageOp operations.
     * This creates a filtered version of the current canvas content.
     *
     * This method is called during fill(), stroke(), and drawImage() operations to apply
     * CSS filters to the rendered content.
     *
     * Implementation approach:
     * 1. Render the operation to a temporary BufferedImage
     * 2. Apply filters using this method
     * 3. Composite the filtered result back to the main canvas
     *
     * This requires architectural changes to support off-screen rendering buffers.
     * See HTML Canvas spec: https://html.spec.whatwg.org/multipage/canvas.html#filters
     */
    private BufferedImage applyFiltersToImage(BufferedImage source) {
        if (filter == null || "none".equals(filter)) {
            return source;
        }

        java.util.List<com.w3canvas.javacanvas.core.FilterFunction> filters =
            com.w3canvas.javacanvas.core.CSSFilterParser.parse(filter);

        if (filters.isEmpty()) {
            return source;
        }

        BufferedImage result = source;

        for (com.w3canvas.javacanvas.core.FilterFunction filterFunc : filters) {
            result = applySingleFilter(result, filterFunc);
        }

        return result;
    }

    /**
     * Apply a single filter function to an image
     */
    private BufferedImage applySingleFilter(BufferedImage source, com.w3canvas.javacanvas.core.FilterFunction filter) {
        switch (filter.getType()) {
            case BLUR:
                return applyBlurFilter(source, filter.getDoubleParam(0));
            case BRIGHTNESS:
                return applyBrightnessFilter(source, filter.getDoubleParam(0));
            case CONTRAST:
                return applyContrastFilter(source, filter.getDoubleParam(0));
            case GRAYSCALE:
                return applyGrayscaleFilter(source, filter.getDoubleParam(0));
            case SEPIA:
                return applySepiaFilter(source, filter.getDoubleParam(0));
            case SATURATE:
                return applySaturateFilter(source, filter.getDoubleParam(0));
            case HUE_ROTATE:
                return applyHueRotateFilter(source, filter.getDoubleParam(0));
            case INVERT:
                return applyInvertFilter(source, filter.getDoubleParam(0));
            case OPACITY:
                return applyOpacityFilter(source, filter.getDoubleParam(0));
            default:
                return source;
        }
    }

    /**
     * Apply blur filter using ConvolveOp with Gaussian kernel.
     * Uses separable convolution (horizontal then vertical) for better performance.
     * Performance: ~5-10x faster than pixel loops for large images.
     */
    private BufferedImage applyBlurFilter(BufferedImage source, double radius) {
        if (radius <= 0) {
            return source;
        }

        // Create Gaussian kernel for separable convolution
        int kernelSize = Math.max(3, (int) Math.ceil(radius) * 2 + 1);
        float[] kernel1D = createGaussianKernel(kernelSize, radius);

        // Horizontal blur
        java.awt.image.Kernel hKernel = new java.awt.image.Kernel(kernelSize, 1, kernel1D);
        java.awt.image.ConvolveOp hBlur = new java.awt.image.ConvolveOp(
            hKernel,
            java.awt.image.ConvolveOp.EDGE_NO_OP,
            null
        );

        BufferedImage temp = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
        hBlur.filter(source, temp);

        // Vertical blur
        java.awt.image.Kernel vKernel = new java.awt.image.Kernel(1, kernelSize, kernel1D);
        java.awt.image.ConvolveOp vBlur = new java.awt.image.ConvolveOp(
            vKernel,
            java.awt.image.ConvolveOp.EDGE_NO_OP,
            null
        );

        BufferedImage result = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
        vBlur.filter(temp, result);

        return result;
    }

    /**
     * Create a 1D Gaussian kernel for separable blur.
     * @param size Kernel size (must be odd)
     * @param sigma Standard deviation (radius/2)
     * @return Normalized Gaussian kernel
     */
    private float[] createGaussianKernel(int size, double sigma) {
        float[] kernel = new float[size];
        double s = sigma > 0 ? sigma : size / 6.0; // Default sigma
        double mean = size / 2.0;
        double sum = 0.0;

        for (int i = 0; i < size; i++) {
            double x = i - mean;
            kernel[i] = (float) Math.exp(-(x * x) / (2 * s * s));
            sum += kernel[i];
        }

        // Normalize kernel
        for (int i = 0; i < size; i++) {
            kernel[i] /= sum;
        }

        return kernel;
    }

    /**
     * Apply brightness filter using RescaleOp
     */
    private BufferedImage applyBrightnessFilter(BufferedImage source, double amount) {
        float scale = (float) amount;
        float offset = 0.0f;

        java.awt.image.RescaleOp rescale = new java.awt.image.RescaleOp(scale, offset, null);
        BufferedImage result = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
        rescale.filter(source, result);
        return result;
    }

    /**
     * Apply contrast filter
     */
    private BufferedImage applyContrastFilter(BufferedImage source, double amount) {
        float scale = (float) amount;
        float offset = 128.0f * (1.0f - scale);

        java.awt.image.RescaleOp rescale = new java.awt.image.RescaleOp(scale, offset, null);
        BufferedImage result = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
        rescale.filter(source, result);
        return result;
    }

    /**
     * Apply grayscale filter using ColorConvertOp for performance.
     * Performance: ~5-10x faster than pixel loops.
     */
    private BufferedImage applyGrayscaleFilter(BufferedImage source, double amount) {
        if (amount <= 0) {
            return source;
        }

        if (amount >= 1.0) {
            // Full grayscale - use ColorConvertOp directly
            ColorConvertOp op = new ColorConvertOp(
                ColorSpace.getInstance(ColorSpace.CS_sRGB),
                ColorSpace.getInstance(ColorSpace.CS_GRAY),
                null
            );
            BufferedImage gray = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
            op.filter(source, gray);

            // Convert back to ARGB to maintain alpha channel
            ColorConvertOp backOp = new ColorConvertOp(
                ColorSpace.getInstance(ColorSpace.CS_GRAY),
                ColorSpace.getInstance(ColorSpace.CS_sRGB),
                null
            );
            BufferedImage result = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
            backOp.filter(gray, result);

            // Preserve alpha channel from source
            int[] srcPixels = source.getRGB(0, 0, source.getWidth(), source.getHeight(), null, 0, source.getWidth());
            int[] dstPixels = result.getRGB(0, 0, result.getWidth(), result.getHeight(), null, 0, result.getWidth());
            for (int i = 0; i < srcPixels.length; i++) {
                dstPixels[i] = (srcPixels[i] & 0xFF000000) | (dstPixels[i] & 0x00FFFFFF);
            }
            result.setRGB(0, 0, result.getWidth(), result.getHeight(), dstPixels, 0, result.getWidth());

            return result;
        }

        // Partial grayscale - need interpolation (fallback to pixel loop)
        BufferedImage result = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < source.getHeight(); y++) {
            for (int x = 0; x < source.getWidth(); x++) {
                int rgb = source.getRGB(x, y);
                int a = (rgb >> 24) & 0xff;
                int r = (rgb >> 16) & 0xff;
                int g = (rgb >> 8) & 0xff;
                int b = rgb & 0xff;

                // Calculate grayscale value
                int gray = (int) (0.299 * r + 0.587 * g + 0.114 * b);

                // Interpolate between original and grayscale
                r = (int) (r + amount * (gray - r));
                g = (int) (g + amount * (gray - g));
                b = (int) (b + amount * (gray - b));

                rgb = (a << 24) | (r << 16) | (g << 8) | b;
                result.setRGB(x, y, rgb);
            }
        }

        return result;
    }

    /**
     * Apply sepia filter
     */
    private BufferedImage applySepiaFilter(BufferedImage source, double amount) {
        BufferedImage result = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < source.getHeight(); y++) {
            for (int x = 0; x < source.getWidth(); x++) {
                int rgb = source.getRGB(x, y);
                int a = (rgb >> 24) & 0xff;
                int r = (rgb >> 16) & 0xff;
                int g = (rgb >> 8) & 0xff;
                int b = rgb & 0xff;

                // Calculate sepia values
                int sr = (int) Math.min(255, (r * 0.393 + g * 0.769 + b * 0.189));
                int sg = (int) Math.min(255, (r * 0.349 + g * 0.686 + b * 0.168));
                int sb = (int) Math.min(255, (r * 0.272 + g * 0.534 + b * 0.131));

                // Interpolate between original and sepia
                r = (int) (r + amount * (sr - r));
                g = (int) (g + amount * (sg - g));
                b = (int) (b + amount * (sb - b));

                rgb = (a << 24) | (r << 16) | (g << 8) | b;
                result.setRGB(x, y, rgb);
            }
        }

        return result;
    }

    /**
     * Apply saturate filter
     */
    private BufferedImage applySaturateFilter(BufferedImage source, double amount) {
        BufferedImage result = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < source.getHeight(); y++) {
            for (int x = 0; x < source.getWidth(); x++) {
                int rgb = source.getRGB(x, y);
                int a = (rgb >> 24) & 0xff;
                int r = (rgb >> 16) & 0xff;
                int g = (rgb >> 8) & 0xff;
                int b = rgb & 0xff;

                // Calculate grayscale for saturation adjustment
                int gray = (int) (0.299 * r + 0.587 * g + 0.114 * b);

                // Adjust saturation
                r = (int) Math.max(0, Math.min(255, gray + amount * (r - gray)));
                g = (int) Math.max(0, Math.min(255, gray + amount * (g - gray)));
                b = (int) Math.max(0, Math.min(255, gray + amount * (b - gray)));

                rgb = (a << 24) | (r << 16) | (g << 8) | b;
                result.setRGB(x, y, rgb);
            }
        }

        return result;
    }

    /**
     * Apply hue-rotate filter
     */
    private BufferedImage applyHueRotateFilter(BufferedImage source, double degrees) {
        BufferedImage result = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);

        double radians = Math.toRadians(degrees);
        double cosA = Math.cos(radians);
        double sinA = Math.sin(radians);

        // Rotation matrix for hue
        double[][] matrix = {
            {0.213 + cosA * 0.787 - sinA * 0.213, 0.715 - cosA * 0.715 - sinA * 0.715, 0.072 - cosA * 0.072 + sinA * 0.928},
            {0.213 - cosA * 0.213 + sinA * 0.143, 0.715 + cosA * 0.285 + sinA * 0.140, 0.072 - cosA * 0.072 - sinA * 0.283},
            {0.213 - cosA * 0.213 - sinA * 0.787, 0.715 - cosA * 0.715 + sinA * 0.715, 0.072 + cosA * 0.928 + sinA * 0.072}
        };

        for (int y = 0; y < source.getHeight(); y++) {
            for (int x = 0; x < source.getWidth(); x++) {
                int rgb = source.getRGB(x, y);
                int a = (rgb >> 24) & 0xff;
                int r = (rgb >> 16) & 0xff;
                int g = (rgb >> 8) & 0xff;
                int b = rgb & 0xff;

                int nr = (int) Math.max(0, Math.min(255, r * matrix[0][0] + g * matrix[0][1] + b * matrix[0][2]));
                int ng = (int) Math.max(0, Math.min(255, r * matrix[1][0] + g * matrix[1][1] + b * matrix[1][2]));
                int nb = (int) Math.max(0, Math.min(255, r * matrix[2][0] + g * matrix[2][1] + b * matrix[2][2]));

                rgb = (a << 24) | (nr << 16) | (ng << 8) | nb;
                result.setRGB(x, y, rgb);
            }
        }

        return result;
    }

    /**
     * Apply invert filter using LookupOp for performance.
     * Performance: ~5-10x faster than pixel loops.
     */
    private BufferedImage applyInvertFilter(BufferedImage source, double amount) {
        if (amount <= 0) {
            return source;
        }

        if (amount >= 1.0) {
            // Full invert - use LookupOp directly
            byte[] invert = new byte[256];
            for (int i = 0; i < 256; i++) {
                invert[i] = (byte) (255 - i);
            }

            // Create separate lookup tables for RGB (invert) and Alpha (preserve)
            byte[] identity = new byte[256];
            for (int i = 0; i < 256; i++) {
                identity[i] = (byte) i;
            }

            byte[][] lookupData = {invert, invert, invert, identity}; // R, G, B inverted, A preserved
            ByteLookupTable lookupTable = new ByteLookupTable(0, lookupData);
            LookupOp op = new LookupOp(lookupTable, null);

            BufferedImage result = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
            op.filter(source, result);
            return result;
        }

        // Partial invert - need interpolation (fallback to pixel loop)
        BufferedImage result = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < source.getHeight(); y++) {
            for (int x = 0; x < source.getWidth(); x++) {
                int rgb = source.getRGB(x, y);
                int a = (rgb >> 24) & 0xff;
                int r = (rgb >> 16) & 0xff;
                int g = (rgb >> 8) & 0xff;
                int b = rgb & 0xff;

                // Invert
                int ir = 255 - r;
                int ig = 255 - g;
                int ib = 255 - b;

                // Interpolate
                r = (int) (r + amount * (ir - r));
                g = (int) (g + amount * (ig - g));
                b = (int) (b + amount * (ib - b));

                rgb = (a << 24) | (r << 16) | (g << 8) | b;
                result.setRGB(x, y, rgb);
            }
        }

        return result;
    }

    /**
     * Apply opacity filter using RescaleOp for performance.
     * Performance: ~5-10x faster than pixel loops.
     */
    private BufferedImage applyOpacityFilter(BufferedImage source, double amount) {
        if (amount >= 1.0) {
            return source; // No change needed
        }

        if (amount <= 0) {
            // Fully transparent
            BufferedImage result = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
            // Return empty image (all transparent)
            return result;
        }

        // Use RescaleOp with per-channel scaling
        // Scale factors: R, G, B unchanged (1.0), Alpha scaled by amount
        float[] scales = {1.0f, 1.0f, 1.0f, (float) amount};
        float[] offsets = {0.0f, 0.0f, 0.0f, 0.0f};

        RescaleOp op = new RescaleOp(scales, offsets, null);
        BufferedImage result = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
        op.filter(source, result);

        return result;
    }

    /**
     * Check if CSS filters should be applied.
     * Filters are active if the filter property is set to something other than "none".
     *
     * @return true if filters should be applied, false otherwise
     */
    private boolean shouldApplyFilters() {
        return filter != null && !"none".equals(filter) && !filter.trim().isEmpty();
    }

    /**
     * Convert an Image object to BufferedImage.
     * If the object is already a BufferedImage, return it directly.
     * Otherwise, create a new BufferedImage and draw the image onto it.
     *
     * @param img The image object to convert
     * @return A BufferedImage, or null if conversion fails
     */
    private BufferedImage toBufferedImage(Object img) {
        if (img instanceof BufferedImage) {
            return (BufferedImage) img;
        }

        if (img instanceof java.awt.Image) {
            java.awt.Image awtImg = (java.awt.Image) img;
            int width = awtImg.getWidth(null);
            int height = awtImg.getHeight(null);

            if (width <= 0 || height <= 0) {
                return null;
            }

            BufferedImage buffered = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = buffered.createGraphics();
            g.drawImage(awtImg, 0, 0, null);
            g.dispose();
            return buffered;
        }

        return null;
    }

    /**
     * Dispose the underlying Graphics2D to release resources and flush any pending drawing operations.
     */
    public void dispose() {
        if (g2d != null) {
            g2d.dispose();
        }
    }
}
