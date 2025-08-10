package com.w3canvas.javacanvas.test;

import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(ApplicationExtension.class)
public class TestJavaFX {

    private Scene scene;

    @Start
    private void start(Stage stage) {
        Rectangle rect = new Rectangle(100, 100);
        StackPane root = new StackPane(rect);
        scene = new Scene(root, 200, 200);
        stage.setScene(scene);
        stage.show();
    }

    @Test
    void should_create_scene() {
        assertNotNull(scene);
    }
}
