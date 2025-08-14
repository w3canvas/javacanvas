package com.w3canvas.javacanvas.dom;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class FontFaceSet {
    private final Set<FontFace> faces = new HashSet<>();

    public void add(FontFace face) {
        faces.add(face);
    }

    public void delete(FontFace face) {
        faces.remove(face);
    }

    public void clear() {
        faces.clear();
    }

    public boolean check(String font, String text) {
        // This is a placeholder.
        // A real implementation would need to check if the font covers the given text.
        for (FontFace face : faces) {
            if (matches(face, font)) {
                return "loaded".equals(face.getStatus());
            }
        }
        return false;
    }

    public CompletableFuture<FontFace[]> load(String font, String text) {
        // This is a placeholder.
        // It should find the matching FontFace and call its load() method.
        for (FontFace face : faces) {
            if (matches(face, font)) {
                return face.load().thenApply(f -> new FontFace[]{f});
            }
        }
        return CompletableFuture.completedFuture(new FontFace[0]);
    }

    public Set<FontFace> getFaces() {
        return faces;
    }

    private boolean matches(FontFace face, String font) {
        // This is a simplified matching logic.
        // A real implementation would need to parse the font string and match properties.
        return font.contains(face.getFamily());
    }
}
