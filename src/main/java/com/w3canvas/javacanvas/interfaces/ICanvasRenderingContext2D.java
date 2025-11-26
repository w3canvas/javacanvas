package com.w3canvas.javacanvas.interfaces;

import org.mozilla.javascript.Scriptable;

/**
 * The main Canvas 2D rendering context API.
 *
 * <p>
 * This interface corresponds to the HTML5 Canvas 2D rendering context
 * ({@code CanvasRenderingContext2D}), providing methods for drawing shapes,
 * text, images,
 * and applying various styles and transformations.
 *
 * <p>
 * The Canvas 2D API maintains a drawing state stack (via {@link #save()} and
 * {@link #restore()})
 * and allows compositing, clipping, and pixel manipulation operations.
 *
 * <p>
 * For more information, see the
 * <a href="https://html.spec.whatwg.org/multipage/canvas.html">HTML5 Canvas
 * specification</a>.
 *
 * @see IGraphicsContext
 * @see ICanvasSurface
 * @since 1.0
 */
public interface ICanvasRenderingContext2D {
    /**
     * Gets the canvas surface associated with this rendering context.
     *
     * @return the canvas surface
     */
    ICanvasSurface getSurface();

    /**
     * Saves the current drawing state onto a stack.
     *
     * <p>
     * The saved state includes: transformation matrix, clipping region,
     * and all drawing properties (fill style, stroke style, line width, etc.).
     *
     * @see #restore()
     */
    void save();

    /**
     * Restores the most recently saved drawing state from the stack.
     *
     * <p>
     * If the state stack is empty, this method does nothing.
     *
     * @see #save()
     */
    void restore();

    // Transformation methods

    /**
     * Scales the transformation matrix.
     *
     * @param x the horizontal scaling factor
     * @param y the vertical scaling factor
     */
    void scale(double x, double y);

    /**
     * Rotates the transformation matrix.
     *
     * @param angle the rotation angle in radians (clockwise)
     */
    void rotate(double angle);

    /**
     * Translates the transformation matrix.
     *
     * @param x the horizontal translation distance
     * @param y the vertical translation distance
     */
    void translate(double x, double y);

    /**
     * Multiplies the current transformation matrix.
     *
     * @param m11 the horizontal scaling component
     * @param m12 the vertical skewing component
     * @param m21 the horizontal skewing component
     * @param m22 the vertical scaling component
     * @param dx  the horizontal translation component
     * @param dy  the vertical translation component
     */
    void transform(double m11, double m12, double m21, double m22, double dx, double dy);

    /**
     * Replaces the current transformation matrix.
     *
     * @param m11 the horizontal scaling component
     * @param m12 the vertical skewing component
     * @param m21 the horizontal skewing component
     * @param m22 the vertical scaling component
     * @param dx  the horizontal translation component
     * @param dy  the vertical translation component
     */
    void setTransform(double m11, double m12, double m21, double m22, double dx, double dy);

    /**
     * Resets the transformation matrix to the identity matrix.
     */
    void resetTransform();

    /**
     * Gets the current transformation matrix.
     *
     * @return a DOMMatrix-like object representing the current transform
     */
    Object getTransform();

    // Compositing properties

    /**
     * Gets the global alpha (opacity) value.
     *
     * @return the global alpha value (0.0 to 1.0)
     */
    double getGlobalAlpha();

    /**
     * Sets the global alpha (opacity) value.
     *
     * @param globalAlpha the alpha value (0.0 = fully transparent, 1.0 = fully
     *                    opaque)
     */
    void setGlobalAlpha(double globalAlpha);

    /**
     * Gets the global composite operation.
     *
     * @return the composite operation name
     */
    String getGlobalCompositeOperation();

    /**
     * Sets the global composite operation (blending mode).
     *
     * @param op the operation: "source-over", "destination-over", "copy",
     *           "lighter", etc.
     */
    void setGlobalCompositeOperation(String op);

    // Fill and stroke styles

    /**
     * Gets the fill style.
     *
     * @return the fill style (color string, gradient, or pattern)
     */
    Object getFillStyle();

    /**
     * Sets the fill style for shapes.
     *
     * @param fillStyle a color string, gradient, or pattern
     */
    void setFillStyle(Object fillStyle);

    /**
     * Gets the stroke style.
     *
     * @return the stroke style (color string, gradient, or pattern)
     */
    Object getStrokeStyle();

    /**
     * Sets the stroke style for shapes.
     *
     * @param strokeStyle a color string, gradient, or pattern
     */
    void setStrokeStyle(Object strokeStyle);

    // Gradients and patterns

    /**
     * Creates a linear gradient.
     *
     * @param x0 the x-coordinate of the start point
     * @param y0 the y-coordinate of the start point
     * @param x1 the x-coordinate of the end point
     * @param y1 the y-coordinate of the end point
     * @return a linear gradient object
     */
    ICanvasGradient createLinearGradient(double x0, double y0, double x1, double y1);

    /**
     * Creates a radial gradient.
     *
     * @param x0 the x-coordinate of the start circle
     * @param y0 the y-coordinate of the start circle
     * @param r0 the radius of the start circle
     * @param x1 the x-coordinate of the end circle
     * @param y1 the y-coordinate of the end circle
     * @param r1 the radius of the end circle
     * @return a radial gradient object
     */
    ICanvasGradient createRadialGradient(double x0, double y0, double r0, double x1, double y1, double r1);

    /**
     * Creates a conic gradient.
     *
     * @param startAngle the start angle in radians
     * @param x          the x-coordinate of the center
     * @param y          the y-coordinate of the center
     * @return a conic gradient object
     */
    ICanvasGradient createConicGradient(double startAngle, double x, double y);

    /**
     * Creates a pattern from an image.
     *
     * @param image      the image to use
     * @param repetition the repetition mode: "repeat", "repeat-x", "repeat-y", or
     *                   "no-repeat"
     * @return a pattern object
     */
    ICanvasPattern createPattern(Object image, String repetition);

    // Line styles

    /**
     * Gets the line width.
     *
     * @return the line width in pixels
     */
    double getLineWidth();

    /**
     * Sets the line width for stroking.
     *
     * @param lw the line width in pixels (must be positive)
     */
    void setLineWidth(double lw);

    /**
     * Gets the line cap style.
     *
     * @return the line cap style
     */
    String getLineCap();

    /**
     * Sets the line cap style.
     *
     * @param cap the cap style: "butt", "round", or "square"
     */
    void setLineCap(String cap);

    /**
     * Gets the line join style.
     *
     * @return the line join style
     */
    String getLineJoin();

    /**
     * Sets the line join style.
     *
     * @param join the join style: "miter", "round", or "bevel"
     */
    void setLineJoin(String join);

    /**
     * Gets the miter limit.
     *
     * @return the miter limit value
     */
    double getMiterLimit();

    /**
     * Sets the miter limit for line joins.
     *
     * @param miterLimit the miter limit (must be positive)
     */
    void setMiterLimit(double miterLimit);

    /**
     * Sets the line dash pattern.
     *
     * @param dash array of dash lengths, or null for solid lines
     */
    void setLineDash(Object dash);

    /**
     * Gets the line dash pattern.
     *
     * @return the line dash array
     */
    Object getLineDash();

    /**
     * Gets the line dash offset.
     *
     * @return the dash offset value
     */
    double getLineDashOffset();

    /**
     * Sets the line dash offset.
     *
     * @param offset the offset at which to start the dash pattern
     */
    void setLineDashOffset(double offset);

    // Shadow properties

    /**
     * Gets the shadow blur radius.
     *
     * @return the blur radius in pixels
     */
    double getShadowBlur();

    /**
     * Sets the shadow blur radius.
     *
     * @param blur the blur radius in pixels (non-negative)
     */
    void setShadowBlur(double blur);

    /**
     * Gets the shadow color.
     *
     * @return the shadow color as a CSS color string
     */
    String getShadowColor();

    /**
     * Sets the shadow color.
     *
     * @param color the shadow color as a CSS color string
     */
    void setShadowColor(String color);

    /**
     * Gets the horizontal shadow offset.
     *
     * @return the horizontal offset in pixels
     */
    double getShadowOffsetX();

    /**
     * Sets the horizontal shadow offset.
     *
     * @param offsetX the horizontal offset in pixels
     */
    void setShadowOffsetX(double offsetX);

    /**
     * Gets the vertical shadow offset.
     *
     * @return the vertical offset in pixels
     */
    double getShadowOffsetY();

    /**
     * Sets the vertical shadow offset.
     *
     * @param offsetY the vertical offset in pixels
     */
    void setShadowOffsetY(double offsetY);

    // Rectangle drawing

    /**
     * Clears a rectangular area by making it fully transparent.
     *
     * @param x the x-coordinate
     * @param y the y-coordinate
     * @param w the width
     * @param h the height
     */
    void clearRect(double x, double y, double w, double h);

    /**
     * Fills a rectangle with the current fill style.
     *
     * @param x the x-coordinate
     * @param y the y-coordinate
     * @param w the width
     * @param h the height
     */
    void fillRect(double x, double y, double w, double h);

    /**
     * Strokes a rectangle with the current stroke style.
     *
     * @param x the x-coordinate
     * @param y the y-coordinate
     * @param w the width
     * @param h the height
     */
    void strokeRect(double x, double y, double w, double h);

    // Image smoothing

    /**
     * Gets whether image smoothing is enabled.
     *
     * @return true if image smoothing is enabled
     */
    boolean getImageSmoothingEnabled();

    /**
     * Enables or disables image smoothing.
     *
     * @param enabled true to enable smoothing, false to disable
     */
    void setImageSmoothingEnabled(boolean enabled);

    /**
     * Gets the image smoothing quality.
     *
     * @return the quality level: "low", "medium", or "high"
     */
    String getImageSmoothingQuality();

    /**
     * Sets the image smoothing quality.
     *
     * @param quality the quality level: "low", "medium", or "high"
     */
    void setImageSmoothingQuality(String quality);

    // Path methods

    /**
     * Begins a new path by clearing the current path.
     */
    void beginPath();

    /**
     * Closes the current subpath.
     */
    void closePath();

    /**
     * Moves to a point without drawing.
     *
     * @param x the x-coordinate
     * @param y the y-coordinate
     */
    void moveTo(double x, double y);

    /**
     * Adds a line to the path.
     *
     * @param x the x-coordinate of the end point
     * @param y the y-coordinate of the end point
     */
    void lineTo(double x, double y);

    /**
     * Adds a quadratic curve to the path.
     *
     * @param cpx the x-coordinate of the control point
     * @param cpy the y-coordinate of the control point
     * @param x   the x-coordinate of the end point
     * @param y   the y-coordinate of the end point
     */
    void quadraticCurveTo(double cpx, double cpy, double x, double y);

    /**
     * Adds a cubic Bezier curve to the path.
     *
     * @param cp1x the x-coordinate of the first control point
     * @param cp1y the y-coordinate of the first control point
     * @param cp2x the x-coordinate of the second control point
     * @param cp2y the y-coordinate of the second control point
     * @param x    the x-coordinate of the end point
     * @param y    the y-coordinate of the end point
     */
    void bezierCurveTo(double cp1x, double cp1y, double cp2x, double cp2y, double x, double y);

    /**
     * Adds an arc to the path.
     *
     * @param x1     the x-coordinate of the first control point
     * @param y1     the y-coordinate of the first control point
     * @param x2     the x-coordinate of the second control point
     * @param y2     the y-coordinate of the second control point
     * @param radius the arc radius
     */
    void arcTo(double x1, double y1, double x2, double y2, double radius);

    /**
     * Adds a rectangle to the path.
     *
     * @param x the x-coordinate
     * @param y the y-coordinate
     * @param w the width
     * @param h the height
     */
    void rect(double x, double y, double w, double h);

    /**
     * Adds a rounded rectangle to the path.
     *
     * @param x     the x-coordinate
     * @param y     the y-coordinate
     * @param w     the width
     * @param h     the height
     * @param radii corner radii (number, array, or DOMPointInit-like object)
     */
    void roundRect(double x, double y, double w, double h, Object radii);

    /**
     * Adds a circular arc to the path.
     *
     * @param x                the x-coordinate of the center
     * @param y                the y-coordinate of the center
     * @param radius           the radius
     * @param startAngle       the start angle in radians
     * @param endAngle         the end angle in radians
     * @param counterclockwise true for counterclockwise
     */
    void arc(double x, double y, double radius, double startAngle, double endAngle, boolean counterclockwise);

    /**
     * Adds an elliptical arc to the path.
     *
     * @param x                the x-coordinate of the center
     * @param y                the y-coordinate of the center
     * @param radiusX          the horizontal radius
     * @param radiusY          the vertical radius
     * @param rotation         the rotation angle in radians
     * @param startAngle       the start angle in radians
     * @param endAngle         the end angle in radians
     * @param counterclockwise true for counterclockwise
     */
    void ellipse(double x, double y, double radiusX, double radiusY, double rotation, double startAngle,
            double endAngle, boolean counterclockwise);

    // Fill and stroke

    /**
     * Fills the current path.
     */
    void fill();

    /**
     * Fills the current path with the specified fill rule.
     *
     * @param fillRule the fill rule: "nonzero" or "evenodd"
     */
    void fill(String fillRule);

    /**
     * Fills a Path2D object.
     *
     * @param path the path to fill
     */
    void fill(IPath2D path);

    /**
     * Fills a Path2D object with the specified fill rule.
     *
     * @param path     the path to fill
     * @param fillRule the fill rule: "nonzero" or "evenodd"
     */
    void fill(IPath2D path, String fillRule);

    /**
     * Strokes the current path.
     */
    void stroke();

    /**
     * Strokes a Path2D object.
     *
     * @param path the path to stroke
     */
    void stroke(IPath2D path);

    /**
     * Creates a clipping region from the current path.
     */
    void clip();

    /**
     * Creates a clipping region with the specified fill rule.
     *
     * @param fillRule the fill rule: "nonzero" or "evenodd"
     */
    void clip(String fillRule);

    /**
     * Creates a clipping region from a Path2D object.
     *
     * @param path the path to use for clipping
     */
    void clip(IPath2D path);

    /**
     * Creates a clipping region from a Path2D object with a fill rule.
     *
     * @param path     the path to use for clipping
     * @param fillRule the fill rule: "nonzero" or "evenodd"
     */
    void clip(IPath2D path, String fillRule);

    /**
     * Tests if a point is inside the current path.
     *
     * @param x the x-coordinate
     * @param y the y-coordinate
     * @return true if the point is inside the path
     */
    boolean isPointInPath(double x, double y);

    /**
     * Tests if a point is inside a Path2D object.
     *
     * @param path the path to test
     * @param x    the x-coordinate
     * @param y    the y-coordinate
     * @return true if the point is inside the path
     */
    boolean isPointInPath(IPath2D path, double x, double y);

    /**
     * Tests if a point is on the stroke of the current path.
     *
     * @param x the x-coordinate
     * @param y the y-coordinate
     * @return true if the point is on the stroke
     */
    boolean isPointInStroke(double x, double y);

    /**
     * Tests if a point is on the stroke of a Path2D object.
     *
     * @param path the path to test
     * @param x    the x-coordinate
     * @param y    the y-coordinate
     * @return true if the point is on the stroke
     */
    boolean isPointInStroke(IPath2D path, double x, double y);

    // Drawing images

    /**
     * Draws an image at the specified position.
     *
     * @param image the image to draw
     * @param dx    the x-coordinate
     * @param dy    the y-coordinate
     */
    void drawImage(Object image, double dx, double dy);

    /**
     * Draws an image scaled to the specified size.
     *
     * @param image   the image to draw
     * @param dx      the x-coordinate
     * @param dy      the y-coordinate
     * @param dWidth  the width to draw
     * @param dHeight the height to draw
     */
    void drawImage(Object image, double dx, double dy, double dWidth, double dHeight);

    /**
     * Draws a portion of an image.
     *
     * @param image   the image to draw
     * @param sx      the source x-coordinate
     * @param sy      the source y-coordinate
     * @param sWidth  the source width
     * @param sHeight the source height
     * @param dx      the destination x-coordinate
     * @param dy      the destination y-coordinate
     * @param dWidth  the destination width
     * @param dHeight the destination height
     */
    void drawImage(Object image, double sx, double sy, double sWidth, double sHeight, double dx, double dy,
            double dWidth, double dHeight);

    // Text

    /**
     * Measures text dimensions.
     *
     * @param text the text to measure
     * @return text metrics
     */
    ITextMetrics measureText(String text);

    /**
     * Gets the current font.
     *
     * @return the font as a CSS font string
     */
    String getFont();

    /**
     * Sets the font for text rendering.
     *
     * @param font the font as a CSS font string (e.g., "10px sans-serif")
     */
    void setFont(String font);

    /**
     * Gets the text alignment.
     *
     * @return the text alignment
     */
    String getTextAlign();

    /**
     * Sets the text alignment.
     *
     * @param textAlign the alignment: "start", "end", "left", "right", or "center"
     */
    void setTextAlign(String textAlign);

    /**
     * Gets the text baseline.
     *
     * @return the text baseline
     */
    String getTextBaseline();

    /**
     * Sets the text baseline.
     *
     * @param textBaseline the baseline: "top", "hanging", "middle", "alphabetic",
     *                     "ideographic", or "bottom"
     */
    void setTextBaseline(String textBaseline);

    /**
     * Gets the text direction.
     *
     * @return the direction: "ltr", "rtl", or "inherit"
     */
    String getDirection();

    /**
     * Sets the text direction.
     *
     * @param direction the direction: "ltr", "rtl", or "inherit"
     */
    void setDirection(String direction);

    /**
     * Gets the letter spacing.
     *
     * @return the letter spacing in pixels
     */
    double getLetterSpacing();

    /**
     * Sets the letter spacing.
     *
     * @param spacing the letter spacing in pixels
     */
    void setLetterSpacing(double spacing);

    /**
     * Gets the word spacing.
     *
     * @return the word spacing in pixels
     */
    double getWordSpacing();

    /**
     * Sets the word spacing.
     *
     * @param spacing the word spacing in pixels
     */
    void setWordSpacing(double spacing);

    /**
     * Fills text at the specified position.
     *
     * @param text     the text to render
     * @param x        the x-coordinate
     * @param y        the y-coordinate
     * @param maxWidth the maximum width (optional, use Double.MAX_VALUE for no
     *                 limit)
     */
    void fillText(String text, double x, double y, double maxWidth);

    /**
     * Strokes text at the specified position.
     *
     * @param text     the text to render
     * @param x        the x-coordinate
     * @param y        the y-coordinate
     * @param maxWidth the maximum width (optional, use Double.MAX_VALUE for no
     *                 limit)
     */
    void strokeText(String text, double x, double y, double maxWidth);

    // Pixel manipulation

    /**
     * Creates a new ImageData object with the specified dimensions.
     *
     * @param width  the width in pixels
     * @param height the height in pixels
     * @return a new ImageData object with transparent black pixels
     */
    IImageData createImageData(int width, int height);

    /**
     * Gets pixel data from a rectangular area.
     *
     * @param x      the x-coordinate
     * @param y      the y-coordinate
     * @param width  the width
     * @param height the height
     * @return an ImageData object containing the pixel data
     */
    IImageData getImageData(int x, int y, int width, int height);

    /**
     * Writes pixel data to a rectangular area.
     *
     * @param imagedata   the image data to write
     * @param dx          the destination x-coordinate
     * @param dy          the destination y-coordinate
     * @param dirtyX      the x-coordinate of the dirty rectangle (within imagedata)
     * @param dirtyY      the y-coordinate of the dirty rectangle
     * @param dirtyWidth  the width of the dirty rectangle
     * @param dirtyHeight the height of the dirty rectangle
     */
    void putImageData(IImageData imagedata, int dx, int dy, int dirtyX, int dirtyY, int dirtyWidth, int dirtyHeight);

    // Context state

    /**
     * Checks if the rendering context has been lost.
     *
     * @return true if the context is lost
     */
    boolean isContextLost();

    /**
     * Gets the context attributes.
     *
     * @return a scriptable object containing context attributes
     */
    Scriptable getContextAttributes();

    // Filters

    /**
     * Gets the current CSS filter.
     *
     * @return the filter string
     */
    String getFilter();

    /**
     * Sets CSS filter effects.
     *
     * @param filter the filter string (e.g., "blur(5px)", "grayscale(50%)"), or
     *               "none"
     */
    void setFilter(String filter);

    /**
     * Resets the rendering context to its default state.
     *
     * <p>
     * This clears the canvas, resets all properties to their defaults,
     * and clears the state stack.
     */
    void reset();

    // Focus management

    /**
     * Draws a focus ring around the current path if the element has focus.
     *
     * @param element the element to check for focus
     */
    void drawFocusIfNeeded(Object element);

    /**
     * Draws a focus ring around a path if the element has focus.
     *
     * @param path    the path to draw the focus ring around
     * @param element the element to check for focus
     */
    void drawFocusIfNeeded(IPath2D path, Object element);

    /**
     * Submits a batch of commands to be executed.
     *
     * @param commands the array of commands (integers and arguments)
     */
    void submit(Object[] commands);
}
