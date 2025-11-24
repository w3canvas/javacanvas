package com.w3canvas.javacanvas.core.dom;

import com.w3canvas.javacanvas.interfaces.IDocument;
import com.w3canvas.javacanvas.interfaces.IElement;
import com.w3canvas.javacanvas.interfaces.INode;

import java.util.HashMap;
import java.util.Map;

/**
 * Core DOM Element implementation - backend agnostic.
 *
 * Extends CoreNode with element-specific functionality: attributes, id, className, etc.
 * Independent of JavaScript engine - can be wrapped by Rhino/GraalJS adapters.
 */
public class CoreElement extends CoreNode implements IElement {

    private final String tagName;
    private final Map<String, String> attributes;
    private Object style; // Style object (will be created by backend)

    /**
     * Create a new element.
     * @param tagName the element tag name (e.g., "div", "canvas")
     * @param ownerDocument the owner document
     */
    public CoreElement(String tagName, IDocument ownerDocument) {
        super(tagName.toLowerCase(), ELEMENT_NODE, ownerDocument);
        this.tagName = tagName.toUpperCase(); // HTML elements are uppercase
        this.attributes = new HashMap<>();
    }

    @Override
    public String getTagName() {
        return tagName;
    }

    @Override
    public String getAttribute(String name) {
        return attributes.get(name.toLowerCase());
    }

    @Override
    public void setAttribute(String name, String value) {
        attributes.put(name.toLowerCase(), value);

        // Special handling for id attribute
        if ("id".equalsIgnoreCase(name)) {
            // Update document's id registry if this element is in the document
            if (getOwnerDocument() instanceof CoreDocument) {
                ((CoreDocument) getOwnerDocument()).registerElementById(value, this);
            }
        }
    }

    @Override
    public void removeAttribute(String name) {
        String oldValue = attributes.remove(name.toLowerCase());

        // Special handling for id attribute
        if ("id".equalsIgnoreCase(name) && oldValue != null) {
            // Unregister from document's id registry
            if (getOwnerDocument() instanceof CoreDocument) {
                ((CoreDocument) getOwnerDocument()).unregisterElementById(oldValue);
            }
        }
    }

    @Override
    public boolean hasAttribute(String name) {
        return attributes.containsKey(name.toLowerCase());
    }

    @Override
    public String getId() {
        return getAttribute("id");
    }

    @Override
    public void setId(String id) {
        setAttribute("id", id);
    }

    @Override
    public String getClassName() {
        return getAttribute("class");
    }

    @Override
    public void setClassName(String className) {
        setAttribute("class", className);
    }

    @Override
    public Object getStyle() {
        return style;
    }

    /**
     * Set the style object (called by backend when creating style wrapper).
     * @param style the style object
     */
    public void setStyle(Object style) {
        this.style = style;
    }

    @Override
    public INode cloneNode(boolean deep) {
        // Create a shallow clone
        CoreElement clone = new CoreElement(tagName, getOwnerDocument());

        // Copy attributes
        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            clone.setAttribute(entry.getKey(), entry.getValue());
        }

        // If deep clone, recursively clone children
        if (deep) {
            for (INode child : getChildNodesInternal()) {
                clone.appendChild(child.cloneNode(true));
            }
        }

        return clone;
    }

    /**
     * Get all attributes as a map (for internal use).
     * @return the attributes map
     */
    public Map<String, String> getAttributes() {
        return new HashMap<>(attributes); // Return copy
    }
}
