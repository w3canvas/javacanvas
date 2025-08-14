package com.w3canvas.javacanvas.backend.awt;

import com.w3canvas.javacanvas.interfaces.ICanvasPattern;
import com.w3canvas.javacanvas.interfaces.IPaint;

import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.TexturePaint;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;

public class AwtPattern implements ICanvasPattern, IPaint {
    private final Raster raster;
    private final int width;
    private final int height;

    public AwtPattern(Object image, String repetition) {
        if (image instanceof BufferedImage) {
            BufferedImage originalImage = (BufferedImage) image;
            this.width = originalImage.getWidth();
            this.height = originalImage.getHeight();
            this.raster = originalImage.getData();
        } else {
            throw new IllegalArgumentException("Image must be a BufferedImage for AWT backend.");
        }
    }

    public Paint getPaint() {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        image.setData(raster);
        return new TexturePaint(image, new Rectangle(0, 0, width, height));
    }
}
