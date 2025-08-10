package com.w3canvas.javacanvas.js;

import java.awt.image.BufferedImage;

public interface ICanvas {
    BufferedImage getImage();
    void dirty();
    Integer getWidth();
    Integer getHeight();
}
