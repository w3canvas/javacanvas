package com.w3canvas.javacanvas.core;

import com.w3canvas.javacanvas.interfaces.IPath2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Reusable path object following the HTML5 Path2D API.
 *
 * <p>This class provides a way to declare paths that can be retained and replayed
 * multiple times. It corresponds to the HTML5 Canvas Path2D API, allowing paths
 * to be created independently of the rendering context and then used with
 * {@code fill()}, {@code stroke()}, {@code clip()}, and hit-testing methods.
 *
 * <p>The path is stored as a list of path elements (commands) that can be replayed
 * on any {@link IGraphicsContext}. This allows for:
 * <ul>
 *   <li>Creating complex paths once and reusing them multiple times</li>
 *   <li>Copying and modifying paths without affecting the original</li>
 *   <li>Combining paths using {@link #addPath(IPath2D)}</li>
 *   <li>Serializing and deserializing paths (command list is accessible)</li>
 * </ul>
 *
 * <p><strong>Example usage:</strong>
 * <pre>{@code
 * Path2D path = new Path2D();
 * path.rect(10, 10, 100, 100);
 * path.arc(60, 60, 40, 0, Math.PI * 2, false);
 *
 * // Use the path multiple times
 * ctx.fill(path, "evenodd");
 * ctx.translate(150, 0);
 * ctx.stroke(path);
 * }</pre>
 *
 * @see IPath2D
 * @see ICanvasRenderingContext2D#fill(IPath2D)
 * @see ICanvasRenderingContext2D#stroke(IPath2D)
 * @since 1.0
 */
public class Path2D implements IPath2D {

    private final List<PathElement> elements;

    /**
     * Create a new empty Path2D.
     */
    public Path2D() {
        this.elements = new ArrayList<>();
    }

    /**
     * Create a new Path2D as a copy of another path.
     */
    public Path2D(IPath2D path) {
        this.elements = new ArrayList<>();
        if (path != null) {
            addPath(path);
        }
    }

    @Override
    public void moveTo(double x, double y) {
        elements.add(new PathElement(PathElement.Type.MOVE_TO, null, x, y));
    }

    @Override
    public void lineTo(double x, double y) {
        elements.add(new PathElement(PathElement.Type.LINE_TO, null, x, y));
    }

    @Override
    public void quadraticCurveTo(double cpx, double cpy, double x, double y) {
        elements.add(new PathElement(PathElement.Type.QUADRATIC_CURVE_TO, null, cpx, cpy, x, y));
    }

    @Override
    public void bezierCurveTo(double cp1x, double cp1y, double cp2x, double cp2y, double x, double y) {
        elements.add(new PathElement(PathElement.Type.BEZIER_CURVE_TO, null, cp1x, cp1y, cp2x, cp2y, x, y));
    }

    @Override
    public void arcTo(double x1, double y1, double x2, double y2, double radius) {
        elements.add(new PathElement(PathElement.Type.ARC_TO, null, x1, y1, x2, y2, radius));
    }

    @Override
    public void rect(double x, double y, double w, double h) {
        elements.add(new PathElement(PathElement.Type.RECT, null, x, y, w, h));
    }

    @Override
    public void roundRect(double x, double y, double w, double h, Object radii) {
        elements.add(new PathElement(PathElement.Type.ROUND_RECT, radii, x, y, w, h));
    }

    @Override
    public void arc(double x, double y, double radius, double startAngle, double endAngle, boolean counterclockwise) {
        elements.add(new PathElement(PathElement.Type.ARC, null, x, y, radius, startAngle, endAngle, counterclockwise ? 1.0 : 0.0));
    }

    @Override
    public void ellipse(double x, double y, double radiusX, double radiusY, double rotation,
                       double startAngle, double endAngle, boolean counterclockwise) {
        elements.add(new PathElement(PathElement.Type.ELLIPSE, null, x, y, radiusX, radiusY, rotation,
                startAngle, endAngle, counterclockwise ? 1.0 : 0.0));
    }

    @Override
    public void closePath() {
        elements.add(new PathElement(PathElement.Type.CLOSE_PATH, null));
    }

    @Override
    public void addPath(IPath2D path) {
        if (path != null && path.getElements() != null) {
            elements.addAll(path.getElements());
        }
    }

    @Override
    public List<PathElement> getElements() {
        return new ArrayList<>(elements);
    }

    /**
     * Replay all path elements onto a graphics context.
     */
    public void replayOn(com.w3canvas.javacanvas.interfaces.IGraphicsContext gc) {
        for (PathElement element : elements) {
            double[] params = element.getParams();
            switch (element.getType()) {
                case MOVE_TO:
                    gc.moveTo(params[0], params[1]);
                    break;
                case LINE_TO:
                    gc.lineTo(params[0], params[1]);
                    break;
                case QUADRATIC_CURVE_TO:
                    gc.quadraticCurveTo(params[0], params[1], params[2], params[3]);
                    break;
                case BEZIER_CURVE_TO:
                    gc.bezierCurveTo(params[0], params[1], params[2], params[3], params[4], params[5]);
                    break;
                case ARC_TO:
                    gc.arcTo(params[0], params[1], params[2], params[3], params[4]);
                    break;
                case RECT:
                    gc.rect(params[0], params[1], params[2], params[3]);
                    break;
                case ROUND_RECT:
                    gc.roundRect(params[0], params[1], params[2], params[3], element.getExtra());
                    break;
                case ARC:
                    gc.arc(params[0], params[1], params[2], params[3], params[4], params[5] != 0.0);
                    break;
                case ELLIPSE:
                    gc.ellipse(params[0], params[1], params[2], params[3], params[4],
                              params[5], params[6], params[7] != 0.0);
                    break;
                case CLOSE_PATH:
                    gc.closePath();
                    break;
            }
        }
    }
}
