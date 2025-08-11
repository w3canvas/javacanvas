package com.w3canvas.javacanvas.backend.rhino.impl.node;

import java.awt.Point;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.RootPaneContainer;

import com.w3canvas.javacanvas.exception.IllegalNodeException;
import com.w3canvas.javacanvas.js.AbstractView;

@SuppressWarnings("serial")
public class Document extends Node {

	private RootPaneContainer frame;
	private Map<String, Node> documentsNode = new HashMap<String, Node>();
	private final static String NODE_NAME = "body";

	public Document() {
		// No-arg constructor for Rhino
	}

	public void init(RootPaneContainer frame) {
		this.frame = frame;
		this.setDocument(this);
	}

	public Node jsFunction_getElementById(String id) {
		return documentsNode.get(id);
	}

	public void removeElementById(String id) {
		if (documentsNode != null) {
			documentsNode.remove(id);
		}
	}

	public void addElement(String id, Node nodeItem) {
		documentsNode.put(id, nodeItem);
	}

	public Node jsFunction_createElement(String nodeType) throws IllegalNodeException {

		NodeType itemNodeType = NodeType.getNodeTypeByName(nodeType);

		if (itemNodeType == null) {
			throw new IllegalNodeException("Illegal node type : " + nodeType);
		}

		Node node = itemNodeType.getNode();
		node.setDocument(this);
		node.init();

		return node;
	}

	public RootPaneContainer getContentPane() {
		return frame;
	}

	public AbstractView jsGet_defaultView() {
		return null;
	}

	@Override
	public String getNodeName() {
		return NODE_NAME;
	}

	/**
	 * return node - event destination
	 *
	 * @param xy
	 * @return highest node in zIndex by point
	 */
	public Node getEventDestination(Point xy) {
		Map<String, Node> nodes = getZNodes().descendingMap();
		Node destinationNode = this;

		for(Entry<String, Node> entry : nodes.entrySet()){
			if (entry.getValue().isMineArea(xy)) {
				destinationNode = entry.getValue();
				break;
			}
		}

		return destinationNode;
	}

	@Override
	protected void appendToParent(Node parent) {
		setParentNode(parent);

		if (parent instanceof Window) {
			jsGet_style().put("width", ((Window) parent).jsGet_innerWidth());
			jsGet_style().put("height", ((Window) parent).jsGet_innerHeight());
		}
	}
}
