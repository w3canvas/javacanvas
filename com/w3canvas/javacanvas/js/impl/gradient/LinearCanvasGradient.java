package com.w3canvas.javacanvas.js.impl.gradient;

import java.awt.Color;
import java.awt.LinearGradientPaint;
import java.awt.Paint;

@SuppressWarnings("serial")
public class LinearCanvasGradient extends CanvasGradient {

	// accessed via reflection
	public LinearCanvasGradient() {
	}

	// accessed via reflection
	public LinearCanvasGradient(Double x1, Double y1, Double x2, Double y2) {
		this.setX1(x1);
		this.setY1(y1);
		this.setX2(x2);
		this.setY2(y2);
	}

	@Override
	public void jsFunction_addColorStop(Double where, String color) {
		addColorStop(where, color);
	}

	@Override
	protected Paint createGradientPaint(float[] where, Color[] color) {
		return new LinearGradientPaint(getX1().floatValue(), getY1()
				.floatValue(), getX2().floatValue(), getY2().floatValue(),
				where, color);
	}
}
