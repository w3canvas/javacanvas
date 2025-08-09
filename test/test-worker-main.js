var worker = new Worker('test-worker.js');

worker.onmessage = function(e) {
    var imageData = e.data;
    var canvas = document.getElementById('canvas');
    var ctx = canvas.getContext('2d');
    ctx.putImageData(imageData, 0, 0);
    console.log("Image data received from worker and drawn on canvas.");
};
