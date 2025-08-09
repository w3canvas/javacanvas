package com.w3canvas.javacanvas.js.impl.node;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.ColorModel;
import java.awt.image.MemoryImageSource;

import com.w3canvas.javacanvas.utils.RhinoCanvasUtils;

public class ImageData extends ProjectScriptableObject
{

    private CanvasPixelArray data;

    // called through reflection
    public ImageData()
    {
        this(0, 0, null);
    }

    // called through reflection
    public ImageData(Integer width, Integer height, CanvasPixelArray data)
    {
        this.data = data;

        if (this.data == null)
        {
            this.data = RhinoCanvasUtils.getScriptableInstance(CanvasPixelArray.class, new Object[]{width, height});
        }
    }

    public Integer jsGet_width()
    {
        return data.getWidth();
    }

    public Integer jsGet_height()
    {
        return data.getHeight();
    }

    public CanvasPixelArray jsGet_data()
    {
        return data;
    }

    public Image getImage(int dirtyX, int dirtyY, int dirtyWidth, int dirtyHeight)
    {

        int width = (dirtyWidth == 0 || dirtyWidth > data.getWidth()) ? data.getWidth() : dirtyWidth;
        int height = (dirtyHeight == 0 || dirtyHeight > data.getHeight()) ? data.getHeight() : dirtyHeight;

        Image image = Toolkit.getDefaultToolkit().createImage(
            new MemoryImageSource(width, height, ColorModel.getRGBdefault(), data.getPixels(dirtyX, dirtyY, width,
                height), 0, width));

        return image;
    }
}
