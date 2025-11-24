package com.w3canvas.javacanvas.interfaces;

import java.util.List;

/**
 * Core DOM Node interface - backend agnostic.
 *
 * Represents a node in the DOM tree structure. This interface is independent
 * of any JavaScript engine (Rhino, GraalJS) and provides the core DOM
 * functionality that can be wrapped by engine-specific adapters.
 */
public interface INode {

    /**
     * Get the node name (e.g., "div", "canvas", "#text").
     * @return the node name
     */
    String getNodeName();

    /**
     * Get the node type (e.g., ELEMENT_NODE = 1, TEXT_NODE = 3).
     * @return the node type constant
     */
    int getNodeType();

    /**
     * Get the parent node.
     * @return the parent node, or null if this is the root
     */
    INode getParentNode();

    /**
     * Set the parent node.
     * @param parent the new parent node
     */
    void setParentNode(INode parent);

    /**
     * Get the list of child nodes.
     * @return the child nodes (may be empty, never null)
     */
    List<INode> getChildNodes();

    /**
     * Append a child node.
     * @param child the child to append
     * @return the appended child
     */
    INode appendChild(INode child);

    /**
     * Remove a child node.
     * @param child the child to remove
     * @return the removed child
     */
    INode removeChild(INode child);

    /**
     * Insert a node before a reference node.
     * @param newNode the node to insert
     * @param refNode the reference node (or null to append)
     * @return the inserted node
     */
    INode insertBefore(INode newNode, INode refNode);

    /**
     * Get the first child node.
     * @return the first child, or null if no children
     */
    INode getFirstChild();

    /**
     * Get the last child node.
     * @return the last child, or null if no children
     */
    INode getLastChild();

    /**
     * Get the next sibling node.
     * @return the next sibling, or null if this is the last child
     */
    INode getNextSibling();

    /**
     * Get the previous sibling node.
     * @return the previous sibling, or null if this is the first child
     */
    INode getPreviousSibling();

    /**
     * Get the owner document.
     * @return the document that owns this node
     */
    IDocument getOwnerDocument();

    /**
     * Clone this node.
     * @param deep if true, recursively clone all descendants
     * @return the cloned node
     */
    INode cloneNode(boolean deep);

    // Node type constants
    int ELEMENT_NODE = 1;
    int TEXT_NODE = 3;
    int COMMENT_NODE = 8;
    int DOCUMENT_NODE = 9;
    int DOCUMENT_FRAGMENT_NODE = 11;
}
