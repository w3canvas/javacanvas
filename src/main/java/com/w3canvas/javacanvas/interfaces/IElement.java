package com.w3canvas.javacanvas.interfaces;

/**
 * Core DOM Element interface - backend agnostic.
 *
 * Represents an element node in the DOM tree with attributes and properties.
 * Extends INode with element-specific functionality.
 */
public interface IElement extends INode {

    /**
     * Get the tag name (e.g., "DIV", "CANVAS").
     * In HTML, this is typically uppercase.
     * @return the tag name
     */
    String getTagName();

    /**
     * Get an attribute value by name.
     * @param name the attribute name
     * @return the attribute value, or null if not present
     */
    String getAttribute(String name);

    /**
     * Set an attribute value.
     * @param name the attribute name
     * @param value the attribute value
     */
    void setAttribute(String name, String value);

    /**
     * Remove an attribute.
     * @param name the attribute name
     */
    void removeAttribute(String name);

    /**
     * Check if an attribute exists.
     * @param name the attribute name
     * @return true if the attribute is present
     */
    boolean hasAttribute(String name);

    /**
     * Get the element's id attribute.
     * @return the id, or null if not set
     */
    String getId();

    /**
     * Set the element's id attribute.
     * @param id the new id
     */
    void setId(String id);

    /**
     * Get the element's class attribute.
     * @return the class string, or null if not set
     */
    String getClassName();

    /**
     * Set the element's class attribute.
     * @param className the new class string
     */
    void setClassName(String className);

    /**
     * Get the element's inline style.
     * This is a simplified style representation.
     * @return the style object
     */
    Object getStyle();
}
