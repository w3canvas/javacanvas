package com.w3canvas.javacanvas.interfaces;

/**
 * Core DOM Document interface - backend agnostic.
 *
 * Represents the document object model root. This interface is independent
 * of any JavaScript engine and provides core document functionality.
 */
public interface IDocument extends INode {

    /**
     * Get the document element (root element, typically <html>).
     * @return the document element
     */
    IElement getDocumentElement();

    /**
     * Create a new element with the given tag name.
     * @param tagName the element tag name (e.g., "div", "canvas")
     * @return the newly created element
     */
    IElement createElement(String tagName);

    /**
     * Create a text node with the given content.
     * @param text the text content
     * @return the newly created text node
     */
    INode createTextNode(String text);

    /**
     * Get an element by its id attribute.
     * @param id the element id
     * @return the element, or null if not found
     */
    IElement getElementById(String id);

    /**
     * Get elements by tag name.
     * @param tagName the tag name to search for
     * @return array of matching elements
     */
    IElement[] getElementsByTagName(String tagName);

    /**
     * Get elements by class name.
     * @param className the class name to search for
     * @return array of matching elements
     */
    IElement[] getElementsByClassName(String className);

    /**
     * Get the document's body element.
     * @return the body element, or null if not present
     */
    IElement getBody();

    /**
     * Set the document's body element.
     * @param body the new body element
     */
    void setBody(IElement body);

    /**
     * Get the document's head element.
     * @return the head element, or null if not present
     */
    IElement getHead();
}
