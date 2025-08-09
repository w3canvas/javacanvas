package com.w3canvas.javacanvas.js.impl.node;

import java.awt.Color;

import com.w3canvas.javacanvas.utils.RhinoCanvasUtils;

import org.mozilla.javascript.Scriptable;

public class CanvasPixelArray extends ProjectScriptableObject {

	private Integer width;
	private Integer height;
	private int pixelsArray[];
	private static final Color BLACK_TRANSPARENT = new Color(0, 0, 0, 255);

	// accessed via reflection
	public CanvasPixelArray() {
		this(0, 0);
	}

	// accessed via reflection
	public CanvasPixelArray(Integer width, Integer height) {
		this.width = width;
		this.height = height;
		this.pixelsArray = new int[getPxArraySize()];
		initialPreFill();
	}

	private void initialPreFill() {
		for (int i = 0; i < pixelsArray.length; i += 4) {
			pixelsArray[i] = BLACK_TRANSPARENT.getRed();
			pixelsArray[i + 1] = BLACK_TRANSPARENT.getGreen();
			pixelsArray[i + 2] = BLACK_TRANSPARENT.getBlue();
			pixelsArray[i + 3] = BLACK_TRANSPARENT.getAlpha();
		}
	}

	public static CanvasPixelArray getInstance(int[] grabbedPixels,
			Integer width, Integer height) {

		CanvasPixelArray pixelArray = RhinoCanvasUtils.getScriptableInstance(
				CanvasPixelArray.class, new Object[] { width, height });

		int[] pxArray = pixelArray.pixelsArray;

		for (int j = 0; j < height; j++) {
			for (int i = 0; i < width; i++) {
				int pixel = grabbedPixels[j * width + i];
				int pxPointer = (j * width + i) * 4;

				pxArray[pxPointer] = ((pixel >> 16) & 0x000000FF); // red
				pxArray[pxPointer + 1] = ((pixel >> 8) & 0x000000FF); // green
				pxArray[pxPointer + 2] = ((pixel) & 0x00ff); // blue
				pxArray[pxPointer + 3] = ((pixel >> 24) & 0x000000FF); // alpha

			}
		}

		return pixelArray;
	}

	public int[] getPixels(int dirtyX, int dirtyY, int dirtyWidth,
			int dirtyHeight) {
		int[] pxArray = new int[dirtyWidth * dirtyHeight];

		for (int j = dirtyY; j < dirtyHeight; j++) {
			for (int i = dirtyX; i < dirtyWidth; i++) {
				int pxPointer = (j * width + i) * 4;
				pxArray[j * dirtyWidth + i] = pixelsArray[pxPointer] << 16
						| pixelsArray[pxPointer + 1] << 8
						| pixelsArray[pxPointer + 2]
						| pixelsArray[pxPointer + 3] << 24;
			}
		}

		return pxArray;
	}

	public Object get(int index, Scriptable start) {
		Object result = null;

		if (start instanceof CanvasPixelArray) {
			result = ((CanvasPixelArray) start).pixelsArray[index] & 0xFF;
		}

		return result;
	}

	public void put(int index, Scriptable start, Object value) {
		int result = 0;
		if (value instanceof Double) {
			result = (((Double) value).intValue() & 0xFF);
		} else if (value instanceof Integer) {
			result = ((Integer) value & 0xFF);
		}
		((CanvasPixelArray) start).pixelsArray[index] = result;
	}

	public int jsGet_length() {
		return getPxArraySize();
	}

	private int getPxArraySize() {
		return width * height * 4;
	}

	public Integer getWidth() {
		return width;
	}

	public Integer getHeight() {
		return height;
	}

}
