package com.w3canvas.javacanvas.backend.rhino.impl.font;

import com.w3canvas.javacanvas.backend.rhino.impl.node.ProjectScriptableObject;
import com.w3canvas.javacanvas.dom.FontFaceSet;
import org.mozilla.javascript.Scriptable;

@SuppressWarnings("serial")
public class RhinoFontFaceSet extends ProjectScriptableObject {

    private FontFaceSet fontFaceSet;

    public RhinoFontFaceSet() {
        // for Rhino
    }

    public void init(Scriptable scope, FontFaceSet fontFaceSet) {
        this.fontFaceSet = fontFaceSet;
        setParentScope(scope);
        setPrototype(getClassPrototype(scope, "FontFaceSet"));
    }

    public void jsFunction_add(RhinoFontFace fontFace) {
        fontFaceSet.add(fontFace.getFontFace());
    }

    public void jsFunction_delete(RhinoFontFace fontFace) {
        fontFaceSet.delete(fontFace.getFontFace());
    }

    public void jsFunction_clear() {
        fontFaceSet.clear();
    }

    public boolean jsFunction_check(String font, String text) {
        return fontFaceSet.check(font, text);
    }

    public Object jsFunction_load(String font, String text) {
        return fontFaceSet.load(font, text);
    }

    @Override
    public String getClassName() {
        return "FontFaceSet";
    }

    public java.util.Set<com.w3canvas.javacanvas.dom.FontFace> getFaces() {
        return fontFaceSet.getFaces();
    }
}
