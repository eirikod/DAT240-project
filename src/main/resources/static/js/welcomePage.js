//import { SocketConnector } from 'socket-connector';

const client = new SocketConnector();

client.onConnect = () => {
    client.send(makeMessage(userId, "JOIN"), `/app/update/${userId}/registerUserUpdates`);
    client.subscribe(`/channel/update/${userId}`, data => {
        if (data.type === "JOIN_PARTY") {
            let role = data.content.role.toLowerCase();
            if (role === "proposer") {
                role = "proposerImageSelection";
            }

            let url = "http://localhost:8080/" + role +
                "?username=" + username + "&partyId=" + data.content.partyId;
            if (role === "guesser") {
                url += "&selectedlabel=" + data.content.selectedlabel;
            }

            window.location.replace(url);
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
    $("#search")[0].style = "visibility: hidden";
    $("#searching-text")[0].style = "visibility:; text-align: center;";
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