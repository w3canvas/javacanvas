package com.w3canvas.javacanvas.backend.rhino.impl.node;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.RootPaneContainer;

import com.w3canvas.javacanvas.Base64;
import com.w3canvas.javacanvas.js.ICanvas;
import com.w3canvas.javacanvas.js.IObserver;
import com.w3canvas.javacanvas.backend.rhino.impl.event.CSSAttribute;
import com.w3canvas.javacanvas.utils.RhinoCanvasUtils;
import com.w3canvas.javacanvas.interfaces.ICanvasPeer;
import com.w3canvas.javacanvas.interfaces.IWindowHost;

import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import com.w3canvas.javacanvas.backend.awt.AwtGraphicsBackend;
import com.w3canvas.javacanvas.backend.javafx.JavaFXGraphicsBackend;
import com.w3canvas.javacanvas.core.CoreCanvasRenderingContext2D;
import com.w3canvas.javacanvas.interfaces.ICanvasRenderingContext2D;
import com.w3canvas.javacanvas.interfaces.IGraphicsBackend;

@SuppressWarnings("serial")
public class HTMLCanvasElement extends Image implements IObserver, ICanvas {

	private static final Map<String, String> FORMATS = new HashMap<String, String>();

	private static final Integer CANVAS_WIDTH = 300;

	private static final Integer CANVAS_HEIGHT = 150;

	private final static String NODE_NAME = "canvas";

	static {
		FORMATS.put("image/png", "png");
		FORMATS.put("image/jpeg", "jpeg");
		FORMATS.put("image/x-png", "png");
		FORMATS.put("image/gif", "gif");
		FORMATS.put("image/svg+xml", "svg");
	}

	private ICanvasPeer peer;
	private IWindowHost windowHost;
	private CanvasRenderingContext2D canvas;

	private static final Color WHITE_TRANSPARENT = new Color(255, 255, 255, 0);
	private static final Color BLACK_TRANSPARENT = new Color(0, 0, 0, 0);

	@Override
	public Object getNodePanel() {
		return peer != null ? peer.getComponent() : null;
	}

	public HTMLCanvasElement() {
		super(CANVAS_WIDTH, CANVAS_HEIGHT);
	}

	@Override
	protected void init() {
		super.init();
		windowHost = this.document.getWindowHost();

		// Only create peer if we have a window host (not headless/pure logic)
		if (windowHost != null) {
			createPeer();
			if (peer != null) {
				setOwner(this);
				windowHost.addComponent(peer.getComponent());
				peer.setBounds(0, 0, getWidth(), getHeight());
				windowHost.validate();
				windowHost.repaint();
			}
		}

		jsGet_style().registerObserver(this, CSSAttribute.Z_ORDER);
		jsGet_style().registerObserver(this, CSSAttribute.DISPLAY);
	}

	private void createPeer() {
		// Factory logic for creating the peer
		// For now, we default to Swing if the host is a SwingWindowHost
		if (windowHost instanceof com.w3canvas.javacanvas.backend.awt.SwingWindowHost) {
			javax.swing.RootPaneContainer rpc = ((com.w3canvas.javacanvas.backend.awt.SwingWindowHost) windowHost)
					.getContainer();
			this.peer = new com.w3canvas.javacanvas.backend.awt.SwingCanvasPeer(this, rpc);
		}
		// Future: Add JavaFX peer creation here
	}

	public Scriptable jsFunction_getContext(String param) {
		if (canvas == null) {
			String backendName = System.getProperty("w3canvas.backend", "awt");
			IGraphicsBackend backend;
			if ("javafx".equalsIgnoreCase(backendName)) {
				backend = new JavaFXGraphicsBackend();
			} else {
				backend = new AwtGraphicsBackend();
			}

			// 2. Create the core rendering context, providing the backend and canvas
			// dimensions
			ICanvasRenderingContext2D coreContext = new CoreCanvasRenderingContext2D(getDocument(), backend, getWidth(),
					getHeight());

			// 3. Create the Rhino adapter, passing the core context to it
			canvas = new CanvasRenderingContext2D();
			canvas.init(coreContext);
			canvas.reset();

			// 4. Initialize the Scriptable parts of the adapter
			canvas.setParentScope(this.getParentScope());
			canvas.setPrototype(ScriptableObject.getClassPrototype(this.getParentScope(), "CanvasRenderingContext2D"));

			// 5. Initialize the canvas reference for dirty() calls
			canvas.initCanvas((ICanvas) this);
		}

		return canvas;
	}

	@Override
	protected void onResize() {
		super.onResize();

		if (peer == null)
			return;

		int width = RhinoCanvasUtils.getIntValue(this, "jsGet_width");
		int height = RhinoCanvasUtils.getIntValue(this, "jsGet_height");
		int left = RhinoCanvasUtils.getIntValue(this, "jsGet_left");
		int top = RhinoCanvasUtils.getIntValue(this, "jsGet_top");

		peer.setBounds(left, top, width, height);
	}

	public String jsFunction_toDataURL(String mimeType) throws Exception {
		return toDataURL(mimeType, getImage());
	}

	public static String toDataURL(String mimeType, BufferedImage image) throws Exception {
		String outputMimeFormat = "image/png";
		String outputFormat = "";
		StringBuffer outputData = new StringBuffer();

		mimeType = mimeType.toLowerCase();

		if (!mimeType.isEmpty() && Arrays.asList(ImageIO.getWriterMIMETypes()).contains(mimeType)) {
			outputMimeFormat = mimeType;
		}

		outputFormat = FORMATS.get(outputMimeFormat);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();

		try {
			if (ImageIO.write(image, outputFormat, bos)) {
				outputData.append("data:").append(outputMimeFormat).append(";base64,").append(
						Base64.encodeBytes(bos.toByteArray()));
			}
		} finally {
			try {
				bos.close();
			} catch (IOException e) {
			}
		}

		return outputData.toString();

	}

	private void setDisplayPref(String value) {
		boolean visible = true;

		if ("none".equalsIgnoreCase(value)) {
			visible = false;
		} else if ("visible".equalsIgnoreCase(value)) {
			visible = true;
		}

		if (peer != null) {
			peer.setVisible(visible);
		}
	}

	@Override
	public void notifyMe(CustomEvent event) {
		Object val = event.getValue();

		switch (event.getEventType()) {
			case Z_ORDER:
				if (val instanceof String) {
					setZOrderingPref(Integer.valueOf((String) val));
				} else if (val instanceof Integer) {
					setZOrderingPref((Integer) val);
				}
				break;
			case DISPLAY:
				if (val instanceof String) {
					setDisplayPref((String) val);
				}
		}
	}

	@Override
	public String getNodeName() {
		return NODE_NAME;
	}

	public CanvasRenderingContext2D getCanvas() {
		return canvas;
	}

	public Integer getWidth() {
		return getRealWidth();
	}

	public Integer getHeight() {
		return getRealHeight();
	}
}