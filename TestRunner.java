///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS org.junit.platform:junit-platform-console-standalone:1.10.1
//DEPS org.openjfx:javafx-controls:17.0.13:${os.detected.jfxname}
//DEPS org.openjfx:javafx-graphics:17.0.13:${os.detected.jfxname}
//DEPS org.openjfx:javafx-base:17.0.13:${os.detected.jfxname}
//DEPS org.openjfx:javafx-media:17.0.13:${os.detected.jfxname}
//DEPS org.openjfx:javafx-swing:17.0.13:${os.detected.jfxname}
//DEPS org.mozilla:rhino:1.7.14
//DEPS org.graalvm.polyglot:polyglot:24.1.0
//DEPS org.graalvm.js:js-language:24.1.0
//DEPS org.testfx:testfx-core:4.0.18
//DEPS org.testfx:testfx-junit5:4.0.18
//DEPS org.testfx:openjfx-monocle:17.0.10
//DEPS org.mockito:mockito-core:5.18.0
//DEPS org.mockito:mockito-junit-jupiter:5.18.0
//DEPS org.hamcrest:hamcrest:2.2
//DEPS org.nanohttpd:nanohttpd:2.3.1
//SOURCES src/main/java/**/*.java
//SOURCES src/test/java/**/*.java

import org.junit.platform.console.ConsoleLauncher;

public class TestRunner {
    public static void main(String... args) {
        // Default to running all tests if no args provided
        if (args.length == 0) {
            args = new String[] { "--scan-classpath" };
        } else {
            // If args provided (e.g. class name), format for ConsoleLauncher
            // Example: jbang TestRunner.java -c com.w3canvas.javacanvas.test.TestGraal
            // If user just passes "TestGraal", we'll try to be smart
            String testName = args[0];
            if (!testName.startsWith("-")) {
                if (!testName.contains(".")) {
                    testName = "com.w3canvas.javacanvas.test." + testName;
                }
                // Add verbose details for specific tests
                args = new String[] { "-c", testName, "--details=verbose" };
            }
        }

        // Set headless properties for JavaFX/AWT
        System.setProperty("java.awt.headless", "true");
        System.setProperty("testfx.robot", "glass");
        System.setProperty("testfx.headless", "true");
        System.setProperty("prism.order", "sw");
        System.setProperty("prism.text", "t2k");

        // Prepend "execute" command to satisfy deprecation warning
        String[] newArgs = new String[args.length + 1];
        newArgs[0] = "execute";
        System.arraycopy(args, 0, newArgs, 1, args.length);

        ConsoleLauncher.main(newArgs);
    }
}
