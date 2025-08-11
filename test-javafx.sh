#!/bin/bash
set -e

echo "Running JavaFX test..."
./mvnw test -Dtest=TestJavaFX
echo "JavaFX test successful."
