package com.w3canvas.javacanvas.dom;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import com.w3canvas.javacanvas.interfaces.IFont;

public class FontFace {
    private final String family;
    private final String source;
    private final String style;
    private final String weight;
    private final String stretch;
    private final String unicodeRange;
    private final String variant;
    private final String featureSettings;
    private String status; // "unloaded", "loading", "loaded", "error"
    private final CompletableFuture<FontFace> loaded;
    private IFont font;
    private byte[] fontData;

    public FontFace(String family, String source, String style, String weight, String stretch, String unicodeRange,
            String variant, String featureSettings) {
        this.family = family;
        this.source = source;
        this.style = style;
        this.weight = weight;
        this.stretch = stretch;
        this.unicodeRange = unicodeRange;
        this.variant = variant;
        this.featureSettings = featureSettings;
        this.status = "unloaded";
        this.loaded = new CompletableFuture<>();
    }

    public String getFamily() {
        return family;
    }

    public String getSource() {
        return source;
    }

    public String getStyle() {
        return style;
    }

    public String getWeight() {
        return weight;
    }

    public String getStretch() {
        return stretch;
    }

    public String getUnicodeRange() {
        return unicodeRange;
    }

    public String getVariant() {
        return variant;
    }

    public String getFeatureSettings() {
        return featureSettings;
    }

    public String getStatus() {
        return status;
    }

    public CompletableFuture<FontFace> getLoaded() {
        return loaded;
    }

    public CompletableFuture<FontFace> load() {
        if (status.equals("loaded") || status.equals("loading")) {
            return loaded;
        }

        status = "loading";

        new Thread(() -> {
            try {
                URL url;
                try {
                    url = new URL(this.source);
                } catch (java.net.MalformedURLException e) {
                    // If not a valid URL, try as a local file
                    java.io.File file = new java.io.File(this.source);
                    if (!file.exists()) {
                        throw new java.io.FileNotFoundException("Font file not found: " + this.source);
                    }
                    url = file.toURI().toURL();
                }

                InputStream is = url.openStream();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[4096];
                int n;
                while ((n = is.read(buffer)) > 0) {
                    baos.write(buffer, 0, n);
                }
                is.close();

                this.fontData = baos.toByteArray();
                this.status = "loaded";
                // The promise resolves with the FontFace itself
                this.loaded.complete(this);
            } catch (Exception e) {
                this.status = "error";
                this.loaded.completeExceptionally(e);
            }
        }).start();

        return loaded;
    }

    public byte[] getFontData() {
        return fontData;
    }

    public IFont getFont() {
        return this.font;
    }

    public void setFont(IFont font) {
        this.font = font;
    }
}
