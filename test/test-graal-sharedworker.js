// Simple GraalJS SharedWorker script for testing

console.log('GraalJS SharedWorker started');

var connectionCount = 0;

// Handle new connections
if (typeof onconnect !== 'undefined') {
    // onconnect is already defined - use it
} else {
    var onconnect = null;
}

// Set up connect handler
this.onconnect = function(e) {
    connectionCount++;
    console.log('New connection to shared worker. Total connections: ' + connectionCount);

    var port = e.port;

    port.onmessage = function(event) {
        console.log('Shared worker received:', event.data);

        // Echo the message back
        port.postMessage({
            echo: event.data,
            connectionNumber: connectionCount
        });
    };

    // Start the port
    port.start();
};
