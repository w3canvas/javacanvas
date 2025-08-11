// In the worker
var canvas = new OffscreenCanvas(100, 100);
var ctx = canvas.getContext('2d');

ctx.fillStyle = '#008000';
ctx.fillRect(0, 0, 100, 100);

var imageData = ctx.getImageData(0, 0, 100, 100);

postMessage(imageData);
