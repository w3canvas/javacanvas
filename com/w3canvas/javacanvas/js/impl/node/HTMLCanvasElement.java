package com.w3canvas.javacanvas.js.impl.node;

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
import com.w3canvas.javacanvas.js.IObserver;
import com.w3canvas.javacanvas.js.impl.event.CSSAttribute;
import com.w3canvas.javacanvas.utils.RhinoCanvasUtils;

import org.mozilla.javascript.Scriptable;

@SuppressWarnings("serial")
public class HTMLCanvasElement extends Image implements IObserver {

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

	private Helper helper;

	private RootPaneContainer container;

	private CanvasRenderingContext2D canvas;

	private static final Color WHITE_TRANSPARENT = new Color(255, 255, 255, 0);

	private static final Color BLACK_TRANSPARENT = new Color(0, 0, 0, 0);

	@Override
	public JPanel getNodePanel() {
		return helper;
	}

	private class Helper extends JPanel // implements Comparable
	{

		@Override
		public void paint(Graphics g) {
			g.setColor(BLACK_TRANSPARENT);
			g.fillRect(0, 0, getWidth(), getHeight());
			g.drawImage(getImage(), 0, 0, (ImageObserver) container);
		}

		@Override
		public void repaint(long tm, int x, int y, int width, int height) {
			container.getRootPane().repaint(tm, x, y, width, height);
		}

		@Override
		public void repaint(Rectangle r) {
			container.getRootPane().repaint(r);
		}

		@Override
		public Dimension getPreferredSize() {
			return new Dimension(getRealWidth(), getRealHeight());
		}

		@Override
		public Dimension getMinimumSize() {
			return new Dimension(getRealWidth(), getRealHeight());
		}

		@Override
		public boolean isOptimizedDrawingEnabled() {
			return false;
		}

	}

	/**
	 * accessed! do not make private! 300x150 by spec.
	 */
	public HTMLCanvasElement() {
		this(CANVAS_WIDTH, CANVAS_HEIGHT);
	}

	private HTMLCanvasElement(int width, int height) {
		super(width, height);

		container = Document.getInstance().getContentPane();
		helper = new Helper();

		helper.setDoubleBuffered(true);

		setOwner(this);

		container.getRootPane().add(helper); // define layers here
		helper.setBounds(0, 0, width, height);
		container.getRootPane().validate();
		container.getRootPane().repaint();

		jsGet_style().registerObserver(this, CSSAttribute.Z_ORDER);
		jsGet_style().registerObserver(this, CSSAttribute.DISPLAY);
	}

	public Scriptable jsFunction_getContext(String param) {
		if (canvas == null) {
			canvas = RhinoCanvasUtils.getScriptableInstance(CanvasRenderingContext2D.class, null);
			canvas.initCanvas(this);
		}

		return canvas;
	}

	@Override
	protected void onResize() {
		super.onResize();

		int width = RhinoCanvasUtils.getIntValue(this, "jsGet_width");
		int height = RhinoCanvasUtils.getIntValue(this, "jsGet_height");
		int left = RhinoCanvasUtils.getIntValue(this, "jsGet_left");
		int top = RhinoCanvasUtils.getIntValue(this, "jsGet_top");

		helper.setBounds(left, top, width, height);
		// container.getRootPane().validate();
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

		helper.setVisible(visible);
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
}