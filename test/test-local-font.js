var f = new FontFace('LocalFont', 'url(fonts/DejaVuSans.ttf)');
document.fonts.add(f);
f.load().then(function (loadedFace) {
    console.log('Font loaded: ' + loadedFace.status);
}, function (err) {
    console.log('Font error: ' + err);
});
