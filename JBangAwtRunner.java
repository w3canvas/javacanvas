///usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 17+
//DEPS org.openjfx:javafx-controls:21.0.8:${os.detected.jfxname}
//DEPS org.openjfx:javafx-graphics:21.0.8:${os.detected.jfxname}
//DEPS org.openjfx:javafx-base:21.0.8:${os.detected.jfxname}
//DEPS org.openjfx:javafx-media:21.0.8:${os.detected.jfxname}
//DEPS org.openjfx:javafx-swing:21.0.8:${os.detected.jfxname}
//DEPS org.mozilla:rhino:1.7.14
//DEPS org.graalvm.polyglot:polyglot:24.1.0
//DEPS org.graalvm.polyglot:js-community:24.1.0@pom
//DEPS io.github.humbleui:jwebgpu:0.1.2
//SOURCES src/main/java/**/*.java
//FILES .=src/main/resources

package com.w3canvas.javacanvas;

/**
 * JBang runner for AWT/Swing backend testing.
 *
 * Forces AWT backend instead of JavaFX for UI operations.
 * Useful for headless testing or systems where JavaFX rendering
 * is problematic.
 *
 * Usage: jbang JBangAwtRunner.java [script.js]
 * With GraalJS: jbang JBangAwtRunner.java --graal script.js
 */
public class JBangAwtRunner {
    public static void main(String... args) {
        // Force AWT/Swing backend
        System.setProperty("javacanvas.backend", "awt");
        System.setProperty("java.awt.headless", "true");
        Main.main(args);
    }
}
