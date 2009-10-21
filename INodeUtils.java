package com.w3canvas.javacanvas.js;

import java.awt.Point;

public interface INodeUtils {

	/**
	 * jsSet_left() wrapper
	 *
	 * @param left
	 */
	public void setLeft(Integer left);

	/**
	 * jsGet_left() wrapper
	 *
	 * @return left value - relatively to position
	 */
	public Integer getLeft();

	/**
	 * @return left value - absolute value
	 */
	public Integer getRealLeft();

	/**
	 * jsSet_Top() wrapper
	 *
	 * @param top
	 */
	public void setTop(Integer top);

	/**
	 * jsGet_Top() wrapper
	 *
	 * @return top value - relatively to position
	 */
	public Integer getTop();

	/**
	 * @return top value - absolute value
	 */
	public Integer getRealTop();

	/**
	 * jsGet_Width() wrapper
	 *
	 * @param width
	 */
	public void setWidth(Integer width);

	/**
	 * jsSet_Width() wrapper
	 *
	 * @return node width
	 */
	public Integer getWidth();

	/**
	 * jsSet_Height() wrapper
	 *
	 * @param height
	 */
	public void setHeight(Integer height);

	/**
	 * jsGet_Height() wrapper
	 *
	 * @return node height
	 */
	public Integer getHeight();

	/**
	 * check CSS display attribute
	 *
	 * @return is displayed
	 */
	boolean isDisplayed();

	/**
	 * check visible CSS attribute
	 *
	 * @return is visible
	 */
	boolean isVisible();

	boolean isMineXYArea(Point xy);

	/**
	 * check node for attributes display, visible
	 *
	 * @return boolean
	 */
	boolean isNodeVisible();

	boolean isMineArea(Point xy);
}
