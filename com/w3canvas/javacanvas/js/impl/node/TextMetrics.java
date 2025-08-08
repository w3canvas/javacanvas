package com.w3canvas.javacanvas.js.impl.node;

@SuppressWarnings("serial")
public class TextMetrics extends ProjectScriptableObject {

	private Double width;

	public TextMetrics() {
	}

	public TextMetrics(Double width) {
		this.width = width;
	}

	public Double jsGet_width() {
		return width;
	}

}
