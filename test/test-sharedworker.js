// Shared worker script
var connectionCount = 0;

onconnect = function(e) {
    var port = e.ports[0];
    connectionCount++;

    console.log('New connection to shared worker. Total connections:', connectionCount);

    port.onmessage = function(event) {
        var data = event.data;
        console.log('Shared worker received:', data);

        if (data.command === 'getImageData') {
            // Create an OffscreenCanvas and draw a blue rectangle
            var canvas = new OffscreenCanvas(100, 100);
            var ctx = canvas.getContext('2d');

            ctx.fillStyle = '#008000'; // Green
            ctx.fillRect(0, 0, 100, 100);

            var imageData = ctx.getImageData(0, 0, 100, 100);

            // Send back the image data
            port.postMessage({
                type: 'imageData',
                imageData: imageData,
                connectionId: connectionCount
            });
        } else {
            // Echo back any other message
            port.postMessage({
                echo: data,
                connectionId: connectionCount
            });
        }
    };

    port.start();
};
