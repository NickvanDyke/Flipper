var socket = null;
var switchCheckbox = null;
var trueText = null;
var falseText = null;

function onLoad() {
    connectWebSocket();
    switchCheckbox = document.getElementById("switch-checkbox");
    trueText = document.getElementById("true-text");
    falseText = document.getElementById("false-text");
    switchCheckbox.onclick = switchClicked;
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
        console.log("socket closed");
        setTimeout(connectWebSocket, 5000);
    };

    socket.onmessage = function(event) {
        var json = JSON.parse(event.data.toString());
        switchCheckbox.checked = json.state;
        trueText.innerHTML = millisecondsToTimeString(json.trueMs);
        falseText.innerHTML = millisecondsToTimeString(json.falseMs);
    };
}
window.addEventListener("load", onLoad);

function switchClicked() {
    switchCheckbox.checked = !switchCheckbox.checked;
    socket.send(!switchCheckbox.checked);
    return false;
}

function millisecondsToTimeString(ms) {
    var days = Math.floor(ms / 86400000);
    ms -= days * 86400000;
    var hours = Math.floor(ms / 3600000);
    ms -= hours * 3600000;
    var minutes = Math.floor(ms / 60000);
    ms -= minutes * 60000;
    var seconds = Math.floor(ms / 1000);
    ms -= seconds * 1000;
    return days + "d " + hours + "h " + minutes + "m " + seconds + "s "
}