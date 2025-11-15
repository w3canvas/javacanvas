// Test CSS Filter Effects Implementation
var canvas = document.createElement('canvas');
canvas.width = 400;
canvas.height = 400;
var ctx = canvas.getContext('2d');

console.log('Testing CSS Filter Effects...\n');

// Test 1: Default filter (none)
console.log('Test 1: Default filter');
console.log('  Expected: "none"');
console.log('  Actual: "' + ctx.filter + '"');
console.log('  Pass: ' + (ctx.filter === 'none') + '\n');

// Test 2: Set blur filter
ctx.filter = 'blur(5px)';
console.log('Test 2: Set blur filter');
console.log('  Expected: "blur(5px)"');
console.log('  Actual: "' + ctx.filter + '"');
console.log('  Pass: ' + (ctx.filter === 'blur(5px)') + '\n');

// Test 3: Set brightness filter
ctx.filter = 'brightness(150%)';
console.log('Test 3: Set brightness filter');
console.log('  Expected: "brightness(150%)"');
console.log('  Actual: "' + ctx.filter + '"');
console.log('  Pass: ' + (ctx.filter === 'brightness(150%)') + '\n');

// Test 4: Set multiple filters
ctx.filter = 'blur(3px) brightness(120%) contrast(1.5)';
console.log('Test 4: Set multiple filters');
console.log('  Expected: "blur(3px) brightness(120%) contrast(1.5)"');
console.log('  Actual: "' + ctx.filter + '"');
console.log('  Pass: ' + (ctx.filter === 'blur(3px) brightness(120%) contrast(1.5)') + '\n');

// Test 5: Save and restore filter
ctx.filter = 'grayscale(100%)';
ctx.save();
ctx.filter = 'sepia(50%)';
console.log('Test 5: Save and restore filter');
console.log('  Before restore: "' + ctx.filter + '"');
ctx.restore();
console.log('  After restore: "' + ctx.filter + '"');
console.log('  Pass: ' + (ctx.filter === 'grayscale(100%)') + '\n');

// Test 6: Reset filter to none
ctx.filter = 'none';
console.log('Test 6: Reset filter to none');
console.log('  Expected: "none"');
console.log('  Actual: "' + ctx.filter + '"');
console.log('  Pass: ' + (ctx.filter === 'none') + '\n');

// Test 7: Visual test - draw shapes with different filters
console.log('Test 7: Visual rendering test');
console.log('  Drawing shapes with various filters...');

// Clear canvas
ctx.clearRect(0, 0, canvas.width, canvas.height);

// No filter - red rectangle
ctx.filter = 'none';
ctx.fillStyle = 'red';
ctx.fillRect(10, 10, 80, 80);

// Blur filter - green rectangle
ctx.filter = 'blur(5px)';
ctx.fillStyle = 'green';
ctx.fillRect(110, 10, 80, 80);

// Brightness filter - blue rectangle
ctx.filter = 'brightness(150%)';
ctx.fillStyle = 'blue';
ctx.fillRect(210, 10, 80, 80);

// Grayscale filter - yellow rectangle
ctx.filter = 'grayscale(100%)';
ctx.fillStyle = 'yellow';
ctx.fillRect(310, 10, 80, 80);

// Contrast filter - purple rectangle
ctx.filter = 'contrast(200%)';
ctx.fillStyle = 'purple';
ctx.fillRect(10, 110, 80, 80);

// Sepia filter - orange rectangle
ctx.filter = 'sepia(100%)';
ctx.fillStyle = 'orange';
ctx.fillRect(110, 110, 80, 80);

// Saturate filter - cyan rectangle
ctx.filter = 'saturate(200%)';
ctx.fillStyle = 'cyan';
ctx.fillRect(210, 110, 80, 80);

// Hue-rotate filter - pink rectangle
ctx.filter = 'hue-rotate(90deg)';
ctx.fillStyle = 'pink';
ctx.fillRect(310, 110, 80, 80);

// Invert filter - teal rectangle
ctx.filter = 'invert(100%)';
ctx.fillStyle = 'teal';
ctx.fillRect(10, 210, 80, 80);

// Opacity filter - brown rectangle
ctx.filter = 'opacity(50%)';
ctx.fillStyle = 'brown';
ctx.fillRect(110, 210, 80, 80);

// Multiple filters - magenta rectangle
ctx.filter = 'blur(2px) brightness(120%) contrast(1.5)';
ctx.fillStyle = 'magenta';
ctx.fillRect(210, 210, 80, 80);

// Reset for text
ctx.filter = 'none';
ctx.fillStyle = 'black';
ctx.font = '12px sans-serif';
ctx.fillText('No Filter', 20, 105);
ctx.fillText('Blur', 130, 105);
ctx.fillText('Brightness', 215, 105);
ctx.fillText('Grayscale', 315, 105);
ctx.fillText('Contrast', 20, 205);
ctx.fillText('Sepia', 130, 205);
ctx.fillText('Saturate', 220, 205);
ctx.fillText('Hue-rotate', 315, 205);
ctx.fillText('Invert', 20, 305);
ctx.fillText('Opacity', 120, 305);
ctx.fillText('Multiple', 220, 305);

console.log('  Visual test complete!\n');

// Save canvas
var image = canvas.toDataURL();
console.log('Canvas saved. All tests completed!');
