package com.w3canvas.javacanvas.interfaces;

/**
 * Graphics context interface that abstracts 2D rendering operations.
 *
 * <p>This interface provides a backend-agnostic abstraction layer for 2D graphics operations,
 * allowing the Canvas 2D API to work with different rendering backends (AWT, JavaFX, etc.).
 * It is implemented by backend-specific classes that handle the actual drawing operations.
 *
 * <p>The interface closely follows the HTML5 Canvas 2D rendering context API, providing
 * methods for drawing shapes, text, images, and applying transformations and styles.
 *
 * @see ICanvasRenderingContext2D
 * @since 1.0
 */
public interface IGraphicsContext {
    // Transformations

    /**
     * Scales the current transformation matrix by the specified factors.
     *
     * @param x the horizontal scaling factor
     * @param y the vertical scaling factor
     */
    void scale(double x, double y);

    /**
     * Rotates the current transformation matrix by the specified angle.
     *
     * @param theta the rotation angle in radians (clockwise)
     */
    void rotate(double theta);

    /**
     * Translates the current transformation matrix by the specified distances.
     *
     * @param tx the horizontal translation distance
     * @param ty the vertical translation distance
     */
    void translate(double tx, double ty);

    /**
     * Multiplies the current transformation matrix by the specified matrix.
     *
     * <p>The transformation matrix is represented as:
     * <pre>
     * [ m11  m21  dx ]
     * [ m12  m22  dy ]
     * [  0    0    1 ]
     * </pre>
     *
     * @param m11 the horizontal scaling component
     * @param m12 the vertical skewing component
     * @param m21 the horizontal skewing component
     * @param m22 the vertical scaling component
     * @param dx the horizontal translation component
     * @param dy the vertical translation component
     */
    void transform(double m11, double m12, double m21, double m22, double dx, double dy);

    /**
     * Replaces the current transformation matrix with the specified matrix.
     *
     * <p>The transformation matrix is represented as:
     * <pre>
     * [ m11  m21  dx ]
     * [ m12  m22  dy ]
     * [  0    0    1 ]
     * </pre>
     *
     * @param m11 the horizontal scaling component
     * @param m12 the vertical skewing component
     * @param m21 the horizontal skewing component
     * @param m22 the vertical scaling component
     * @param dx the horizontal translation component
     * @param dy the vertical translation component
     */
    void setTransform(double m11, double m12, double m21, double m22, double dx, double dy);

    /**
     * Sets the current transformation matrix to the specified object.
     *
     * @param transform the transformation object (backend-specific, e.g., AffineTransform)
     */
    void setTransform(Object transform);

    /**
     * Resets the current transformation matrix to the identity matrix.
     *
     * <p>This is equivalent to calling {@code setTransform(1, 0, 0, 1, 0, 0)}.
     */
    void resetTransform();

    /**
     * Gets the current transformation matrix.
     *
     * @return the current transformation object (backend-specific, e.g., AffineTransform)
     */
    Object getTransform();

    // Drawing properties

    /**
     * Sets the paint to use for filling shapes.
     *
     * @param paint the fill paint (color, gradient, or pattern)
     */
    void setFillPaint(IPaint paint);

    /**
     * Sets the paint to use for stroking shapes.
     *
     * @param paint the stroke paint (color, gradient, or pattern)
     */
    void setStrokePaint(IPaint paint);

    /**
     * Creates a pattern using an image for repeating.
     *
     * @param image the image to use as the pattern
     * @param repetition the repetition mode: "repeat", "repeat-x", "repeat-y", or "no-repeat"
     * @return a canvas pattern object
     */
    ICanvasPattern createPattern(Object image, String repetition);

    /**
     * Sets the line width for stroking operations.
     *
     * @param width the line width in pixels (must be positive)
     */
    void setLineWidth(double width);

    /**
     * Sets the line cap style for stroking operations.
     *
     * @param cap the line cap style: "butt", "round", or "square"
     */
    void setLineCap(String cap);

    /**
     * Sets the line join style for stroking operations.
     *
     * @param join the line join style: "miter", "round", or "bevel"
     */
    void setLineJoin(String join);

    /**
     * Sets the miter limit for line joins.
     *
     * <p>The miter limit determines the maximum miter length when using "miter" line joins.
     * If the miter length exceeds this limit, the join is rendered as "bevel" instead.
     *
     * @param limit the miter limit (must be positive, default is 10.0)
     */
    void setMiterLimit(double limit);

    /**
     * Sets the line dash pattern for stroking operations.
     *
     * @param dash array of dash lengths (alternating on/off), or null for solid lines
     */
    void setLineDash(double[] dash);

    /**
     * Sets the line dash offset for stroking operations.
     *
     * @param offset the offset at which to start the dash pattern
     */
    void setLineDashOffset(double offset);

    /**
     * Sets the compositing operation for drawing.
     *
     * @param comp the composite object defining the blending mode
     */
    void setComposite(IComposite comp);

    /**
     * Sets the global alpha (opacity) for all drawing operations.
     *
     * @param alpha the alpha value (0.0 = fully transparent, 1.0 = fully opaque)
     */
    void setGlobalAlpha(double alpha);

    /**
     * Sets the font to use for text rendering.
     *
     * @param font the font object
     */
    void setFont(IFont font);

    /**
     * Sets the text alignment for text rendering.
     *
     * @param textAlign the text alignment: "start", "end", "left", "right", or "center"
     */
    void setTextAlign(String textAlign);

    /**
     * Sets the text baseline for text rendering.
     *
     * @param textBaseline the text baseline: "top", "hanging", "middle", "alphabetic", "ideographic", or "bottom"
     */
    void setTextBaseline(String textBaseline);

    /**
     * Sets the text direction.
     *
     * @param direction the text direction: "ltr", "rtl", or "inherit"
     */
    void setDirection(String direction);

    /**
     * Sets the letter spacing (tracking).
     *
     * @param spacing the letter spacing in pixels
     */
    void setLetterSpacing(double spacing);

    /**
     * Sets the word spacing.
     *
     * @param spacing the word spacing in pixels
     */
    void setWordSpacing(double spacing);

    // Shadow properties

    /**
     * Sets the shadow blur radius.
     *
     * @param blur the blur radius in pixels (non-negative)
     */
    void setShadowBlur(double blur);

    /**
     * Sets the shadow color.
     *
     * @param color the shadow color as a CSS color string
     */
    void setShadowColor(String color);

    /**
     * Sets the horizontal shadow offset.
     *
     * @param offsetX the horizontal offset in pixels
     */
    void setShadowOffsetX(double offsetX);

    /**
     * Sets the vertical shadow offset.
     *
     * @param offsetY the vertical offset in pixels
     */
    void setShadowOffsetY(double offsetY);

    // Image smoothing

    /**
     * Enables or disables image smoothing for image rendering.
     *
     * <p>When enabled, images are smoothed (interpolated) when scaled.
     * When disabled, nearest-neighbor scaling is used.
     *
     * @param enabled true to enable smoothing, false to disable
     */
    void setImageSmoothingEnabled(boolean enabled);

    /**
     * Sets the image smoothing quality.
     *
     * @param quality the quality level: "low", "medium", or "high"
     */
    void setImageSmoothingQuality(String quality);

    // Filter

    /**
     * Sets CSS filter effects to apply to drawing operations.
     *
     * @param filter the filter string (e.g., "blur(5px)", "grayscale(50%)"), or "none"
     */
    void setFilter(String filter);

    /**
     * Gets the current CSS filter.
     *
     * @return the current filter string
     */
    String getFilter();

    // Drawing operations

    /**
     * Clears a rectangular area by making it fully transparent.
     *
     * @param x the x-coordinate of the rectangle's top-left corner
     * @param y the y-coordinate of the rectangle's top-left corner
     * @param w the width of the rectangle
     * @param h the height of the rectangle
     */
    void clearRect(double x, double y, double w, double h);

    /**
     * Fills a rectangle directly without using the path system.
     *
     * <p>This method bypasses the normal path-based rendering and is used
     * internally for performance optimization.
     *
     * @param x the x-coordinate of the rectangle's top-left corner
     * @param y the y-coordinate of the rectangle's top-left corner
     * @param w the width of the rectangle
     * @param h the height of the rectangle
     */
    void fillRectDirect(double x, double y, double w, double h);

    /**
     * Draws (strokes) the outline of a shape.
     *
     * @param shape the shape to draw
     */
    void draw(IShape shape);

    /**
     * Fills the interior of a shape.
     *
     * @param shape the shape to fill
     */
    void fill(IShape shape);

    /**
     * Draws an image at the specified position.
     *
     * @param img the image to draw
     * @param x the x-coordinate of the destination position
     * @param y the y-coordinate of the destination position
     */
    void drawImage(Object img, int x, int y);

    /**
     * Draws a portion of an image to a destination rectangle.
     *
     * @param img the image to draw
     * @param sx the x-coordinate of the source rectangle's top-left corner
     * @param sy the y-coordinate of the source rectangle's top-left corner
     * @param sw the width of the source rectangle
     * @param sh the height of the source rectangle
     * @param dx the x-coordinate of the destination rectangle's top-left corner
     * @param dy the y-coordinate of the destination rectangle's top-left corner
     * @param dw the width of the destination rectangle
     * @param dh the height of the destination rectangle
     */
    void drawImage(Object img, int sx, int sy, int sw, int sh, int dx, int dy, int dw, int dh);

    /**
     * Draws pixel data as an image.
     *
     * @param pixels the pixel array in ARGB format
     * @param x the x-coordinate of the destination position
     * @param y the y-coordinate of the destination position
     * @param width the width of the image
     * @param height the height of the image
     */
    void drawImage(int[] pixels, int x, int y, int width, int height);

    /**
     * Draws a text string at the specified position.
     *
     * @param str the text to draw
     * @param x the x-coordinate of the text position
     * @param y the y-coordinate of the text position
     */
    void drawString(String str, int x, int y);

    /**
     * Measures the dimensions of text.
     *
     * @param text the text to measure
     * @return text metrics containing width and other measurements
     */
    ITextMetrics measureText(String text);

    /**
     * Fills text at the specified position.
     *
     * @param text the text to render
     * @param x the x-coordinate of the text position
     * @param y the y-coordinate of the text position
     * @param maxWidth the maximum width before text is scaled (use Double.MAX_VALUE for no limit)
     */
    void fillText(String text, double x, double y, double maxWidth);

    /**
     * Strokes (outlines) text at the specified position.
     *
     * @param text the text to render
     * @param x the x-coordinate of the text position
     * @param y the y-coordinate of the text position
     * @param maxWidth the maximum width before text is scaled (use Double.MAX_VALUE for no limit)
     */
    void strokeText(String text, double x, double y, double maxWidth);

    /**
     * Creates a new ImageData object with the specified dimensions.
     *
     * @param width the width of the image data
     * @param height the height of the image data
     * @return a new ImageData object with transparent black pixels
     */
    IImageData createImageData(int width, int height);

    /**
     * Gets pixel data from a rectangular area of the canvas.
     *
     * @param x the x-coordinate of the rectangle's top-left corner
     * @param y the y-coordinate of the rectangle's top-left corner
     * @param width the width of the rectangle
     * @param height the height of the rectangle
     * @return an ImageData object containing the pixel data
     */
    IImageData getImageData(int x, int y, int width, int height);

    // Clipping

    /**
     * Creates a clipping region from the current path.
     *
     * <p>All subsequent drawing operations will be clipped to this region.
     * The clipping region is cumulative (intersects with existing region).
     */
    void clip();

    // Path methods

    /**
     * Begins a new path by clearing the current path.
     */
    void beginPath();

    /**
     * Closes the current subpath by drawing a line to the first point.
     */
    void closePath();

    /**
     * Moves the path to the specified point without drawing.
     *
     * @param x the x-coordinate
     * @param y the y-coordinate
     */
    void moveTo(double x, double y);

    /**
     * Adds a line to the path from the current point to the specified point.
     *
     * @param x the x-coordinate of the end point
     * @param y the y-coordinate of the end point
     */
    void lineTo(double x, double y);

    /**
     * Adds a quadratic Bezier curve to the path.
     *
     * @param cpx the x-coordinate of the control point
     * @param cpy the y-coordinate of the control point
     * @param x the x-coordinate of the end point
     * @param y the y-coordinate of the end point
     */
    void quadraticCurveTo(double cpx, double cpy, double x, double y);

    /**
     * Adds a cubic Bezier curve to the path.
     *
     * @param cp1x the x-coordinate of the first control point
     * @param cp1y the y-coordinate of the first control point
     * @param cp2x the x-coordinate of the second control point
     * @param cp2y the y-coordinate of the second control point
     * @param x the x-coordinate of the end point
     * @param y the y-coordinate of the end point
     */
    void bezierCurveTo(double cp1x, double cp1y, double cp2x, double cp2y, double x, double y);

    /**
     * Adds an arc to the path, connecting to the previous point with a line.
     *
     * <p>The arc is tangent to the lines from the current point to (x1,y1)
     * and from (x1,y1) to (x2,y2), with the specified radius.
     *
     * @param x1 the x-coordinate of the first control point
     * @param y1 the y-coordinate of the first control point
     * @param x2 the x-coordinate of the second control point
     * @param y2 the y-coordinate of the second control point
     * @param radius the arc radius
     */
    void arcTo(double x1, double y1, double x2, double y2, double radius);

    /**
     * Adds a rectangle to the path.
     *
     * @param x the x-coordinate of the rectangle's top-left corner
     * @param y the y-coordinate of the rectangle's top-left corner
     * @param w the width of the rectangle
     * @param h the height of the rectangle
     */
    void rect(double x, double y, double w, double h);

    /**
     * Adds a rounded rectangle to the path.
     *
     * @param x the x-coordinate of the rectangle's top-left corner
     * @param y the y-coordinate of the rectangle's top-left corner
     * @param w the width of the rectangle
     * @param h the height of the rectangle
     * @param radii corner radii (number, array, or DOMPointInit-like object)
     */
    void roundRect(double x, double y, double w, double h, Object radii);

    /**
     * Adds a circular arc to the path.
     *
     * @param x the x-coordinate of the arc's center
     * @param y the y-coordinate of the arc's center
     * @param radius the arc radius
     * @param startAngle the start angle in radians
     * @param endAngle the end angle in radians
     * @param counterclockwise true for counterclockwise, false for clockwise
     */
    void arc(double x, double y, double radius, double startAngle, double endAngle, boolean counterclockwise);

    /**
     * Adds an elliptical arc to the path.
     *
     * @param x the x-coordinate of the ellipse's center
     * @param y the y-coordinate of the ellipse's center
     * @param radiusX the horizontal radius
     * @param radiusY the vertical radius
     * @param rotation the rotation angle in radians
     * @param startAngle the start angle in radians
     * @param endAngle the end angle in radians
     * @param counterclockwise true for counterclockwise, false for clockwise
     */
    void ellipse(double x, double y, double radiusX, double radiusY, double rotation, double startAngle, double endAngle, boolean counterclockwise);

    /**
     * Fills the current path using the non-zero winding rule.
     */
    void fill();

    /**
     * Fills the current path using the specified fill rule.
     *
     * @param fillRule the fill rule: "nonzero" or "evenodd"
     */
    void fill(String fillRule);

    /**
     * Strokes the current path.
     */
    void stroke();

    /**
     * Tests whether a point is inside the current path.
     *
     * @param x the x-coordinate of the point
     * @param y the y-coordinate of the point
     * @return true if the point is inside the path
     */
    boolean isPointInPath(double x, double y);

    /**
     * Tests whether a point is on the stroke of the current path.
     *
     * @param x the x-coordinate of the point
     * @param y the y-coordinate of the point
     * @return true if the point is on the stroke
     */
    boolean isPointInStroke(double x, double y);

    /**
     * Sets the fill rule for path filling operations.
     *
     * @param fillRule the fill rule: "nonzero" or "evenodd"
     */
    void setFillRule(String fillRule);

    /**
     * Gets the current path as a shape object.
     *
     * @return the current path
     */
    IShape getPath();

    /**
     * Sets the current path to the specified shape.
     *
     * @param path the shape to use as the current path
     */
    void setPath(IShape path);

    /**
     * Gets the last point in the current path.
     *
     * @return an array containing [x, y] coordinates of the last point
     */
    double[] getLastPoint();
}
