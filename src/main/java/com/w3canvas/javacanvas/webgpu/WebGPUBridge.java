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
            String osName = System.getProperty("os.name").toLowerCase();
            String libName;
            String suffix;

            if (osName.contains("win")) {
                libName = "jWebGPU64.dll";
                suffix = ".dll";
            } else if (osName.contains("mac")) {
                libName = "libjWebGPU64.dylib";
                suffix = ".dylib";
            } else {
                libName = "libjWebGPU64.so";
                suffix = ".so";
            }

            String resourcePath = "/native/wgpu/" + libName;

            System.out.println("[WebGPUBridge] Looking for resource: " + resourcePath);
            InputStream is = WebGPUBridge.class.getResourceAsStream(resourcePath);
            if (is == null) {
                System.err.println("[WebGPUBridge] Could not find native library in classpath: " + resourcePath);
                return;
            }

            File tempLib = File.createTempFile("jWebGPU64", suffix);
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
        } catch (Throwable e) {
            System.err.println("[WebGPUBridge] Failed to load native library: " + e.getMessage());
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

    private WGPUQueue currentQueue;

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
                    future.complete(new Adapter(WebGPUBridge.this, adapter));
                } else {
                    System.err.println("[WebGPUBridge] Failed to get adapter: " + message);
                    future.completeExceptionally(new RuntimeException("Failed to get adapter: " + message));
                }
            }
        });

        return future;
    }

    public static class Adapter {
        private final WebGPUBridge bridge;
        private final WGPUAdapter adapter;

        public Adapter(WebGPUBridge bridge, WGPUAdapter adapter) {
            this.bridge = bridge;
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
                        bridge.currentQueue = device.getQueue();
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
        if (currentQueue == null) {
            System.err.println("[WebGPUBridge] Error: Queue not initialized");
            return;
        }

        System.out
                .println("[WebGPUBridge] submit called with " + (commands != null ? commands.length : 0) + " commands");

        if (commands == null || commands.length == 0)
            return;

        // Simple Command Decoder
        int i = 0;
        WGPUCommandEncoder encoder = null;
        WGPURenderPassEncoder passEncoder = null;

        while (i < commands.length) {
            int cmd = commands[i++];
            switch (cmd) {
                case 1: // BEGIN_RENDER_PASS
                    System.out.println("  CMD: BEGIN_RENDER_PASS");
                    // TODO: Create real encoder/pass
                    break;
                case 2: // END_RENDER_PASS
                    System.out.println("  CMD: END_RENDER_PASS");
                    break;
                case 3: // SET_PIPELINE
                    System.out.println("  CMD: SET_PIPELINE");
                    break;
                case 4: // DRAW
                    int vertexCount = commands[i++];
                    int instanceCount = commands[i++];
                    int firstVertex = commands[i++];
                    int firstInstance = commands[i++];
                    System.out.println("  CMD: DRAW " + vertexCount + ", " + instanceCount + ", " + firstVertex + ", "
                            + firstInstance);
                    break;
                default:
                    System.err.println("  Unknown command: " + cmd);
                    break;
            }
        }
    }
}
