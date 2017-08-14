var socket = null;
var switchCheckbox = null;

function onLoad() {
    connectWebSocket();
    switchCheckbox = document.getElementById("switch-checkbox");
    switchCheckbox.onclick = switchClicked
}

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
        socket.send("close");
        console.log("socket closed");
        setTimeout(connectWebSocket, 5000);
    };

    socket.onmessage = function(event) {
        var msg = event.data.toString();
        if (msg === "true")
            onTrueReceived();
        else if (msg === "false")
            onFalseReceived();
    };
}
window.addEventListener("load", onLoad);


function onTrueReceived() {
    console.log("onTrueReceived");
    switchCheckbox.checked = true;
}

function onFalseReceived() {
    console.log("onFalseReceived");
    switchCheckbox.checked = false;
}

function switchClicked() {
    switchCheckbox.checked = !switchCheckbox.checked;
    socket.send(!switchCheckbox.checked);
    return false;
}

// function sendTogglePost() {
//     socket.send("toggle");
//     var request = new XMLHttpRequest();
//     request.onload = function() {
//     };
//     request.open("POST", "http://" + window.location.host + "/toggle", true);
//     request.send();
// }