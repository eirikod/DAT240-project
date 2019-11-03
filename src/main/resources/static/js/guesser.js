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

const nbChance = {
	    1: "You have 3 chances",
	    2: "You have 2 chances",
	    3: "last chance"
	};

var imageId = 0;

var guessChance = 3;

/*
 * funtion trigerred on websocket connexion
 */
client.onConnect = function () {
    route = `/app/party/${partyId}/addUser`;
    client.send({sender: "", type: 'JOIN'}, route);
};

var state = PLAYER_STATES.WAITING;

console.log(state);

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
        content: {
            guess: guess,
            role: PLAYER_ROLES.GUESSER,
            sender: userId
        },
        type: "SEND_GUESS"
    };
    client.send(message, `/app/party/${partyId}/update`);
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
    client.addStompListener(`/channel/update/${user_id}`, update);
    console.log("Subscribe ends--------------------");
}


/**
 *callback
 * @param msg - object or string
 * @author Guillien Grégoire
 */
function update(msg) {
    console.log(msg);
    imageId = msg.content.segment;
    state = msg.content.state;
    score = msg.content.score;
    time = msg.content.time;

    updateState();
    updateFeatures();
    printImageSegment();
}

/**
 *update the state feature
 * @author Guillien Grégoire
 */
function updateState() {

    switch (state) {
        case PLAYER_STATES.PLAYING:
            $("#proposerPopUp").text(msg.msgMyTurn);
            console.log($("#submitGuess"));
            $("#submitGuess")[0].disabled=false;
            $("#submitNewSegment")[0].disabled=false;
            document.getElementById("submitGuess").disabled = false; 
            document.getElementById("submitNewSegment").disabled = false;
            $("#guessChance").text(3);
            guessChance=3;
            break;

        case PLAYER_STATES.WAITING:
            console.log("Not my turn");
            $("#proposerPopUp").text(msg.msgNotMyTurn);
            $("#submitGuess").className="myButton";
            $("#submitGuess")[0].disabled=true;
            $("#submitNewSegment")[0].disabled=true;
            break;

        case PLAYER_STATES.FINISHED:
            console.log("Party finished");
            $("#proposerPopUp").text(msg.msgPartyFinished);
            document.getElementById("submitGuess").disabled = true; 
            document.getElementById("submitNewSegment").disabled = true; 
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
    $("#submitGuess").click(function () {
        submitGuess();
    });
    $("#submitNewSegment").click(function () {
        submitNewSegment();
    });
});

function submitGuess() {
    console.log($("#guess").val());
    var guess = $("#guess").val();
    imageId = guess;
    console.log(guess);
    console.log("submitGuess used");
    const message = {
        content: ({
            guess: guess,
            role: PLAYER_ROLES.GUESSER
        }),
        type: MSG_TYPES.SEND_GUESS
    };
    client.send(message, `/app/party/${partyId}/update`);
    guessChance --;
    console.log(guessChance);
    if(guessChance===0){
    	state = PLAYER_STATES.WAITING;
        updateState();
    }
    $("#guessChance").text(guessChance);
}

function submitNewSegment() {
    console.log(guess);
    console.log("submitNewSegment used");
    const message = {
        content: ({
            requestSegment: true,
            role: PLAYER_ROLES.GUESSER,
        }),
        type: MSG_TYPES.REQUEST_SEGMENT,
        sender: userId
    };
    client.send(message, `/app/party/${partyId}/update`);
    state = PLAYER_STATES.WAITING;
    updateState();
}