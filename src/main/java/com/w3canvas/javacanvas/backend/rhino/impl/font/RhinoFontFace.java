package com.w3canvas.javacanvas.backend.rhino.impl.font;

import com.w3canvas.javacanvas.backend.rhino.impl.node.ProjectScriptableObject;
import com.w3canvas.javacanvas.dom.FontFace;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

@SuppressWarnings("serial")
public class RhinoFontFace extends ProjectScriptableObject {

    private FontFace fontFace;

    public RhinoFontFace() {
        // for Rhino
    }

    public void jsConstructor(String family, String source, ScriptableObject descriptors) {
        String style = "", weight = "", stretch = "", unicodeRange = "", variant = "", featureSettings = "";
        if (descriptors != null) {
            style = getString(descriptors, "style");
            weight = getString(descriptors, "weight");
            stretch = getString(descriptors, "stretch");
            unicodeRange = getString(descriptors, "unicodeRange");
            variant = getString(descriptors, "variant");
            featureSettings = getString(descriptors, "featureSettings");
        }

        String url = source.replaceAll("url\\(|\\)", "");
        this.fontFace = new FontFace(family, url, style, weight, stretch, unicodeRange, variant, featureSettings);
    }

    private String getString(ScriptableObject obj, String name) {
        Object value = obj.get(name, obj);
        return value instanceof String ? (String) value : "";
    }

    public String jsGet_family() {
        return fontFace.getFamily();
    }

    public String jsGet_source() {
        return fontFace.getSource();
    }

    public String jsGet_style() {
        return fontFace.getStyle();
    }

    public String jsGet_weight() {
        return fontFace.getWeight();
    }

    public String jsGet_stretch() {
        return fontFace.getStretch();
    }

    public String jsGet_unicodeRange() {
        return fontFace.getUnicodeRange();
    }

    public String jsGet_variant() {
        return fontFace.getVariant();
    }

    public String jsGet_featureSettings() {
        return fontFace.getFeatureSettings();
    }

    public String jsGet_status() {
        return fontFace.getStatus();
    }

    public Object jsGet_loaded() {
        return createPromise(fontFace.getLoaded());
    }

    public Object jsFunction_load() {
        fontFace.load();
        return createPromise(fontFace.getLoaded());
    }

    private RhinoPromise createPromise(java.util.concurrent.CompletableFuture<?> future) {
        RhinoPromise promise = new RhinoPromise(future);
        Scriptable scope = ScriptableObject.getTopLevelScope(this);
        promise.setParentScope(scope);
        promise.setPrototype(ScriptableObject.getClassPrototype(scope, "Promise"));
        return promise;
    }

    @Override
    public String getClassName() {
        return "FontFace";
    }

    public FontFace getFontFace() {
        return fontFace;
    }
}
