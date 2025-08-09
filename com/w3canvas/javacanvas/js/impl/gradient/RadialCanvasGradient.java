package com.w3canvas.javacanvas.js.impl.gradient;

import java.awt.Color;
import java.awt.Paint;
import java.awt.RadialGradientPaint;
import java.awt.MultipleGradientPaint.CycleMethod;

@SuppressWarnings("serial")
public class RadialCanvasGradient extends CanvasGradient {

	// accessed through reflection
	public RadialCanvasGradient() {
	}

	// accessed through reflection
	public RadialCanvasGradient(Double x1, Double y1, Double r1, Double x2,
			Double y2, Double r2) {
		this.setX1(x1);
		this.setY1(y1);
		this.setX2(x2);
		this.setY2(y2);
		this.setR1(r1);
		this.setR2(r2);
	}

	@Override
	public void jsFunction_addColorStop(Double where, String color) {
		addColorStop(where, color);
	}

	@Override
	protected Paint createGradientPaint(float[] where, Color[] color) {
		return new RadialGradientPaint(getX2().floatValue(), getY2()
				.floatValue(), getR2().floatValue(), getX1().floatValue(),
				getY1().floatValue(), where, color, CycleMethod.NO_CYCLE);
	}
}
