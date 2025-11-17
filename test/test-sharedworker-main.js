// Main thread script for SharedWorker test
var worker = new SharedWorker('test-sharedworker.js');

worker.port.onmessage = function(e) {
    console.log('Main thread received:', e.data);

    if (e.data.type === 'imageData') {
        var imageData = e.data.imageData;
        var canvas = document.getElementById('canvas');
        var ctx = canvas.getContext('2d');
        ctx.putImageData(imageData, 0, 0);
        console.log("Image data received from shared worker and drawn on canvas.");
    }
};

// Request image data from the shared worker
worker.port.postMessage({command: 'getImageData'});
