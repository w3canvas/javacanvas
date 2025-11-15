package com.w3canvas.javacanvas.core;

import com.w3canvas.javacanvas.interfaces.IBlob;

/**
 * Core implementation of Blob representing immutable binary data.
 */
public class Blob implements IBlob {
    private final byte[] data;
    private final String type;

    /**
     * Create a new Blob with the given data and MIME type.
     * @param data The binary data
     * @param type The MIME type (e.g., "image/png")
     */
    public Blob(byte[] data, String type) {
        this.data = data != null ? data.clone() : new byte[0];
        this.type = type != null ? type : "";
    }

    @Override
    public long getSize() {
        return data.length;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public byte[] getData() {
        return data.clone(); // Return a copy to maintain immutability
    }
}
