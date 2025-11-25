package com.w3canvas.javacanvas.backend.graal.impl.node;

import org.graalvm.polyglot.HostAccess;

import com.w3canvas.javacanvas.core.dom.CoreDocument;

import java.util.HashMap;
import java.util.Map;

/**
 * GraalJS adapter for Document.
 * Wraps CoreDocument and provides DOM-like API for GraalJS.
 */
public class GraalDocument {
    private final CoreDocument core;
    private final Map<String, Object> elementsById;

    public GraalDocument() {
        this.core = new CoreDocument();
        this.elementsById = new HashMap<>();
    }

    @HostAccess.Export
    public Object createElement(String tagName) {
        switch (tagName.toLowerCase()) {
            case "canvas":
                GraalHTMLCanvasElement canvas = new GraalHTMLCanvasElement();
                return canvas;
            default:
                // For now, only support canvas elements
                System.err.println("GraalDocument: createElement('" + tagName + "') not fully implemented");
                return new Object(); // Placeholder
        }
    }

    @HostAccess.Export
    public Object getElementById(String id) {
        return elementsById.get(id);
    }

    /**
     * Add an element to the document's ID registry.
     * Called from JavaScript or internally when element.id is set.
     *
     * @param id The element ID
     * @param element The element
     */
    @HostAccess.Export
    public void addElement(String id, Object element) {
        if (id != null && !id.isEmpty()) {
            elementsById.put(id, element);

            // Also register in core document if it's a canvas
            if (element instanceof GraalHTMLCanvasElement) {
                GraalHTMLCanvasElement canvas = (GraalHTMLCanvasElement) element;
                canvas.setId(id);
            }
        }
    }

    @HostAccess.Export
    public void removeElement(String id) {
        elementsById.remove(id);
    }

    /**
     * Get the core document (for internal use).
     */
    public CoreDocument getCore() {
        return core;
    }
}
