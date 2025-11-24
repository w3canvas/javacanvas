package com.w3canvas.javacanvas.core.dom;

import com.w3canvas.javacanvas.interfaces.IDocument;
import com.w3canvas.javacanvas.interfaces.INode;

import java.util.ArrayList;
import java.util.List;

/**
 * Core DOM Node implementation - backend agnostic.
 *
 * This class implements the DOM tree structure logic independent of any
 * JavaScript engine. It can be wrapped by engine-specific adapters (Rhino,
 * GraalJS) to provide JavaScript bindings.
 */
public class CoreNode implements INode {

    private final String nodeName;
    private final int nodeType;
    private INode parentNode;
    private final List<INode> childNodes;
    private IDocument ownerDocument;

    /**
     * Create a new node.
     * @param nodeName the node name
     * @param nodeType the node type constant
     * @param ownerDocument the owner document
     */
    public CoreNode(String nodeName, int nodeType, IDocument ownerDocument) {
        this.nodeName = nodeName;
        this.nodeType = nodeType;
        this.ownerDocument = ownerDocument;
        this.childNodes = new ArrayList<>();
    }

    @Override
    public String getNodeName() {
        return nodeName;
    }

    @Override
    public int getNodeType() {
        return nodeType;
    }

    @Override
    public INode getParentNode() {
        return parentNode;
    }

    @Override
    public void setParentNode(INode parent) {
        this.parentNode = parent;
    }

    @Override
    public List<INode> getChildNodes() {
        return new ArrayList<>(childNodes); // Return copy to prevent external modification
    }

    @Override
    public INode appendChild(INode child) {
        if (child == null) {
            throw new IllegalArgumentException("Cannot append null child");
        }

        // Remove from previous parent if any
        if (child.getParentNode() != null) {
            child.getParentNode().removeChild(child);
        }

        // Add to this node's children
        childNodes.add(child);
        child.setParentNode(this);

        return child;
    }

    @Override
    public INode removeChild(INode child) {
        if (child == null || !childNodes.contains(child)) {
            throw new IllegalArgumentException("Node is not a child of this node");
        }

        childNodes.remove(child);
        child.setParentNode(null);

        return child;
    }

    @Override
    public INode insertBefore(INode newNode, INode refNode) {
        if (newNode == null) {
            throw new IllegalArgumentException("Cannot insert null node");
        }

        // If refNode is null, append to end
        if (refNode == null) {
            return appendChild(newNode);
        }

        // Find index of reference node
        int index = childNodes.indexOf(refNode);
        if (index == -1) {
            throw new IllegalArgumentException("Reference node is not a child of this node");
        }

        // Remove from previous parent if any
        if (newNode.getParentNode() != null) {
            newNode.getParentNode().removeChild(newNode);
        }

        // Insert at the found index
        childNodes.add(index, newNode);
        newNode.setParentNode(this);

        return newNode;
    }

    @Override
    public INode getFirstChild() {
        return childNodes.isEmpty() ? null : childNodes.get(0);
    }

    @Override
    public INode getLastChild() {
        return childNodes.isEmpty() ? null : childNodes.get(childNodes.size() - 1);
    }

    @Override
    public INode getNextSibling() {
        if (parentNode == null) {
            return null;
        }

        List<INode> siblings = parentNode.getChildNodes();
        int index = siblings.indexOf(this);
        if (index == -1 || index == siblings.size() - 1) {
            return null;
        }

        return siblings.get(index + 1);
    }

    @Override
    public INode getPreviousSibling() {
        if (parentNode == null) {
            return null;
        }

        List<INode> siblings = parentNode.getChildNodes();
        int index = siblings.indexOf(this);
        if (index <= 0) {
            return null;
        }

        return siblings.get(index - 1);
    }

    @Override
    public IDocument getOwnerDocument() {
        return ownerDocument;
    }

    @Override
    public INode cloneNode(boolean deep) {
        // Create a shallow clone
        CoreNode clone = new CoreNode(nodeName, nodeType, ownerDocument);

        // If deep clone, recursively clone children
        if (deep) {
            for (INode child : childNodes) {
                clone.appendChild(child.cloneNode(true));
            }
        }

        return clone;
    }

    /**
     * Set the owner document (used internally).
     * @param ownerDocument the owner document
     */
    protected void setOwnerDocument(IDocument ownerDocument) {
        this.ownerDocument = ownerDocument;
    }

    /**
     * Get the internal child list (for subclass use).
     * @return the child nodes list
     */
    protected List<INode> getChildNodesInternal() {
        return childNodes;
    }
}
