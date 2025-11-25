var canvas = document.createElement('canvas');
canvas.width = 800;
canvas.height = 600;
var ctx = canvas.getContext('2d');

ctx.fillStyle = 'blue';
ctx.fillRect(10, 10, 100, 100);

console.log("Hello from JBang! Drew a blue rectangle.");
