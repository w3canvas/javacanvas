package com.w3canvas.javacanvas.js;

import com.w3canvas.javacanvas.js.impl.node.TextMetrics;


public interface CanvasText {

	/**
	 * @return font
	 */
	public String jsGet_font();

	/**
	 * simpleCSS font parsing "font size"_"font family"
	 * default : 10px sans-serif
	 *
	 * @param font
	 */
	public void jsSet_font(String font);

	/**
	 * @return textAlign
	 */
	public String jsGet_textAlign();

	/**
	 * "start", "end", "left", "right", "center" (default: "start")
	 *
	 * @param textAlign
	 */
	public void jsSet_textAlign(String textAlign);

	/**
	 * @return textBaseline
	 */
	public String jsGet_textBaseline();

	/**
	 * "top", "hanging", "middle", "alphabetic", "ideographic", "bottom" (default: "alphabetic")
	 *
	 * @param textBaseline
	 */
	public void jsSet_textBaseline(String textBaseline);

	/**
	 * @param text
	 * @param x
	 * @param y
	 * @param maxWidth
	 */
	public void jsFunction_fillText(String text, Double x, Double y, int maxWidth);

	/**
	 * @param text
	 * @param x
	 * @param y
	 * @param maxWidth
	 */
	public void jsFunction_strokeText(String text, Double x, Double y, int maxWidth);

	/**
	 * @param text
	 * @return TextMetrics
	 */
	public TextMetrics jsFunction_measureText(String text);

}
