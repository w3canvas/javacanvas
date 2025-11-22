var canvas = document.createElement('canvas');
canvas.width = 200;
canvas.height = 200;
var ctx = canvas.getContext('2d');

// Test Filter on Stroke
ctx.lineWidth = 10;
ctx.strokeStyle = 'red';
ctx.filter = 'blur(5px)';
ctx.beginPath();
ctx.moveTo(20, 20);
ctx.lineTo(20, 180);
ctx.stroke();
console.log("Stroke with filter executed");

// Test Pattern Transform
var pCanvas = document.createElement('canvas');
pCanvas.width = 20;
pCanvas.height = 20;
var pCtx = pCanvas.getContext('2d');
pCtx.fillStyle = 'blue';
pCtx.fillRect(0, 0, 10, 10);
pCtx.fillRect(10, 10, 10, 10);

var pattern = ctx.createPattern(pCanvas, 'repeat');

ctx.save();
ctx.scale(2, 2);
var matrix = ctx.getTransform();
ctx.restore();

if (pattern.setTransform) {
    pattern.setTransform(matrix);
    console.log("Pattern setTransform executed");
} else {
    console.log("Pattern setTransform NOT found");
}

ctx.fillStyle = pattern;
ctx.fillRect(50, 0, 150, 200);
console.log("Pattern fill executed");

// Test Text Direction
ctx.resetTransform();
ctx.font = "20px sans-serif";
ctx.direction = "rtl";
ctx.textAlign = "start";
ctx.fillStyle = "black";
ctx.fillText("RTL Text", 150, 150);
console.log("RTL Text executed");

ctx.direction = "ltr";
ctx.fillText("LTR Text", 150, 180);
console.log("LTR Text executed");
