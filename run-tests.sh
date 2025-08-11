#!/bin/bash
set -e

echo "Running all tests under xvfb..."
xvfb-run --auto-servernum ./mvnw test
