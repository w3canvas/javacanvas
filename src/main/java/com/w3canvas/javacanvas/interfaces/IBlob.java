package com.w3canvas.javacanvas.interfaces;

/**
 * Interface for Blob objects representing binary data.
 * Blobs are immutable raw data objects.
 */
public interface IBlob {
    /**
     * Get the size of the blob in bytes.
     * @return The size in bytes
     */
    long getSize();

    /**
     * Get the MIME type of the blob.
     * @return The MIME type string
     */
    String getType();

    /**
     * Get the raw byte data of the blob.
     * @return The byte array containing blob data
     */
    byte[] getData();
}
