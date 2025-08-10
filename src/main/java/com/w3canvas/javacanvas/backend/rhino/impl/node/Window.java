package com.w3canvas.javacanvas.backend.rhino.impl.node;

@SuppressWarnings("serial")
public class Window extends Node {

	private int innerWidth, innerHeight;

	private String name;

	private String location;

	private Document document;
	private static Window instance = null;

	/**
	 * must remain public - instantiated through reflection
	 */
	public Window() {
	}

	public void initInstance(int wide, int high) {
		innerHeight = high;
		innerWidth = wide;
		instance = this;
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

	public static Window getInstance() {
		return instance;
	}

}
