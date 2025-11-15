// Test ImageBitmap API implementation
// This tests the core ImageBitmap functionality according to HTML5 Canvas spec

var canvas = document.getElementById('canvas');
if (!canvas) {
    canvas = document.createElement('canvas');
    canvas.id = 'canvas';
    canvas.width = 400;
    canvas.height = 400;
}
var ctx = canvas.getContext('2d');

// Clear the canvas
ctx.clearRect(0, 0, canvas.width, canvas.height);

// Test 1: Create ImageBitmap from canvas
print('Test 1: Creating ImageBitmap from HTMLCanvasElement');
ctx.fillStyle = 'red';
ctx.fillRect(0, 0, 100, 100);
var imageBitmap1 = createImageBitmap(canvas);
print('  ImageBitmap width: ' + imageBitmap1.width);
print('  ImageBitmap height: ' + imageBitmap1.height);
print('  Expected: width=400, height=400');
if (imageBitmap1.width === 400 && imageBitmap1.height === 400) {
    print('  PASS: ImageBitmap created from canvas with correct dimensions');
} else {
    print('  FAIL: ImageBitmap dimensions incorrect');
}

// Test 2: Draw ImageBitmap to canvas
print('\nTest 2: Drawing ImageBitmap to canvas');
ctx.clearRect(0, 0, canvas.width, canvas.height);
ctx.fillStyle = 'blue';
ctx.fillRect(0, 0, canvas.width, canvas.height);

var canvas2 = document.createElement('canvas');
canvas2.width = 50;
canvas2.height = 50;
var ctx2 = canvas2.getContext('2d');
ctx2.fillStyle = 'green';
ctx2.fillRect(0, 0, 50, 50);

var imageBitmap2 = createImageBitmap(canvas2);
ctx.drawImage(imageBitmap2, 10, 10);
print('  Drew 50x50 green ImageBitmap at (10, 10) on blue canvas');
print('  PASS: drawImage with ImageBitmap executed without error');

// Test 3: Create ImageBitmap from ImageData
print('\nTest 3: Creating ImageBitmap from ImageData');
var imageData = ctx.createImageData(30, 30);
// Fill with semi-transparent yellow
for (var i = 0; i < imageData.data.length; i += 4) {
    imageData.data[i] = 255;     // R
    imageData.data[i + 1] = 255; // G
    imageData.data[i + 2] = 0;   // B
    imageData.data[i + 3] = 128; // A (semi-transparent)
}
var imageBitmap3 = createImageBitmap(imageData);
print('  ImageBitmap width: ' + imageBitmap3.width);
print('  ImageBitmap height: ' + imageBitmap3.height);
if (imageBitmap3.width === 30 && imageBitmap3.height === 30) {
    print('  PASS: ImageBitmap created from ImageData with correct dimensions');
} else {
    print('  FAIL: ImageBitmap dimensions incorrect');
}

// Draw the ImageBitmap from ImageData
ctx.drawImage(imageBitmap3, 100, 10);
print('  Drew ImageBitmap from ImageData at (100, 10)');

// Test 4: Create ImageBitmap from another ImageBitmap (copy)
print('\nTest 4: Creating ImageBitmap from another ImageBitmap');
var imageBitmap4 = createImageBitmap(imageBitmap3);
print('  ImageBitmap width: ' + imageBitmap4.width);
print('  ImageBitmap height: ' + imageBitmap4.height);
if (imageBitmap4.width === 30 && imageBitmap4.height === 30) {
    print('  PASS: ImageBitmap copied from another ImageBitmap with correct dimensions');
} else {
    print('  FAIL: ImageBitmap dimensions incorrect');
}
ctx.drawImage(imageBitmap4, 150, 10);
print('  Drew copied ImageBitmap at (150, 10)');

// Test 5: ImageBitmap.close()
print('\nTest 5: Testing ImageBitmap.close()');
var imageBitmap5 = createImageBitmap(canvas2);
print('  Before close() - width: ' + imageBitmap5.width + ', height: ' + imageBitmap5.height);
imageBitmap5.close();
print('  After close() - width: ' + imageBitmap5.width + ', height: ' + imageBitmap5.height);
if (imageBitmap5.width === 0 && imageBitmap5.height === 0) {
    print('  PASS: close() properly releases resources, width and height are 0');
} else {
    print('  FAIL: close() did not reset dimensions to 0');
}

// Test 6: Create ImageBitmap from Image element (if supported)
print('\nTest 6: Creating ImageBitmap from HTMLImageElement');
try {
    var img = document.createElement('img');
    // In a real browser, you'd need to load an actual image
    // For this test, we'll just try to create an ImageBitmap from an empty img element
    // This might fail, which is okay for this test
    print('  Image element creation test - skipped (requires actual image data)');
} catch (e) {
    print('  Image element test skipped: ' + e);
}

// Test 7: Test drawImage with different signatures
print('\nTest 7: Testing drawImage with ImageBitmap using different signatures');
ctx.clearRect(0, 0, canvas.width, canvas.height);

var testCanvas = document.createElement('canvas');
testCanvas.width = 100;
testCanvas.height = 100;
var testCtx = testCanvas.getContext('2d');
testCtx.fillStyle = 'orange';
testCtx.fillRect(0, 0, 100, 100);
var testBitmap = createImageBitmap(testCanvas);

// drawImage(image, dx, dy)
ctx.drawImage(testBitmap, 10, 10);
print('  drawImage(bitmap, 10, 10) - PASS');

// drawImage(image, dx, dy, dWidth, dHeight)
ctx.drawImage(testBitmap, 120, 10, 50, 50);
print('  drawImage(bitmap, 120, 10, 50, 50) - PASS (scaled)');

// drawImage(image, sx, sy, sWidth, sHeight, dx, dy, dWidth, dHeight)
ctx.drawImage(testBitmap, 25, 25, 50, 50, 200, 10, 75, 75);
print('  drawImage(bitmap, 25, 25, 50, 50, 200, 10, 75, 75) - PASS (cropped and scaled)');

print('\n=== All ImageBitmap tests completed ===');
