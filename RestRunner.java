///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS org.openjfx:javafx-controls:17.0.13:${os.detected.jfxname}
//DEPS org.openjfx:javafx-graphics:17.0.13:${os.detected.jfxname}
//DEPS org.openjfx:javafx-base:17.0.13:${os.detected.jfxname}
//DEPS org.openjfx:javafx-media:17.0.13:${os.detected.jfxname}
//DEPS org.openjfx:javafx-swing:17.0.13:${os.detected.jfxname}
//DEPS org.mozilla:rhino:1.7.14
//DEPS org.graalvm.polyglot:polyglot:24.1.0
//DEPS org.graalvm.js:js-language:24.1.0
//DEPS org.nanohttpd:nanohttpd:2.3.1
//DEPS com.github.xpenatan.jWebGPU:webgpu-core:0.1.12
//DEPS com.github.xpenatan.jWebGPU:webgpu-desktop:0.1.12:windows_64_wgpu
//REPOS https://oss.sonatype.org/content/repositories/releases/
//SOURCES src/main/java/**/*.java

package com.w3canvas.javacanvas;

import com.w3canvas.javacanvas.server.RenderingServer;
import java.io.IOException;

public class RestRunner {
    public static void main(String... args) throws IOException {
        RenderingServer.main(args);
    }
}
