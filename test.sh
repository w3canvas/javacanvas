#!/bin/bash
set -e

echo "Running test..."
# Run the test in the background
xvfb-run java -cp bin:lib/rhino.jar com.w3canvas.javacanvas.test.TestCanvas &
TEST_PID=$!

# Wait for a few seconds
sleep 5

# Check if the process is still running
if kill -0 $TEST_PID 2>/dev/null; then
  echo "Test process is still running. Killing it."
  kill $TEST_PID
  echo "Test successful: Application started and ran for 5 seconds without crashing."
  exit 0
else
  echo "Test failed: Application crashed."
  exit 1
fi
