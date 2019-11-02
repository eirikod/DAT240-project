//import { SocketConnector } from 'socket-connector';

const client = new SocketConnector();

client.onConnect = () => {
    client.send(makeMessage(userId, "JOIN"), `/app/update/${userId}/registerUserUpdates`);
    client.subscribe(`/channel/update/${userId}`, data => {
        console.log(data.type);
        console.log(data.content.type);
        console.log(data.content);
        if (data.type === "JOIN_PARTY") {
            //window.location.replace("http://localhost:8080/" + data.content.toLowerCase());
        }
    });
};

function makeMessage(content, type = "MSG") {
    return {
        content: content,
        sender: username,
        type: type
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
function sendPartyParameters() {
    console.log("sendPartyParameters used");
    client.send(makeMessage($("#dropOperator").val()), "/app/party/queueUp");
}

function researchParty() {
    $("#loader")[0].style = "visibility:;";
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
    $("#connect").click(function () {
        connect();
    });
    $("#search").click(function () {
        if (!searching) {
            sendPartyParameters();
            researchParty();
            searching = true;
        }
    });
});