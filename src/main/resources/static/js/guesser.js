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
    msgMyTurn: "choose new segment",
    msgNotMyTurn: "Wait for segment",
    msgPartyFinished: "YOU WIN"
};

/*
 * Enum statement
 */
const enumState = {
    myTurn: "PLAYING",
    waiting: "WAITING",
    finished: "FINISHED"
};

var imageId = 5;

/*
 * funtion trigerred on websocket connexion
 */
client.onConnect = function () {
    route = `/app/party/${partyId}/addUser`;
    client.send({sender: "", type: 'JOIN'}, route);
};

var state = enumState.myTurn;

var score = "0";

var time = "00:00";

updateState();

/**
 *Send the image's id to the back-end using the web-socket channel
 * @param id - image's id
 * @author Grégoire Guillien
 */
function sendGuess(guess) {
    console.log(guess);
    console.log("sendImageId used");
    const message = {
        content: guess
    };
//	client.send(message, `app/party/queueUp`);
    client.send(message, `/app/party/${partyId}/sendGuess`);
}

/**
 *Subscribe to the websocket
 * @param userId - Id of the user
 * @param partyId - Id of the party
 * @author Guillien Grégoire
 */
function subscribe(user_id, party_id) {
    console.log("Subscribe begins--------------------");
    userId = user_id;
    partyId = party_id;
    console.log(user_id);
    client.addStompListener(`/channel/update/${userId}`, update);
    console.log("Subscribe ends--------------------");
}


/**
 *callback
 * @param msg - object or string
 * @author Guillien Grégoire
 */
function update(msg) {
    console.log("update appele");
    console.log(msg);
    imgSegmnent = msg.content.segment;
    state = msg.content.state;
    score = msg.content.score;
    time = msg.content.time;
    console.log(state);
    updateState();
    updateFeatures();
    printImageSegment();
    //TODO
}

/**
 *update the state feature
 * @author Guillien Grégoire
 */
function updateState() {

    switch (state) {
        case enumState.myTurn:
            console.log("my turn");
            $("#proposerPopUp").text(msg.msgMyTurn);
            break;

        case enumState.waiting:
            console.log("Not my turn");
            $("#proposerPopUp").text(msg.msgNotMyTurn);
            break;

        case enumState.finished:
            console.log("Party finished");
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

function printImageSegment() {
    document.getElementById(imageId).style.visibility = "visible";
}

/**
 *Manage the 'click' events
 * @param String - trigger event
 * @param target - html object
 * @author Grégoire Guillien
 */
$(function () {
    $("form").on('submit', function (e) {
        console.log(e);
        e.preventDefault();
    });
    $("#submitGuess").click(function () {
        submitGuess();
    });
    $("#submitNewSegment").click(function () {
        submitNewSegment();
    });
});

function submitGuess() {
    console.log($("#guess"));
    var guess = $("#guess").val();
    imageId = guess;
    console.log(guess);
    console.log("submitGuess used");
    const message = {
        content: guess
    };
    client.send(message, `/app/party/${partyId}/sendGuess`);
}

function submitNewSegment() {
    console.log(guess);
    console.log("submitNewSegment used");
    const message = {
        content: "newSegment"
    };
    client.send(message, `/app/party/${partyId}/sendGuess`);
}