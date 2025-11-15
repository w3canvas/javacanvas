package com.w3canvas.javacanvas.backend.rhino.impl.node;

import com.w3canvas.javacanvas.interfaces.IImageData;
import com.w3canvas.javacanvas.interfaces.IImageBitmap;
import org.mozilla.javascript.ScriptableObject;

@SuppressWarnings("serial")
public class Window extends Node {

	private int innerWidth, innerHeight;

	private String name;

	private String location;

	private Document document;

	public Window() {
		// No-arg constructor for Rhino
	}

	public void init(int wide, int high) {
		this.innerHeight = high;
		this.innerWidth = wide;
	}

	public void setSize(int wide, int high) {
		innerHeight = high;
		innerWidth = wide;
	}

	public Document jsGet_document() {
		return document;
	}

	public int jsGet_innerWidth() {
		return innerWidth;
	}

	public void jsSet_innerWidth(int innerWidth) {
		this.innerWidth = innerWidth;
	}

	public int jsGet_innerHeight() {
		return innerHeight;
	}

	public void jsSet_innerHeight(int innerHeight) {
		this.innerHeight = innerHeight;
	}

	public String jsGet_name() {
		return name;
	}

	public void jsSet_name(String name) {
		this.name = name;
	}

	public String jsGet_location() {
		return location;
	}

	public void jsSet_location(String location) {
		this.location = location;
	}

	public void setDocument(Document document) {
		this.document = document;
		jsFunction_appendChild(document);
	}

	/**
	 * Creates an ImageBitmap from various image sources.
	 *
	 * This implements the HTML5 Canvas createImageBitmap() global function.
	 * In the standard, this returns a Promise, but for simplicity in this
	 * Java implementation, it returns the ImageBitmap directly.
	 *
	 * @param image the image source (HTMLCanvasElement, HTMLImageElement, ImageData, or ImageBitmap)
	 * @return a new ImageBitmap object
	 * @throws Exception if the image source is invalid or null
	 */
	public ImageBitmap jsFunction_createImageBitmap(Object image) throws Exception {
		if (image == null) {
			throw new IllegalArgumentException("Image source cannot be null");
		}

		IImageBitmap coreBitmap = null;

		// Handle different image source types
		if (image instanceof HTMLCanvasElement) {
			coreBitmap = new com.w3canvas.javacanvas.core.ImageBitmap((HTMLCanvasElement) image);
		} else if (image instanceof Image) {
			coreBitmap = new com.w3canvas.javacanvas.core.ImageBitmap((Image) image);
		} else if (image instanceof IImageData) {
			coreBitmap = new com.w3canvas.javacanvas.core.ImageBitmap((IImageData) image);
		} else if (image instanceof IImageBitmap) {
			coreBitmap = new com.w3canvas.javacanvas.core.ImageBitmap((IImageBitmap) image);
		} else {
			throw new IllegalArgumentException("Unsupported image source type: " + image.getClass().getName());
		}

		// Create Rhino wrapper
		ImageBitmap rhinoBitmap = new ImageBitmap();
		rhinoBitmap.init(coreBitmap);
		rhinoBitmap.setParentScope(this.getParentScope());
		rhinoBitmap.setPrototype(ScriptableObject.getClassPrototype(this.getParentScope(), "ImageBitmap"));

		return rhinoBitmap;
	}
}
