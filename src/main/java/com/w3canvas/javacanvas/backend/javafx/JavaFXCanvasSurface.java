package com.w3canvas.javacanvas.backend.javafx;

import com.w3canvas.javacanvas.interfaces.ICanvasSurface;
import com.w3canvas.javacanvas.interfaces.IGraphicsContext;
import javafx.scene.image.WritableImage;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.PixelReader;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;


public class JavaFXCanvasSurface implements ICanvasSurface {
    private final WritableImage image;
    private final Canvas canvas;
    private final GraphicsContext gc;
    private JavaFXGraphicsContext graphicsContext;

    public JavaFXCanvasSurface(int width, int height) {
        this.image = new WritableImage(width, height);
        this.canvas = new Canvas(width, height);
        this.gc = canvas.getGraphicsContext2D();
    }

    @Override
    public int getWidth() {
        return (int) image.getWidth();
    }

    @Override
    public int getHeight() {
        return (int) image.getHeight();
    }

    @Override
    public void reset() {
        // This is not ideal, but it will ensure a clean state.
        graphicsContext = new JavaFXGraphicsContext(canvas.getGraphicsContext2D());
    }

    @Override
    public IGraphicsContext getGraphicsContext() {
        if (graphicsContext == null) {
            graphicsContext = new JavaFXGraphicsContext(gc);
        }
        return graphicsContext;
    }

    @Override
    public Object getNativeImage() {
        return image;
    }

    @Override
    public int[] getPixelData(int x, int y, int width, int height) {
        // Snapshot the canvas to the image before reading pixels
        canvas.snapshot(null, image);
        int[] pixels = new int[width * height];
        PixelReader pixelReader = image.getPixelReader();
        pixelReader.getPixels(x, y, width, height, javafx.scene.image.PixelFormat.getIntArgbInstance(), pixels, 0, width);
        return pixels;
    }
}
