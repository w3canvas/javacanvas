#!/bin/bash
set -e

echo "Running JavaFX test..."
./mvnw test -Dtest=TestJavaFX -Dtestfx.robot=glass -Dtestfx.headless=true -Dprism.order=sw
echo "JavaFX test successful."
