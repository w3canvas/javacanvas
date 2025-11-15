try {
    var canvas = new OffscreenCanvas(100, 100);
    print("OffscreenCanvas created successfully");
    print("Canvas: " + canvas);
    var ctx = canvas.getContext('2d');
    print("Context: " + ctx);
} catch (e) {
    print("ERROR: " + e);
    print("Stack: " + e.stack);
}
