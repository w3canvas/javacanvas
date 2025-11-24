package com.w3canvas.javacanvas.interfaces;

/**
 * Interface for the host window/container that holds the canvas.
 * Abstracts away the specific UI toolkit (Swing/AWT vs JavaFX).
 */
public interface IWindowHost {
    /**
     * Get the width of the host window.
     * 
     * @return width in pixels
     */
    int getWidth();

    /**
     * Get the height of the host window.
     * 
     * @return height in pixels
     */
    int getHeight();

    /**
     * Request a repaint of the host window.
     */
    void repaint();

    /**
     * Add a component to the host window.
     * 
     * @param component The component to add (must be compatible with the underlying
     *                  toolkit)
     */
    void addComponent(Object component);

    /**
     * Validate the layout of the host window.
     */
    void validate();
}
