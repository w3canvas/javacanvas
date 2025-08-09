// In the worker
var canvas = new OffscreenCanvas(100, 100);
var ctx = canvas.getContext('2d');

ctx.fillStyle = 'green';
ctx.fillRect(0, 0, 100, 100);

var imageData = ctx.getImageData(0, 0, 100, 100);

postMessage(imageData);
