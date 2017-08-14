var socket = null;

function connectWebSocket() {
    console.log("begin connect");
    socket = new WebSocket("ws://" + window.location.host + "/ws");

    socket.onerror = function() {
        console.log("socket error");
    };

    socket.onopen = function() {
        console.log("socket opened");
    };

    socket.onclose = function() {
        console.log("socket closed");
        setTimeout(connectWebSocket, 5000);
    };

    socket.onmessage = function(event) {
        var msg = event.data.toString();
        console.log(msg);
    };
}
window.addEventListener("load", connectWebSocket);

function onBlue() {
    sendTogglePost();
    console.log("onBlue");
}

function onRed() {
    sendTogglePost();
    console.log("onRed");
}

function sendTogglePost() {
    var request = new XMLHttpRequest();
    request.onload = function() {
        console.log(request.status)
    };
    request.open("POST", "http://" + window.location.host + "/toggle", true);
    request.send();
}