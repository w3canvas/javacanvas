/**
 * ImageBitmap Sprite Example
 *
 * This example demonstrates using ImageBitmap for efficient sprite rendering.
 * ImageBitmap objects are immutable and optimized for drawing, making them
 * ideal for game sprites or repeated rendering operations.
 */

// Create main canvas
var canvas = document.getElementById('canvas');
if (!canvas) {
    canvas = document.createElement('canvas');
    canvas.id = 'canvas';
    canvas.width = 600;
    canvas.height = 400;
}
var ctx = canvas.getContext('2d');

// Clear canvas
ctx.fillStyle = 'white';
ctx.fillRect(0, 0, canvas.width, canvas.height);

print('ImageBitmap Sprite Example');
print('==========================\n');

// Step 1: Create a sprite sheet using a temporary canvas
print('Step 1: Creating sprite sheet...');
var spriteSheet = document.createElement('canvas');
spriteSheet.width = 200;
spriteSheet.height = 50;
var spriteCtx = spriteSheet.getContext('2d');

// Draw 4 different colored circles as sprites
var colors = ['red', 'green', 'blue', 'yellow'];
for (var i = 0; i < 4; i++) {
    spriteCtx.fillStyle = colors[i];
    spriteCtx.beginPath();
    spriteCtx.arc(25 + i * 50, 25, 20, 0, 2 * Math.PI);
    spriteCtx.fill();
    spriteCtx.strokeStyle = 'black';
    spriteCtx.lineWidth = 2;
    spriteCtx.stroke();
}
print('  Created sprite sheet with 4 colored circles\n');

// Step 2: Convert sprite sheet to ImageBitmap for optimal performance
print('Step 2: Converting to ImageBitmap...');
var spriteImageBitmap = createImageBitmap(spriteSheet);
print('  ImageBitmap created: ' + spriteImageBitmap.width + 'x' + spriteImageBitmap.height + '\n');

// Step 3: Draw sprites using ImageBitmap
print('Step 3: Drawing sprites using ImageBitmap...');
print('  Drawing original sprite sheet at top');
ctx.drawImage(spriteImageBitmap, 10, 10);

// Draw individual sprites by using the source rectangle parameters
print('  Drawing individual sprites extracted from bitmap:');
for (var i = 0; i < 4; i++) {
    // Extract sprite from bitmap: sx, sy, sw, sh, dx, dy, dw, dh
    var x = 50 + i * 80;
    var y = 100;
    ctx.drawImage(spriteImageBitmap, i * 50, 0, 50, 50, x, y, 60, 60);
    print('    Sprite ' + (i + 1) + ' (' + colors[i] + ') at (' + x + ', ' + y + ')');
}

// Step 4: Demonstrate scaling with ImageBitmap
print('\nStep 4: Demonstrating scaling...');
var scaledY = 200;
ctx.drawImage(spriteImageBitmap, 0, 0, 50, 50, 50, scaledY, 100, 100);  // 2x scale
ctx.drawImage(spriteImageBitmap, 50, 0, 50, 50, 160, scaledY, 50, 50);   // 1x scale
ctx.drawImage(spriteImageBitmap, 100, 0, 50, 50, 220, scaledY, 25, 25);  // 0.5x scale
print('  Drew same sprite at 2x, 1x, and 0.5x scales\n');

// Step 5: Create ImageBitmap from ImageData for procedural sprites
print('Step 5: Creating procedural sprite from ImageData...');
var size = 40;
var imageData = ctx.createImageData(size, size);

// Create a gradient pattern
for (var y = 0; y < size; y++) {
    for (var x = 0; x < size; x++) {
        var index = (y * size + x) * 4;
        imageData.data[index] = x * 6;       // R
        imageData.data[index + 1] = y * 6;   // G
        imageData.data[index + 2] = 128;     // B
        imageData.data[index + 3] = 255;     // A
    }
}

var proceduralBitmap = createImageBitmap(imageData);
print('  Created procedural gradient sprite: ' + proceduralBitmap.width + 'x' + proceduralBitmap.height);

// Draw the procedural sprite multiple times with transformations
ctx.save();
for (var i = 0; i < 5; i++) {
    var rotation = i * 0.3;
    var x = 350 + i * 50;
    var y = 250;

    ctx.save();
    ctx.translate(x + 20, y + 20);
    ctx.rotate(rotation);
    ctx.drawImage(proceduralBitmap, -20, -20);
    ctx.restore();
}
ctx.restore();
print('  Drew procedural sprite 5 times with rotation\n');

// Step 6: Demonstrate ImageBitmap resource management
print('Step 6: Resource management with close()...');
var tempCanvas = document.createElement('canvas');
tempCanvas.width = 30;
tempCanvas.height = 30;
var tempCtx = tempCanvas.getContext('2d');
tempCtx.fillStyle = 'purple';
tempCtx.fillRect(0, 0, 30, 30);

var tempBitmap = createImageBitmap(tempCanvas);
print('  Created temp bitmap: ' + tempBitmap.width + 'x' + tempBitmap.height);
ctx.drawImage(tempBitmap, 500, 10);
print('  Drew temp bitmap before close()');

tempBitmap.close();
print('  Called close() - bitmap dimensions now: ' + tempBitmap.width + 'x' + tempBitmap.height);
print('  (Note: Attempting to draw after close() may fail or draw nothing)\n');

print('=== Example Complete ===');
print('\nImageBitmap Benefits:');
print('  - Immutable: Safe to share and reuse');
print('  - Optimized: Better performance for repeated drawing');
print('  - Resource Management: Explicit cleanup with close()');
print('  - Flexible: Create from canvas, ImageData, or other bitmaps');
