package com.w3canvas.javacanvas.backend.rhino.impl.node;

import com.w3canvas.javacanvas.core.dom.CoreElement;
import com.w3canvas.javacanvas.interfaces.IDocument;
import com.w3canvas.javacanvas.interfaces.INode;

import java.util.List;

/**
 * Adapter that wraps a Rhino Node to implement IElement.
 *
 * This allows Rhino Node objects to be registered in CoreDocument's ID registry,
 * enabling cross-Context getElementById lookups. The adapter delegates method calls
 * to the wrapped Rhino Node.
 *
 * This is a temporary solution - the full refactoring would make Node directly
 * implement IElement and wrap CoreElement.
 */
public class RhinoNodeAdapter extends CoreElement {

    private final Node rhinoNode;

    /**
     * Create an adapter for a Rhino Node.
     * @param rhinoNode the Rhino node to wrap
     */
    public RhinoNodeAdapter(Node rhinoNode) {
        super(rhinoNode.getNodeName() != null ? rhinoNode.getNodeName() : "node",
              rhinoNode.getDocument() != null ? rhinoNode.getDocument().getCoreDocument() : null);
        this.rhinoNode = rhinoNode;
    }

    /**
     * Get the wrapped Rhino node.
     * @return the Rhino node
     */
    public Node getRhinoNode() {
        return rhinoNode;
    }

    @Override
    public String getId() {
        return rhinoNode.jsGet_id();
    }

    @Override
    public void setId(String id) {
        rhinoNode.jsSet_id(id);
    }

    @Override
    public String getClassName() {
        String classAttr = rhinoNode.jsFunction_getAttribute("class");
        return classAttr != null ? classAttr : "";
    }

    @Override
    public void setClassName(String className) {
        rhinoNode.jsFunction_setAttribute("class", className);
    }

    @Override
    public String getAttribute(String name) {
        return rhinoNode.jsFunction_getAttribute(name);
    }

    @Override
    public void setAttribute(String name, String value) {
        rhinoNode.jsFunction_setAttribute(name, value);
    }

    @Override
    public void removeAttribute(String name) {
        rhinoNode.jsFunction_removeAttribute(name);
    }

    @Override
    public boolean hasAttribute(String name) {
        return rhinoNode.jsFunction_hasAttribute(name);
    }

    @Override
    public Object getStyle() {
        return rhinoNode.jsGet_style();
    }

    // Note: Other INode methods (appendChild, etc.) would delegate to rhinoNode
    // but are not critical for getElementById functionality
}
