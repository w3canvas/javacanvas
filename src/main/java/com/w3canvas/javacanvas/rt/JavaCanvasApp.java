package com.w3canvas.javacanvas.rt;

import javax.swing.JFrame;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Properties;

import com.w3canvas.javacanvas.backend.rhino.impl.event.JSMouseEvent;
import com.w3canvas.javacanvas.backend.rhino.impl.node.Document;
import com.w3canvas.javacanvas.backend.rhino.impl.node.Node;
import com.w3canvas.javacanvas.backend.rhino.impl.node.Window;
import com.w3canvas.javacanvas.utils.PropertiesHolder;
import org.mozilla.javascript.Context;

public class JavaCanvasApp {

    private final JavaCanvas canvas;
    private final JFrame frame;

    public JavaCanvasApp(String title, JavaCanvas canvas) {
        this.canvas = canvas;
        this.frame = new JFrame(title);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.addWindowListener(new DemoWindowAdapter(canvas, frame));
        Container contentPane = frame.getContentPane();
        contentPane.addMouseListener(new CanvasMouseListener(canvas));
        contentPane.addMouseMotionListener(new CanvasMouseMotionListener(canvas));
        contentPane.addComponentListener(new CanvasComponentListener(contentPane, canvas));

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setSize(screenSize.width - 200, screenSize.height - 100);
        frame.setLocation(200, 0);
    }

    public void setVisible(boolean visible) {
        frame.setVisible(visible);
    }

    public JFrame getFrame() {
        return frame;
    }

    private static class DemoWindowAdapter extends WindowAdapter {
        private final JavaCanvas canvas;
        private final JFrame frame;

        private DemoWindowAdapter(JavaCanvas canvas, JFrame frame) {
            this.canvas = canvas;
            this.frame = frame;
        }

        public void windowClosing(WindowEvent e) {
            canvas.getWindow().callCloseFunction();
            Context context = Context.getCurrentContext();
            if (context != null) {
                Context.exit();
            }
            System.exit(0);
        }

        public void windowOpened(WindowEvent e) {
            canvas.init(frame.getContentPane());
            canvas.getWindow().callLoadFunction();
        }
    }

    private static class CanvasMouseListener extends MouseAdapter {
        private final JavaCanvas canvas;

        public CanvasMouseListener(JavaCanvas canvas) {
            this.canvas = canvas;
        }

        protected Node getDestinationNode(JSMouseEvent mouseEvent) {
            return canvas.getDocument().getEventDestination(new Point(mouseEvent.jsGet_clientX(), mouseEvent.jsGet_clientY()));
        }

        public void mouseClicked(MouseEvent e) {
            JSMouseEvent mouseEvent = JSMouseEvent.convert(e);
            Node dstNode = getDestinationNode(mouseEvent);
            if (e.getClickCount() == 2) {
                dstNode.callDoubleclickFunction(mouseEvent);
            } else if (e.getClickCount() == 1) {
                dstNode.callClickFunction(mouseEvent);
            }
        }

        public void mousePressed(MouseEvent e) {
            JSMouseEvent mouseEvent = JSMouseEvent.convert(e);
            Node dstNode = getDestinationNode(mouseEvent);
            dstNode.callMousedownFunction(mouseEvent);
        }

        public void mouseReleased(MouseEvent e) {
            JSMouseEvent mouseEvent = JSMouseEvent.convert(e);
            Node dstNode = getDestinationNode(mouseEvent);
            dstNode.callMouseupFunction(JSMouseEvent.convert(e));
        }
    }

    private static class CanvasMouseMotionListener extends MouseAdapter {
        private final JavaCanvas canvas;

        public CanvasMouseMotionListener(JavaCanvas canvas) {
            this.canvas = canvas;
        }

        public void mouseMoved(MouseEvent e) {
            canvas.getDocument().callMousemoveFunction(JSMouseEvent.convert(e));
        }
    }

    private static class CanvasComponentListener extends ComponentAdapter {
        private Container contentPane;
        private JavaCanvas canvas;

        private CanvasComponentListener(Container contentPane, JavaCanvas canvas) {
            this.contentPane = contentPane;
            this.canvas = canvas;
        }

        public void componentResized(ComponentEvent e) {
            super.componentResized(e);
            Window w = canvas.getWindow();
            if (w != null) {
                w.setSize(contentPane.getWidth(), contentPane.getHeight());
                w.callResizeFunction();
            }
        }
    }

    public static void main(String[] args) {
        PropertiesHolder pHolder = new PropertiesHolder();
        pHolder.processCommandLineParams(args);
        JavaCanvas canvas = new JavaCanvas(pHolder.getBaseDir(), false);
        JavaCanvasApp app = new JavaCanvasApp(pHolder.getAppTitle(), canvas);
        app.setVisible(true);
    }
}
