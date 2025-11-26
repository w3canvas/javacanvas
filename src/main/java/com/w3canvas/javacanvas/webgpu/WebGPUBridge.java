package com.w3canvas.javacanvas.webgpu;

import com.github.xpenatan.webgpu.*;
import java.io.*;
import java.util.concurrent.CompletableFuture;

public class WebGPUBridge {
    private static boolean nativeLoaded = false;
    private WGPUInstance instance;

    static {
        loadNativeLibrary();
    }

    private static void loadNativeLibrary() {
        if (nativeLoaded)
            return;
        try {
            String libName = "jWebGPU64.dll";
            String resourcePath = "/native/wgpu/" + libName;

            System.out.println("[WebGPUBridge] Looking for resource: " + resourcePath);
            InputStream is = WebGPUBridge.class.getResourceAsStream(resourcePath);
            if (is == null) {
                System.err.println("[WebGPUBridge] Could not find native library in classpath: " + resourcePath);
                return;
            }

            File tempLib = File.createTempFile("jWebGPU64", ".dll");
            tempLib.deleteOnExit();

            try (OutputStream os = new FileOutputStream(tempLib)) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                }
            }

            System.out.println("[WebGPUBridge] Extracted native library to: " + tempLib.getAbsolutePath());
            System.load(tempLib.getAbsolutePath());
            System.out.println("[WebGPUBridge] Native library loaded successfully.");
            nativeLoaded = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public WebGPUBridge() {
        if (nativeLoaded) {
            instance = WGPU.setupInstance();
            if (instance.isValid()) {
                System.out.println("[WebGPUBridge] WGPUInstance created successfully.");
            } else {
                System.err.println("[WebGPUBridge] Failed to create WGPUInstance.");
            }
        }
    }

    public CompletableFuture<Adapter> requestAdapter() {
        System.out.println("[WebGPUBridge] requestAdapter called");
        CompletableFuture<Adapter> future = new CompletableFuture<>();

        if (instance == null) {
            future.completeExceptionally(new RuntimeException("WebGPU not initialized"));
            return future;
        }

        WGPURequestAdapterOptions options = WGPURequestAdapterOptions.obtain();
        options.setPowerPreference(WGPUPowerPreference.HighPerformance);

        instance.requestAdapter(options, WGPUCallbackMode.AllowProcessEvents, new WGPURequestAdapterCallback() {
            @Override
            public void onCallback(WGPURequestAdapterStatus status, WGPUAdapter adapter, String message) {
                if (status == WGPURequestAdapterStatus.Success) {
                    System.out.println("[WebGPUBridge] Adapter obtained: " + adapter);
                    future.complete(new Adapter(adapter));
                } else {
                    System.err.println("[WebGPUBridge] Failed to get adapter: " + message);
                    future.completeExceptionally(new RuntimeException("Failed to get adapter: " + message));
                }
            }
        });

        return future;
    }

    public static class Adapter {
        private final WGPUAdapter adapter;

        public Adapter(WGPUAdapter adapter) {
            this.adapter = adapter;
        }

        public CompletableFuture<Device> requestDevice() {
            System.out.println("[WebGPUBridge] requestDevice called");
            CompletableFuture<Device> future = new CompletableFuture<>();

            WGPUDeviceDescriptor descriptor = WGPUDeviceDescriptor.obtain();

            adapter.requestDevice(descriptor, WGPUCallbackMode.AllowProcessEvents, new WGPURequestDeviceCallback() {
                @Override
                public void onCallback(WGPURequestDeviceStatus status, WGPUDevice device, String message) {
                    if (status == WGPURequestDeviceStatus.Success) {
                        System.out.println("[WebGPUBridge] Device obtained: " + device);
                        future.complete(new Device(device));
                    } else {
                        System.err.println("[WebGPUBridge] Failed to get device: " + message);
                        future.completeExceptionally(new RuntimeException("Failed to get device: " + message));
                    }
                }
            }, new WGPUUncapturedErrorCallback() {
                @Override
                public void onCallback(WGPUErrorType type, String message) {
                    System.err.println("[WebGPUBridge] Uncaptured Error: " + type + " - " + message);
                }
            });
            return future;
        }
    }

    public static class Device {
        private final WGPUDevice device;
        public final Queue queue;

        public Device(WGPUDevice device) {
            this.device = device;
            this.queue = new Queue(device.getQueue());
        }
    }

    public static class Queue {
        private final WGPUQueue queue;

        public Queue(WGPUQueue queue) {
            this.queue = queue;
        }
    }

    public void submit(int[] commands) {
        System.out
                .println("[WebGPUBridge] submit called with " + (commands != null ? commands.length : 0) + " commands");
        // TODO: Implement command decoding and execution
    }
}
