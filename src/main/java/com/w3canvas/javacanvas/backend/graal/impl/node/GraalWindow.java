package com.w3canvas.javacanvas.backend.graal.impl.node;

import org.graalvm.polyglot.HostAccess;

/**
 * GraalJS adapter for Window object.
 * Provides window properties and methods for GraalJS.
 */
public class GraalWindow {
    private int innerWidth;
    private int innerHeight;
    private GraalDocument document;

    public GraalWindow() {
        this(800, 600);
    }

    public GraalWindow(int width, int height) {
        this.innerWidth = width;
        this.innerHeight = height;
    }

    @HostAccess.Export
    public int getInnerWidth() {
        return innerWidth;
    }

    @HostAccess.Export
    public void setInnerWidth(int width) {
        this.innerWidth = width;
    }

    @HostAccess.Export
    public int getInnerHeight() {
        return innerHeight;
    }

    @HostAccess.Export
    public void setInnerHeight(int height) {
        this.innerHeight = height;
    }

    @HostAccess.Export
    public GraalDocument getDocument() {
        return document;
    }

    @HostAccess.Export
    public void setDocument(GraalDocument document) {
        this.document = document;
    }
}
