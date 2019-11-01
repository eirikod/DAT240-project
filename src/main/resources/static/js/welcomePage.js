//import { SocketConnector } from 'socket-connector';

const client = new SocketConnector();

let username = "user_" + Math.random().toFixed(3) * 1000;
/**
*
* @param connected {boolean} - Websocket state
* @author Grégoire Guillien
*/
function setConnected(connected) {
	console.log("setConnected")
    $("#connect").prop("disabled", connected);
    $("#disconnect").prop("disabled", !connected);
}

/**
*
* @param void
* @author Grégoire Guillien
*/
function connect() {
	currentSubscription = client.subscribe(`/channel/state`, onStateReceive);
//	console.log("connect")
//    var socket = new SockJS('/ws');
//    stompClient = Stomp.over(socket);
//    stompClient.connect({}, function (frame) {
//        setConnected(true);
//        console.log('Connected: ' + frame);
//        stompClient.subscribe('/game/lunch', function (lunch) {
//            console.log("----------------------------------J'ai recu le call");
//            console.log(lunch);
//            console.log(lunch.toString());
//            var link = lunch.body;
//            console.log(link);
//            $("#navigateur")[0].action = link;
//        });
//        stompClient.subscribe('/game/state', function (lunch) {
//            console.log("----------------------------------J'ai recu le call");
//            console.log(lunch);
//            console.log(lunch.toString());
//            var link = lunch.body;
//            console.log(link);
//            $("#navigateur")[0].action = link;
//        });
//    });
}

function onStateReceive(state){
	console.log(state);
}


function disconnect() {
    if (stompClient !== null) {
        stompClient.disconnect();
    }
    setConnected(false);
    console.log("Disconnected");
}


/**
*
* @param void
* @author Grégoire Guillien
*/
function sendPartyParameters(){
	console.log("sendPartyParameters used");
	stompClient.send("/app/welcomePage", {}, JSON.stringify({'selectedPlayModelabel': $("#dropOperator").val(), 'selectedPlayerModelabel': $("#dropOperator2").val()}));
}

/**
*login method
* @param void
* @author Grégoire Guillien
*/
function login(){
	var userName = $("#loginDiv")[0].children[0].value;
	var password = $("#loginDiv")[0].children[1].value;
	console.log(userName);
	console.log(password);
	const chatMessage = {
            sender: username,
            content: JSON.stringify({'User name': userName, 'selectedPlayerModelabel': password}),
            type: 'CHAT'
        };
	client.send(chatMessage, `/app/welcomePage`);
//	client.send("/app/welcomePage", {}, JSON.stringify({'User name': userName, 'selectedPlayerModelabel': password}));
}

function researchParty(){
	console.log("tamere");
	$( "#loader" )[0].style="visibility:;";
	console.log($( "#loader" ));
	//console.log("document.getElementById('demo').innerHTML = Date()");
}

/**
*Event management
* @param void
* @author Grégoire Guillien
*/
$(function () {
    $("form").on('submit', function (e) {
        e.preventDefault();
    });
    $( "#connect" ).click(function() { connect(); });
    $( "#search" ).click(function() { sendPartyParameters(); });
    $( "#login" ).click(function() { login(); });
    $( "#researchParty" ).click(function() { researchParty(); });
});