function onBlue() {
    sendTogglePost();
    console.log("blue");
}

function onRed() {
    sendTogglePost();
    console.log("red");
}

function sendTogglePost() {
    var request = new XMLHttpRequest();
    request.onload = function() {
        console.log(request.status)
    };
    request.open("POST", "http://localhost:8080/toggle", true);
    request.send();
}