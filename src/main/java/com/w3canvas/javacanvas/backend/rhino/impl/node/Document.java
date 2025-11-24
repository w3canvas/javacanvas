package com.w3canvas.javacanvas.backend.rhino.impl.node;

import java.awt.Point;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.RootPaneContainer;

import com.w3canvas.javacanvas.interfaces.IWindowHost;
import com.w3canvas.javacanvas.interfaces.IElement;
import com.w3canvas.javacanvas.interfaces.INode;
import com.w3canvas.javacanvas.core.dom.CoreDocument;
import com.w3canvas.javacanvas.core.dom.CoreElement;
import com.w3canvas.javacanvas.exception.IllegalNodeException;
import com.w3canvas.javacanvas.js.AbstractView;

import com.w3canvas.javacanvas.backend.rhino.impl.font.RhinoFontFaceSet;
import com.w3canvas.javacanvas.dom.FontFaceSet;

/**
 * Rhino adapter for Document - wraps CoreDocument.
 *
 * This class is a thin Rhino-specific wrapper around CoreDocument. The CoreDocument
 * instance is NOT Context-bound and can be safely accessed from multiple threads
 * and multiple Rhino Contexts. This solves the thread-locality issue for Worker/
 * SharedWorker communication.
 *
 * All DOM operations are delegated to the wrapped CoreDocument.
 */
@SuppressWarnings("serial")
public class Document extends Node {

	// Core DOM implementation (NOT Context-bound, thread-safe)
	private final CoreDocument coreDocument;

	private IWindowHost windowHost;
	private Map<String, Node> documentsNode = new HashMap<String, Node>();
	private final static String NODE_NAME = "body";
	private RhinoFontFaceSet fonts;

	public Document() {
		// No-arg constructor for Rhino
		// Create the core document (NOT Context-bound)
		this.coreDocument = new CoreDocument();

		// Set up element factory to create Rhino-wrapped elements
		this.coreDocument.setElementFactory(new CoreDocument.ElementFactory() {
			@Override
			public IElement createElementWrapper(CoreElement coreElement, String tagName) {
				// Create Rhino wrapper based on tag name
				return Document.this.createRhinoElement(coreElement, tagName);
			}

			@Override
			public INode createTextNode(String text) {
				// For now, return a simple core text node
				// Can be enhanced later with Rhino wrapper if needed
				return new com.w3canvas.javacanvas.core.dom.CoreTextNode(text, coreDocument);
			}
		});
	}

	/**
	 * Get the core document (NOT Context-bound, accessible from any thread/Context).
	 * @return the core document
	 */
	public CoreDocument getCoreDocument() {
		return coreDocument;
	}

	public void init(IWindowHost windowHost) {
		this.windowHost = windowHost;
		this.setDocument(this);
		this.fonts = new RhinoFontFaceSet();
		this.fonts.init(this.getParentScope(), new FontFaceSet());
	}

	public Node jsFunction_getElementById(String id) {
		// Delegate to core document
		IElement element = coreDocument.getElementById(id);
		if (element instanceof Node) {
			return (Node) element;
		}

		// Fallback to legacy documentsNode map for backwards compatibility
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

	/**
	 * Create a Rhino-wrapped element for the given tag name.
	 * Called by the ElementFactory.
	 */
	private IElement createRhinoElement(CoreElement coreElement, String tagName) {
		try {
			NodeType itemNodeType = NodeType.getNodeTypeByName(tagName);

			if (itemNodeType == null) {
				// Unknown element type - return the core element
				return coreElement;
			}

			Node node = itemNodeType.getNode();
			node.setDocument(this);
			node.setParentScope(this.getParentScope()); // Set parent scope for Rhino
			node.init();

			// Register the core element's ID with the Rhino node
			String id = coreElement.getId();
			if (id != null) {
				documentsNode.put(id, node);
				// Also set the ID on the Rhino node
				node.jsSet_id(id);
			}

			// Return the core element for now
			// TODO: Make Node implement IElement and wrap CoreElement
			return coreElement;
		} catch (Exception e) {
			System.err.println("Error creating Rhino element for tag: " + tagName + ": " + e.getMessage());
			return coreElement;
		}
	}

	public Node jsFunction_createElement(String nodeType) throws IllegalNodeException {
		NodeType itemNodeType = NodeType.getNodeTypeByName(nodeType);

		if (itemNodeType == null) {
			throw new IllegalNodeException("Illegal node type : " + nodeType);
		}

		Node node = itemNodeType.getNode();
		node.setDocument(this);
		node.setParentScope(this.getParentScope()); // Set parent scope for Rhino
		node.init();

		// TODO: Also register in core document for cross-Context access
		// For now, this uses the old implementation

		return node;
	}

	public IWindowHost getWindowHost() {
		return windowHost;
	}

	public AbstractView jsGet_defaultView() {
		return null;
	}

	public RhinoFontFaceSet jsGet_fonts() {
		return fonts;
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

		for (Entry<String, Node> entry : nodes.entrySet()) {
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
