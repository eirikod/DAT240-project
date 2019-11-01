//import { SocketConnector } from 'socket-connector';

const client = new SocketConnector();

client.addStompListener(`channel/update/${id}/`, data => {

});

function makeMessage(content) {
    return {
        content: content,
        sender: username,
        type: "MSG"
    };
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
	client.send(makeMessage($("#dropOperator").val()), "/app/party/queueUp");
}

/*function enterRoom(newRoomId) {
    topic = `/app/chat/${newRoomId}`;

    if (currentSubscription) {
        currentSubscription.unsubscribe();
    }
    currentSubscription = client.subscribe(`/channel/${newRoomId}`, onMessageReceived);

    client.send({sender: username, type: 'JOIN'}, `${topic}/addUser`);
}


 */

function researchParty(){
	console.log("tamere");
	$( "#loader" )[0].style="visibility:;";
	console.log($( "#loader" ));
	//console.log("document.getElementById('demo').innerHTML = Date()");
}

let searching = false;

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
    $( "#search" ).click(function() {
        if (searching) {
            sendPartyParameters();
            researchParty();
            searching = true;
        }
    });
});