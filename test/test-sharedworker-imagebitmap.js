// Shared worker script for ImageBitmap test
onconnect = function(e) {
    var port = e.ports[0];

    console.log('ImageBitmap worker connected');

    port.onmessage = function(event) {
        var data = event.data;
        console.log('ImageBitmap worker received:', data);

        if (data.command === 'create') {
            // Create an OffscreenCanvas with the specified dimensions
            var canvas = new OffscreenCanvas(data.width, data.height);
            var ctx = canvas.getContext('2d');

            // Fill with the specified color
            ctx.fillStyle = data.color;
            ctx.fillRect(0, 0, data.width, data.height);

            // Create ImageBitmap using the global createImageBitmap function
            var imageBitmap = createImageBitmap(canvas);

            console.log('Created ImageBitmap:', imageBitmap.width, 'x', imageBitmap.height);

            // Send back the ImageBitmap
            port.postMessage(imageBitmap);
        }
    };

    port.start();
};
