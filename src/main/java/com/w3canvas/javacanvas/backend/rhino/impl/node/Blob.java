package com.w3canvas.javacanvas.backend.rhino.impl.node;

import com.w3canvas.javacanvas.interfaces.IBlob;

/**
 * Rhino JavaScript binding for Blob objects.
 * Blobs represent immutable file-like objects of raw data.
 */
@SuppressWarnings("serial")
public class Blob extends ProjectScriptableObject implements IBlob {

    private IBlob core;

    public Blob() {
    }

    /**
     * Initialize the Blob with a core implementation.
     * @param core The core Blob implementation
     */
    public void init(IBlob core) {
        this.core = core;
    }

    @Override
    public String getClassName() {
        return "Blob";
    }

    /**
     * JavaScript getter for the size property.
     * @return The size of the blob in bytes
     */
    public long jsGet_size() {
        return core.getSize();
    }

    /**
     * JavaScript getter for the type property.
     * @return The MIME type of the blob
     */
    public String jsGet_type() {
        return core.getType();
    }

    @Override
    public long getSize() {
        return core.getSize();
    }

    @Override
    public String getType() {
        return core.getType();
    }

    @Override
    public byte[] getData() {
        return core.getData();
    }
}
