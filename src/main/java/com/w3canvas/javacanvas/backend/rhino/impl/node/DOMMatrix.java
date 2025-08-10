package com.w3canvas.javacanvas.backend.rhino.impl.node;

import java.awt.geom.AffineTransform;

@SuppressWarnings("serial")
public class DOMMatrix extends ProjectScriptableObject {

    private AffineTransform transform;

    public DOMMatrix() {
        this.transform = new AffineTransform();
    }

    public DOMMatrix(AffineTransform transform) {
        this.transform = transform;
    }

    @Override
    public String getClassName() {
        return "DOMMatrix";
    }

    public double jsGet_a() {
        return transform.getScaleX();
    }

    public double jsGet_b() {
        return transform.getShearY();
    }

    public double jsGet_c() {
        return transform.getShearX();
    }

    public double jsGet_d() {
        return transform.getScaleY();
    }

    public double jsGet_e() {
        return transform.getTranslateX();
    }

    public double jsGet_f() {
        return transform.getTranslateY();
    }

    public AffineTransform getTransform() {
        return transform;
    }
}
