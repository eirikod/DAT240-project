/**
 * proposer.js
 * file managing the event on the proposer.html
 *
 */

const client = new SocketConnector();

/*
 * party state label
 */
const msg = {
    msgMyTurn: "It's your turn",
    msgNotMyTurn: "wait for your turn",
    msgPartyFinished: "congrats! You win!"
};

/*
 * Enum statement
 */
const enumState = {
    myTurn: "PLAYING",
    waiting: "WAITING",
    finished: "FINISHED"
};

/*
 * funtion trigerred on websocket connexion
 */
client.onConnect = function () {
    console.log("----------------------------------------------")
    route = `/app/party/${partyId}/respToGuesser`;
    console.log(route);
    client.send({sender: "", content: `${label}`, type: 'JOIN'}, route);
};

var userId = "";

var partyId = "";

var myTurn = true;

var state = enumState.myTurn;

var score = "0";

updateState();

/**
 *Send the image's id to the back-end using the web-socket channel
 * @param id - image's id
 * @author Grégoire Guillien
 */
function sendImageId(id) {
    console.log("sendImageId used");
    const message = {
        content: {
            segment: id,
            role: PLAYER_ROLES.PROPOSER
        },
        type: "SEND_SEGMENT",
        sender: userId
    };
//	client.send(message, `app/party/queueUp`);
    client.send(message, `/app/party/${partyId}/update`);
}

/**
 *Subscribe to the websocket
 * @param userId - Id of the user
 * @param partyId - Id of the party
 * @author Guillien Grégoire
 */
function subscribe(user_id, party_id) {
    userId = user_id;
    partyId = party_id;
    client.addStompListener(`/channel/update/${userId}`, update);
    console.log("Subscribe to the websocket--------------------");
}


/**
 *callback
 * @param msg - object or string
 * @author Guillien Grégoire
 */
function update(msg) {
    console.log("receive a message via the websocket");
    state = msg.content.state;
    score = msg.content.score;
    time = Number(msg.content.time);
    updateState();
    updateFeatures();
}

/**
 *update the state feature
 * @author Guillien Grégoire
 */
function updateState() {

    switch (state) {
        case enumState.myTurn:
            $("#proposerPopUp").text(msg.msgMyTurn);
            break;

        case enumState.waiting:
            $("#proposerPopUp").text(msg.msgNotMyTurn);
            break;

        case enumState.finished:
            $("#proposerPopUp").text(msg.msgPartyFinished);
            break;

        default:
            break

    }
}

function updateFeatures() {
    $("#time").text(time);
    $("#score").text(score);

}

/**
 *Fonctions managing events on id html object
 * @author Grégoire Guillien
 */
$(function () {
    $("#disconnect").click(function () {
        disconnect();
    });
    $("#endPlayRound").click(function () {
        sendNotif();
    });
});

/**
 *Manage the 'mouseover' events
 * @param String - trigger event
 * @param target - html object
 * @author Grégoire Guillien
 */
document.addEventListener("mouseover", ({target}) => {
    if (target.className === "myButton") {
        var element = document.getElementById(target.value).parentElement;
        element.className = "beta-mask";
    }
})

/**
 *Manage the 'mouseout' events
 * @param String - trigger event
 * @param target - html object
 * @author Grégoire Guillien
 */
document.addEventListener("mouseout", ({target}) => {
    if (target.className === "myButton") {
        var element = document.getElementById(target.value).parentElement
        element.className = "alpha-mask"
    }
})

/**
 *Manage the 'click' events
 * @param String - trigger event
 * @param target - html object
 * @author Grégoire Guillien
 */
document.addEventListener("click", ({target}) => {
    if (target.className === "myButton") {
        if (state === enumState.myTurn) {
            var element = document.getElementById(target.value).parentElement;
            sendImageId(target.value);
            target.disabled = true;
            target.className = "";
            myTurn = false;
            state = enumState.waiting;
            updateState();
        } else if (state === enumState.finished) {
            var element = document.getElementById(target.value).parentElement;
            sendImageId(target.value);
            target.disabled = true;
        } else {
            state = enumState.waiting;
            updateState();
        }
    }
})
