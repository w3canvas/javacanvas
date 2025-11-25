///usr/bin/env jbang "$0" "$@" ; exit $?
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

import com.w3canvas.javacanvas.Main;

public class JBangRunner {
    public static void main(String... args) {
        Main.main(args);
    }
}
