///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS org.openjfx:javafx-controls:17.0.8:${os.detected.jfxname}
//DEPS org.openjfx:javafx-graphics:17.0.8:${os.detected.jfxname}
//DEPS org.openjfx:javafx-base:17.0.8:${os.detected.jfxname}
//DEPS org.openjfx:javafx-media:17.0.8:${os.detected.jfxname}
//DEPS org.openjfx:javafx-swing:17.0.8:${os.detected.jfxname}
//DEPS org.mozilla:rhino:1.7.14
//DEPS org.graalvm.js:js:23.0.0
//DEPS org.graalvm.sdk:graal-sdk:23.0.0
//SOURCES src/main/java/**/*.java

package com.w3canvas.javacanvas;

import com.w3canvas.javacanvas.Main;

public class JBangRunner {
    public static void main(String... args) {
        Main.main(args);
    }
}
