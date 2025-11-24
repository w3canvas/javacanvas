package com.w3canvas.javacanvas.backend.awt;

import com.w3canvas.javacanvas.interfaces.ICanvasPeer;
import com.w3canvas.javacanvas.backend.rhino.impl.node.HTMLCanvasElement;
import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Dimension;
import java.awt.image.ImageObserver;
import java.awt.Color;
import javax.swing.RootPaneContainer;

public class SwingCanvasPeer extends JPanel implements ICanvasPeer {

    private final HTMLCanvasElement canvasElement;
    private final RootPaneContainer rootContainer;
    private static final Color BLACK_TRANSPARENT = new Color(0, 0, 0, 0);

    public SwingCanvasPeer(HTMLCanvasElement canvasElement, RootPaneContainer rootContainer) {
        this.canvasElement = canvasElement;
        this.rootContainer = rootContainer;
        this.setDoubleBuffered(true);
    }

    @Override
    public void paint(Graphics g) {
        g.setColor(BLACK_TRANSPARENT);
        g.fillRect(0, 0, getWidth(), getHeight());
        g.drawImage(canvasElement.getImage(), 0, 0, (ImageObserver) rootContainer);
    }

    @Override
    public void repaint(long tm, int x, int y, int width, int height) {
        if (rootContainer != null) {
            rootContainer.getRootPane().repaint(tm, x, y, width, height);
        }
    }

    @Override
    public void repaint(Rectangle r) {
        if (rootContainer != null) {
            rootContainer.getRootPane().repaint(r);
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(canvasElement.getRealWidth(), canvasElement.getRealHeight());
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(canvasElement.getRealWidth(), canvasElement.getRealHeight());
    }

    @Override
    public boolean isOptimizedDrawingEnabled() {
        return false;
    }

    @Override
    public Object getComponent() {
        return this;
    }
}
