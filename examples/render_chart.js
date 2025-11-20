// Assuming 'ctx' is provided and 'canvas' size is 800x600
ctx.fillStyle = "white";
ctx.fillRect(0, 0, 800, 600);

// Draw a simple bar chart
var data = [100, 200, 150, 300, 250];
var colors = ["red", "orange", "yellow", "green", "blue"];
var barWidth = 50;
var spacing = 20;
var startX = 100;
var startY = 500;

ctx.font = "20px Arial";
ctx.textAlign = "center";
ctx.fillStyle = "black";
ctx.fillText("Sales Data", 400, 50);

for (var i = 0; i < data.length; i++) {
    var h = data[i];
    ctx.fillStyle = colors[i % colors.length];
    // Use shadow for effect
    ctx.shadowBlur = 10;
    ctx.shadowColor = "rgba(0,0,0,0.5)";
    ctx.fillRect(startX + (i * (barWidth + spacing)), startY - h, barWidth, h);

    // Reset shadow for text
    ctx.shadowBlur = 0;
    ctx.fillStyle = "black";
    ctx.fillText(h, startX + (i * (barWidth + spacing)) + (barWidth/2), startY - h - 10);
}

// Draw axes
ctx.strokeStyle = "black";
ctx.lineWidth = 2;
ctx.beginPath();
ctx.moveTo(50, 50);
ctx.lineTo(50, 500);
ctx.lineTo(750, 500);
ctx.stroke();
