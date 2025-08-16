var canvas = document.getElementById('canvas');
if (!canvas) {
    // If we are in a pure JS environment for testing, we might need to create the canvas.
    // In the Java environment, this should be provided.
    canvas = document.createElement('canvas');
    canvas.id = 'canvas';
    canvas.width = 400;
    canvas.height = 400;
}
var ctx = canvas.getContext('2d');

// Clear the canvas to a known state (transparent black)
ctx.clearRect(0, 0, canvas.width, canvas.height);

// Test fillRect
ctx.fillStyle = 'red';
ctx.fillRect(10, 10, 50, 50);
test.assertPixel(20, 20, 255, 0, 0, 255); // Check inside the rect
test.assertPixel(5, 5, 0, 0, 0, 0);       // Check outside the rect

// Test strokeRect
ctx.strokeStyle = 'blue';
ctx.lineWidth = 5;
ctx.strokeRect(70, 10, 50, 50);
test.assertPixel(95, 10, 0, 0, 255, 255); // Check on the stroke

// Test paths
ctx.beginPath();
ctx.moveTo(130, 10);
ctx.lineTo(180, 60);
ctx.lineTo(130, 60);
ctx.closePath();
ctx.strokeStyle = 'green';
ctx.lineWidth = 1;
ctx.stroke();
// This is harder to test with a single pixel check due to anti-aliasing.
// We'll skip pixel assertion for this path for now.

// Test text
ctx.font = '20px sans-serif';
ctx.fillStyle = 'purple';
ctx.fillText('Hello', 10, 100);
// Text rendering is also very complex to test with single pixels due to anti-aliasing and font metrics.
// We will trust that it doesn't crash for now.

// Test globalCompositeOperation
ctx.globalCompositeOperation = 'copy';
ctx.fillStyle = 'rgba(0, 0, 255, 0.5)';
ctx.fillRect(10, 10, 20, 20);
// The 'copy' operation should replace the underlying pixels.
// The color is semi-transparent blue, but since it's copying, the background is ignored.
// The final color will be the source color, rgba(0, 0, 255, 127)
test.assertPixel(15, 15, 0, 0, 255, 128);

// Reset composite operation
ctx.globalCompositeOperation = 'source-over';

console.log("Canvas 2D tests completed.");

test.testComplete();
