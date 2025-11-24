package com.w3canvas.javacanvas.interfaces;

/**
 * Interface for the native UI component that displays the canvas content.
 * Abstracts away the specific UI toolkit (Swing/AWT vs JavaFX).
 */
public interface ICanvasPeer {
    /**
     * Set the bounds of the peer component.
     * 
     * @param x      x coordinate
     * @param y      y coordinate
     * @param width  width
     * @param height height
     */
    void setBounds(int x, int y, int width, int height);

    /**
     * Set the visibility of the peer component.
     * 
     * @param visible true to show, false to hide
     */
    void setVisible(boolean visible);

    /**
     * Request a repaint of the peer component.
     */
    void repaint();

    /**
     * Get the underlying UI component.
     * 
     * @return The native component (e.g. JPanel or javafx.scene.Node)
     */
    Object getComponent();
}
