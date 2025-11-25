package com.w3canvas.javacanvas.rt;

import javax.swing.SwingUtilities;

public class SwingUIToolkit implements IUIToolkit {
    @Override
    public void invokeLater(Runnable r) {
        SwingUtilities.invokeLater(r);
    }
}
