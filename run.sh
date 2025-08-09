#!/bin/bash
set -e
xvfb-run java -cp bin:lib/rhino.jar com.w3canvas.javacanvas.rt.JavaCanvas
