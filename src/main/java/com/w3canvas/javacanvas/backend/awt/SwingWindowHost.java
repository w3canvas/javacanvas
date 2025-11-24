package com.w3canvas.javacanvas.backend.awt;

import com.w3canvas.javacanvas.interfaces.IWindowHost;
import javax.swing.RootPaneContainer;
import java.awt.Component;

public class SwingWindowHost implements IWindowHost {
    private final RootPaneContainer container;

    public SwingWindowHost(RootPaneContainer container) {
        this.container = container;
    }

    @Override
    public int getWidth() {
        if (container instanceof Component) {
            return ((Component) container).getWidth();
        }
        return 0;
    }

    @Override
    public int getHeight() {
        if (container instanceof Component) {
            return ((Component) container).getHeight();
        }
        return 0;
    }

    @Override
    public void repaint() {
        if (container instanceof Component) {
            ((Component) container).repaint();
        }
    }

    @Override
    public void addComponent(Object component) {
        if (component instanceof Component) {
            container.getRootPane().add((Component) component);
        } else {
            throw new IllegalArgumentException("Component must be an instance of java.awt.Component");
        }
    }

    @Override
    public void validate() {
        if (container instanceof Component) {
            ((Component) container).validate();
        }
    }

    public RootPaneContainer getContainer() {
        return container;
    }
}
