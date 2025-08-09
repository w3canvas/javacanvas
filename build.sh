#!/bin/bash
set -e
mkdir -p bin
find . -name "*.java" > sources.txt
javac -d bin -cp .:lib/rhino.jar @sources.txt
rm sources.txt
cp -r META-INF bin/
echo "Build successful! Class files are in the bin directory."
