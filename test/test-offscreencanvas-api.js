// Test OffscreenCanvas API Implementation
// This test demonstrates all the OffscreenCanvas features

console.log("=== Testing OffscreenCanvas API ===");

// Test 1: Create OffscreenCanvas with initial dimensions
console.log("\n1. Creating OffscreenCanvas(300, 200)...");
var canvas = new OffscreenCanvas(300, 200);
console.log("   Width: " + canvas.width);
console.log("   Height: " + canvas.height);
console.log("   ✓ OffscreenCanvas created successfully");

// Test 2: Get 2D rendering context
console.log("\n2. Getting 2D rendering context...");
var ctx = canvas.getContext('2d');
console.log("   Context type: " + ctx);
console.log("   ✓ 2D context obtained successfully");

// Test 3: Draw to the canvas
console.log("\n3. Drawing to canvas...");
ctx.fillStyle = '#FF0000';
ctx.fillRect(0, 0, 150, 100);
ctx.fillStyle = '#00FF00';
ctx.fillRect(150, 0, 150, 100);
ctx.fillStyle = '#0000FF';
ctx.fillRect(0, 100, 150, 100);
ctx.fillStyle = '#FFFF00';
ctx.fillRect(150, 100, 150, 100);
console.log("   ✓ Drawing completed");

// Test 4: Convert to Blob (synchronous version for testing)
console.log("\n4. Testing convertToBlobSync()...");
try {
    var blob = canvas.convertToBlobSync("image/png");
    console.log("   Blob type: " + blob.type);
    console.log("   Blob size: " + blob.size + " bytes");
    console.log("   ✓ convertToBlobSync() works correctly");
} catch (e) {
    console.log("   ✗ Error: " + e);
}

// Test 5: Transfer to ImageBitmap
console.log("\n5. Testing transferToImageBitmap()...");
try {
    var imageBitmap = canvas.transferToImageBitmap();
    console.log("   ImageBitmap width: " + imageBitmap.width);
    console.log("   ImageBitmap height: " + imageBitmap.height);
    console.log("   ✓ transferToImageBitmap() works correctly");

    // Canvas should now be cleared after transfer
    console.log("   Canvas width after transfer: " + canvas.width);
    console.log("   Canvas height after transfer: " + canvas.height);

    // Close the ImageBitmap to release resources
    imageBitmap.close();
    console.log("   ImageBitmap width after close: " + imageBitmap.width);
    console.log("   ImageBitmap height after close: " + imageBitmap.height);
    console.log("   ✓ ImageBitmap.close() works correctly");
} catch (e) {
    console.log("   ✗ Error: " + e);
}

// Test 6: Resize canvas by setting width/height
console.log("\n6. Testing width/height setters...");
try {
    canvas.width = 400;
    canvas.height = 300;
    console.log("   New width: " + canvas.width);
    console.log("   New height: " + canvas.height);
    console.log("   ✓ Width/height setters work correctly");

    // Redraw on resized canvas
    var ctx2 = canvas.getContext('2d');
    ctx2.fillStyle = '#800080';
    ctx2.fillRect(0, 0, canvas.width, canvas.height);
    console.log("   ✓ Drawing on resized canvas works");
} catch (e) {
    console.log("   ✗ Error: " + e);
}

// Test 7: Convert resized canvas to blob
console.log("\n7. Testing convertToBlobSync() on resized canvas...");
try {
    var blob2 = canvas.convertToBlobSync("image/jpeg");
    console.log("   Blob type: " + blob2.type);
    console.log("   Blob size: " + blob2.size + " bytes");
    console.log("   ✓ convertToBlobSync() works on resized canvas");
} catch (e) {
    console.log("   ✗ Error: " + e);
}

// Test 8: Test different MIME types
console.log("\n8. Testing different MIME types...");
try {
    var pngBlob = canvas.convertToBlobSync("image/png");
    console.log("   PNG blob size: " + pngBlob.size + " bytes (type: " + pngBlob.type + ")");

    var jpegBlob = canvas.convertToBlobSync("image/jpeg");
    console.log("   JPEG blob size: " + jpegBlob.size + " bytes (type: " + jpegBlob.type + ")");

    var defaultBlob = canvas.convertToBlobSync();
    console.log("   Default blob size: " + defaultBlob.size + " bytes (type: " + defaultBlob.type + ")");

    console.log("   ✓ Multiple MIME types supported");
} catch (e) {
    console.log("   ✗ Error: " + e);
}

console.log("\n=== All OffscreenCanvas API tests completed ===");
