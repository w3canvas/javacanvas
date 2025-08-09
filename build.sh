#!/bin/bash
mkdir -p bin
find . -name "*.java" > sources.txt
trap "rm sources.txt" EXIT
javac -d bin -cp .:lib/rhino.jar -Xlint:deprecation -Xlint:unchecked @sources.txt
cp -r META-INF bin/
echo "Build successful! Class files are in the bin directory."
