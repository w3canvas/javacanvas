package com.w3canvas.javacanvas.core.dom;

import com.w3canvas.javacanvas.interfaces.IDocument;
import com.w3canvas.javacanvas.interfaces.INode;

/**
 * Core DOM Text Node implementation - backend agnostic.
 *
 * Represents a text node in the DOM tree.
 */
public class CoreTextNode extends CoreNode {

    private String textContent;

    /**
     * Create a new text node.
     * @param text the text content
     * @param ownerDocument the owner document
     */
    public CoreTextNode(String text, IDocument ownerDocument) {
        super("#text", TEXT_NODE, ownerDocument);
        this.textContent = text;
    }

    /**
     * Get the text content.
     * @return the text content
     */
    public String getTextContent() {
        return textContent;
    }

    /**
     * Set the text content.
     * @param textContent the new text content
     */
    public void setTextContent(String textContent) {
        this.textContent = textContent;
    }

    @Override
    public INode cloneNode(boolean deep) {
        return new CoreTextNode(textContent, getOwnerDocument());
    }
}
