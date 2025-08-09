var canvas = document.getElementById('canvas');
var ctx = canvas.getContext('2d');

// Test fillRect
ctx.fillStyle = 'red';
ctx.fillRect(10, 10, 50, 50);

// Test strokeRect
ctx.strokeStyle = 'blue';
ctx.lineWidth = 5;
ctx.strokeRect(70, 10, 50, 50);

// Test paths
ctx.beginPath();
ctx.moveTo(130, 10);
ctx.lineTo(180, 60);
ctx.lineTo(130, 60);
ctx.closePath();
ctx.stroke();

// Test text
ctx.font = '20px sans-serif';
ctx.fillText('Hello', 10, 100);

// Test line dash
ctx.setLineDash([5, 5]);
ctx.beginPath();
ctx.moveTo(10, 120);
ctx.lineTo(180, 120);
ctx.stroke();

// Test roundRect
ctx.roundRect(10, 140, 50, 50, 10);
ctx.fill();

// Test ellipse
ctx.ellipse(120, 165, 50, 25, Math.PI / 4, 0, 2 * Math.PI);
ctx.stroke();

console.log("Canvas 2D tests completed.");

// Test globalCompositeOperation
ctx.globalCompositeOperation = 'copy';
ctx.fillStyle = 'rgba(0, 0, 255, 0.5)';
ctx.fillRect(10, 10, 50, 50);
ctx.globalCompositeOperation = 'source-over'; // reset to default

console.log("Additional canvas tests completed.");
