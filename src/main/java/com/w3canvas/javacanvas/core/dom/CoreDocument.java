package com.w3canvas.javacanvas.core.dom;

import com.w3canvas.javacanvas.interfaces.IDocument;
import com.w3canvas.javacanvas.interfaces.IElement;
import com.w3canvas.javacanvas.interfaces.INode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Core DOM Document implementation - backend agnostic.
 *
 * This is the heart of the DOM Trident refactoring. This class is NOT tied to
 * any JavaScript engine Context, so it can be safely accessed from multiple
 * threads and multiple Rhino Contexts.
 *
 * Backend-specific adapters (Rhino, GraalJS) wrap this core implementation
 * and provide JavaScript bindings.
 */
public class CoreDocument extends CoreNode implements IDocument {

    private IElement documentElement;
    private IElement body;
    private IElement head;

    // ID registry for fast getElementById lookups
    private final Map<String, IElement> elementRegistry = new HashMap<>();

    // Factory for creating backend-specific elements
    // This allows the core document to delegate element creation to the backend
    private ElementFactory elementFactory;

    /**
     * Factory interface for creating backend-specific elements.
     * The backend (Rhino, GraalJS) implements this to create wrapped elements.
     */
    public interface ElementFactory {
        /**
         * Create a backend-specific element wrapper around a core element.
         * @param coreElement the core element
         * @param tagName the tag name
         * @return the backend-specific element wrapper
         */
        IElement createElementWrapper(CoreElement coreElement, String tagName);

        /**
         * Create a backend-specific text node.
         * @param text the text content
         * @return the backend-specific text node
         */
        INode createTextNode(String text);
    }

    /**
     * Create a new document.
     */
    public CoreDocument() {
        super("#document", DOCUMENT_NODE, null); // Document owns itself
        setOwnerDocument(this);
    }

    /**
     * Set the element factory for creating backend-specific elements.
     * @param factory the element factory
     */
    public void setElementFactory(ElementFactory factory) {
        this.elementFactory = factory;
    }

    @Override
    public IElement getDocumentElement() {
        return documentElement;
    }

    /**
     * Set the document element (root element).
     * @param documentElement the document element
     */
    public void setDocumentElement(IElement documentElement) {
        this.documentElement = documentElement;
    }

    @Override
    public IElement createElement(String tagName) {
        // Create core element
        CoreElement coreElement = new CoreElement(tagName, this);

        // If we have a factory, let it create a wrapped element
        if (elementFactory != null) {
            return elementFactory.createElementWrapper(coreElement, tagName);
        }

        // Otherwise return the core element directly
        return coreElement;
    }

    @Override
    public INode createTextNode(String text) {
        if (elementFactory != null) {
            return elementFactory.createTextNode(text);
        }

        // Fallback: create a simple text node
        return new CoreTextNode(text, this);
    }

    @Override
    public IElement getElementById(String id) {
        return elementRegistry.get(id);
    }

    @Override
    public IElement[] getElementsByTagName(String tagName) {
        List<IElement> result = new ArrayList<>();
        String upperTagName = tagName.toUpperCase();
        collectElementsByTagName(this, upperTagName, result);
        return result.toArray(new IElement[0]);
    }

    private void collectElementsByTagName(INode node, String tagName, List<IElement> result) {
        for (INode child : node.getChildNodes()) {
            if (child instanceof IElement) {
                IElement element = (IElement) child;
                if ("*".equals(tagName) || tagName.equals(element.getTagName())) {
                    result.add(element);
                }
                // Recursively search children
                collectElementsByTagName(child, tagName, result);
            }
        }
    }

    @Override
    public IElement[] getElementsByClassName(String className) {
        List<IElement> result = new ArrayList<>();
        collectElementsByClassName(this, className, result);
        return result.toArray(new IElement[0]);
    }

    private void collectElementsByClassName(INode node, String className, List<IElement> result) {
        for (INode child : node.getChildNodes()) {
            if (child instanceof IElement) {
                IElement element = (IElement) child;
                String elementClass = element.getClassName();
                if (elementClass != null) {
                    // Check if className is in the space-separated class list
                    String[] classes = elementClass.trim().split("\\s+");
                    for (String cls : classes) {
                        if (className.equals(cls)) {
                            result.add(element);
                            break;
                        }
                    }
                }
                // Recursively search children
                collectElementsByClassName(child, className, result);
            }
        }
    }

    @Override
    public IElement getBody() {
        return body;
    }

    @Override
    public void setBody(IElement body) {
        this.body = body;
    }

    @Override
    public IElement getHead() {
        return head;
    }

    /**
     * Set the head element.
     * @param head the head element
     */
    public void setHead(IElement head) {
        this.head = head;
    }

    /**
     * Register an element by ID (called by CoreElement.setAttribute).
     * @param id the element id
     * @param element the element
     */
    public void registerElementById(String id, IElement element) {
        if (id != null && !id.isEmpty()) {
            elementRegistry.put(id, element);
        }
    }

    /**
     * Unregister an element by ID (called by CoreElement.removeAttribute).
     * @param id the element id
     */
    public void unregisterElementById(String id) {
        if (id != null) {
            elementRegistry.remove(id);
        }
    }
}
