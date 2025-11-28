package com.w3canvas.javacanvas.interfaces;

public interface IGraphicsBackend {
    /**
     * Creates a new drawable surface.
     * 
     * @param width  the width of the surface
     * @param height the height of the surface
     * @return a new ICanvasSurface
     */
    ICanvasSurface createCanvasSurface(int width, int height);

    ICanvasGradient createLinearGradient(double x0, double y0, double x1, double y1);

    ICanvasGradient createRadialGradient(double x0, double y0, double r0, double x1, double y1, double r1);

    ICanvasGradient createConicGradient(double startAngle, double x, double y);

    ICanvasPattern createPattern(Object image, String repetition);

    IFont createFont(String family, double size);

    IFont createFont(String family, double size, String style, String weight);

    /**
     * Creates a font from binary font data (e.g., TrueType or OpenType font file).
     *
     * @param fontData the binary font data to load
     * @param size     the font size in points
     * @param style    the font style (e.g., "normal", "italic", "oblique")
     * @param weight   the font weight (e.g., "normal", "bold")
     * @return a new IFont instance
     * @throws IllegalArgumentException if fontData is null or empty
     * @throws IllegalArgumentException if fontData exceeds 10MB (10485760 bytes)
     */
    IFont createFont(byte[] fontData, float size, String style, String weight);

    /**
     * Creates a composite operation.
     * 
     * @param op    the composite operation type
     * @param alpha the alpha value
     * @return a new IComposite instance
     */
    IComposite createComposite(CompositeOperation op, double alpha);

    /**
     * Creates a paint from a CSS color string.
     *
     * @param color the CSS color string (e.g., "red", "#ff0000", "rgba(0,0,0,0.5)")
     * @return a new IPaint instance
     */
    IPaint createPaint(String color);
}
