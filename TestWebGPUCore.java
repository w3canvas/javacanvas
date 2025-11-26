///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS com.github.xpenatan.jWebGPU:webgpu-core:0.1.12
//DEPS com.github.xpenatan.jWebGPU:webgpu-desktop:0.1.12:windows_64_wgpu
//REPOS https://oss.sonatype.org/content/repositories/releases/

import com.github.xpenatan.webgpu.WGPU;
import com.github.xpenatan.webgpu.WGPUInstance;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class TestWebGPUCore {
    public static void main(String... args) {
        System.out.println("Attempting to load WebGPU...");
        try {
            loadNativeLibrary();

            WGPUInstance instance = WGPU.setupInstance();
            if (instance.isValid()) {
                System.out.println("WebGPU Instance is VALID!");
            } else {
                System.out.println("WebGPU Instance is INVALID (Native lib missing?)");
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private static void loadNativeLibrary() throws Exception {
        String libName = "jWebGPU64.dll";
        String resourcePath = "/native/wgpu/" + libName;

        System.out.println("Looking for resource: " + resourcePath);
        InputStream is = TestWebGPUCore.class.getResourceAsStream(resourcePath);
        if (is == null) {
            throw new RuntimeException("Could not find native library in classpath: " + resourcePath);
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

        System.out.println("Extracted native library to: " + tempLib.getAbsolutePath());
        System.load(tempLib.getAbsolutePath());
        System.out.println("Native library loaded successfully.");
    }
}
