#!/bin/bash
set -e

echo "Running smoke test..."
# Run the test in the background
xvfb-run java -cp bin:lib/rhino.jar com.w3canvas.javacanvas.test.TestCanvas &
TEST_PID=$!

# Wait for a few seconds
sleep 5

# Check if the process is still running
if kill -0 $TEST_PID 2>/dev/null; then
  echo "Smoke test process is still running. Killing it."
  kill $TEST_PID
  echo "Smoke test successful: Application started and ran for 5 seconds without crashing."
else
  echo "Smoke test failed: Application crashed."
  exit 1
fi

echo "Running CSS parser test..."
java -cp bin:lib/rhino.jar com.w3canvas.javacanvas.test.TestCSSParser
echo "CSS parser test successful."

echo "All tests passed!"
exit 0
