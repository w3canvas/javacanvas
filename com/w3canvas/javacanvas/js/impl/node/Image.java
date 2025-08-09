package com.w3canvas.javacanvas.js.impl.node;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;

import javax.imageio.ImageIO;

import com.w3canvas.javacanvas.Base64;
import com.w3canvas.javacanvas.rt.RhinoRuntime;
import com.w3canvas.javacanvas.utils.RhinoCanvasUtils;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;

@SuppressWarnings("serial")
public class Image extends Node {

	private static final String NODE_NAME = "img";
	private HTMLCanvasElement owner;
	private BufferedImage image;
	private String src;
	private Function onload;
	private boolean doNotify;

	public Image() {
		this(100, 100);
	}

	public Image(int width, int height) {
		initImage(width, height);
	}

	public void jsConstructor() {
		initImage(100, 100);
	}

	private void initImage(int width, int height) {
		jsGet_style().put("width", width);
		jsGet_style().put("height", height);
		jsGet_style().put("left", 0);
		jsGet_style().put("top", 0);

		createImg();
	}

	private void createImg() {
		int width = RhinoCanvasUtils.getIntValue(this, "jsGet_width");
		int height = RhinoCanvasUtils.getIntValue(this, "jsGet_height");

		if (width > 0 && height > 0) {
			if (image != null) {
				BufferedImage oldImage = image;
				image = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
				Graphics g = image.getGraphics();
				g.drawImage(oldImage, 0, 0, null);
				g.dispose();

				if (this instanceof HTMLCanvasElement && ((HTMLCanvasElement) this).getCanvas() != null) {
					((HTMLCanvasElement) this).getCanvas().initCanvas((HTMLCanvasElement) this);
				}
			} else {
				image = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
			}
		}
	}

	private BufferedImage base64ToImage(String base64string) {
		BufferedImage bufImage = null;

		try {
			bufImage = ImageIO.read(new ByteArrayInputStream(Base64.decode(base64string)));
		} catch (IOException e) {
			e.printStackTrace();
		}

		return bufImage;
	}

	private String getImgEncodedString(String encodedString) {
		String result = "";
		if (encodedString.startsWith("data:image")) {
			String arr[] = encodedString.split("base64,");

			if (arr.length == 2) {
				result = arr[1];
			}
		}

		return result;
	}

	public String jsGet_src() {
		return src;
	}

	public void jsSet_src(String url) {
		BufferedImage bufImage;
		this.src = url;

		try {
			RhinoRuntime runtime = (RhinoRuntime) Context.getCurrentContext().getThreadLocal("runtime");
			Scriptable scope = runtime.getScope();
			String base = (String) scope.get("documentBase", scope);

			if (url.startsWith("data:image")) {
				bufImage = base64ToImage(getImgEncodedString(url));
			} else if (url.startsWith("file:/")) {
				bufImage = ImageIO.read(new URI(url).toURL());
			} else {
				URI baseUri = new URI(base);
				bufImage = ImageIO.read(baseUri.resolve(url).toURL());
			}

			if (bufImage != null && (bufImage.getWidth() != getRealWidth() || bufImage.getHeight() != getRealHeight())) {
				setImgParams(bufImage);
				image = bufImage;
			}

			if (onload != null) {
				onload.call(Context.getCurrentContext(), onload, onload, new Object[0]);
			} else {
				doNotify = true;
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void setImgParams(BufferedImage bufImage) {
		jsGet_style().put("width", bufImage.getWidth());
		jsGet_style().put("height", bufImage.getHeight());

		createImg();
	}

	public Function jsGet_onload() {
		return onload;
	}

	public void jsSet_onload(Function onload) {
		this.onload = onload;

		if (doNotify) {
			doNotify = false;
			onload.call(Context.getCurrentContext(), onload, onload, null);
		}
	}

	@Override
	protected void onResize() {
		createImg();
	}

	public Integer getRealWidth() {
		return image.getWidth();
	}

	public Integer getRealHeight() {
		return image.getHeight();
	}

	public void setOwner(HTMLCanvasElement owner) {
		this.owner = owner;
	}

	@Override
	public HTMLCanvasElement getOwner() {
		return this.owner;
	}

	void dirty() {
		if (owner != null) {
			owner.getNodePanel().repaint();
		}
	}

	@Override
	public String getNodeName() {
		return NODE_NAME;
	}

	public BufferedImage getImage() {
		return image;
	}

}