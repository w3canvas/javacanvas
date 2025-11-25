package com.w3canvas.javacanvas.backend.javafx;

import com.w3canvas.javacanvas.rt.IUIToolkit;
import javafx.application.Platform;

public class JavaFXUIToolkit implements IUIToolkit {
    @Override
    public void invokeLater(Runnable r) {
        Platform.runLater(r);
    }
}
