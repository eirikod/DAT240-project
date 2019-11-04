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
    msgMyTurn: "Make a guess!",
    msgNotMyTurn: "Wait for a new segment...",
    msgPartyFinished: "congrats! You win!",
    msgPartyLoose: "you lost, try again!",
    msgPartyDeco: "Deconnection! You can come back to the home page"
};

const nbChance = {
    0: "",
    1: "Wrong again! Last chance!",
    2: "Wrong! 2 chances remaining",
    3: "You have 3 chances"
};

var imageId = 0;

var guessChance = 0;

/*
 * funtion used on websocket connexion
 * @author Grégoire Guillien
 */
client.onConnect = function () {
    route = `/app/party/${partyId}/addUser`;
    client.send({sender: "", type: 'JOIN'}, route);
};

var state = PLAYER_STATES.WAITING;

var score = "0";

updateState();

/**
 *Send the image's id to the back-end using the web-socket channel
 * @param id - image's id
 * @author Grégoire Guillien
 */
function sendGuess(guess) {
    console.log("send the guess via websocket----------");
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
    userId = user_id;
    partyId = party_id;
    console.log(user_id);
    client.addStompListener(`/channel/update/${user_id}`, update);
    console.log("Subscribed ro the web-socket--------------------");
}


/**
 *callback
 * @param msg - object or string
 * @author Guillien Grégoire
 */
function update(msg) {
    console.log("Received a msg via web-socket---------------");
    if (msg.content.segment != null){
    	imageId = msg.content.segment;    	
    }
    state = msg.content.state;
    score = msg.content.score;
    time = Number(msg.content.time);
    if(msg.content.segments !=null){
    	console.log("update segments");
    	segments = msg.content.segments;
    	updateSegments();
    }
    updateState();
    updateFeatures();
}

/**
 *update the image segments printed
 * @author Guillien Grégoire
 */
function updateSegments(){
	segments.forEach(function(element){
		console.log(element);
		document.getElementById(element).style.visibility = "visible";		
	});
}

/**
 *update the state feature
 * @author Guillien Grégoire
 */
function updateState() {

    switch (state) {
        case PLAYER_STATES.PLAYING:
            stopTimer = false;
            $("#proposerPopUp").text(msg.msgMyTurn);
            $("#submitGuess")[0].disabled = false;
            $("#submitNewSegment")[0].disabled = false;
            document.getElementById("submitGuess").disabled = false;
            document.getElementById("submitNewSegment").disabled = false;
            guessChance = 3;
            $("#guessRemaning")[0].innerText = nbChance[guessChance];
            printImageSegment();
            break;

        case PLAYER_STATES.WAITING:
            stopTimer = true;
            $("#proposerPopUp").text(msg.msgNotMyTurn);
            $("#submitGuess").className = "myButton";
            $("#submitGuess")[0].disabled = true;
            $("#submitNewSegment")[0].disabled = true;
            $("#guessRemaning")[0].innerText = nbChance[guessChance];
            break;

        case PLAYER_STATES.FINISHED:
            stopTimer = true;
            $("#proposerPopUp").text(msg.msgPartyFinished);
            document.getElementById("submitGuess").disabled = true;
            document.getElementById("submitNewSegment").disabled = true;
            client.disconnect();
            $("#score").text(score);
            $("#guessRemaning")[0].innerText=nbChance[0];
            break;

        case "DECONNECTION":
        	console.log("disconected");
            stopTimer = true;
            $("#proposerPopUp").text(msg.msgPartyDeco);
            document.getElementById("submitGuess").disabled = true;
            document.getElementById("submitNewSegment").disabled = true;
            client.disconnect();
            $("#score").text(score);
            $("#guessRemaning")[0].innerText=nbChance[0];
            break;
        default:
            break

    }
}

/**
 *update the optionnal features
 * @author Guillien Grégoire
 */
function updateFeatures() {
    $("#time").text(time);
    $("#score").text(score);

}

/**
 *print an image segment
 * @author Guillien Grégoire
 */
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
    $("#home").click(function () {
        home();
    });
    $("#disconnect").click(function () {
    	disconnect();
    });
});

/**
 *send the guess to the back and update the view
 * @author Guillien Grégoire
 */
function submitGuess() {
    var guess = $("#guess").val();
    imageId = guess;
    const message = {
        content: ({
            guess: guess,
            role: PLAYER_ROLES.GUESSER
        }),
        type: MSG_TYPES.SEND_GUESS
    };
    client.send(message, `/app/party/${partyId}/update`);
    guessChance--;
    console.log("send a msg via web-socket---------");
    $("#guessRemaning")[0].innerText = nbChance[guessChance];
    if (guessChance === 0) {
        state = PLAYER_STATES.WAITING;
        updateState();
    }
}

/**
 *ask for an other segment to the back and update the view
 * @author Guillien Grégoire
 */
function submitNewSegment() {
    console.log("send a msg via web-socket---------");
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
    guessChance = 0;
    updateState();
}

/**
 *send a surrend message to the back and return to the home page.
 * @author Guillien Grégoire
 */
function home(){
	const message = {
	        content: {
	        	partyId: partyId,
	            role: PLAYER_ROLES.GUESSER,
	            sender: userId
	        },
	        type: "QUIT"
	    };
	client.send(message, `/app/party/${partyId}/update`);
	let url = "http://localhost:8080/welcomePage" + "?username=" + username + "&id=" + userId;
	window.location.replace(url);
}

/**
 *send a surrend message to the back and return to the login page.
 * @author Guillien Grégoire
 */
function disconnect(){
	const message = {
	        content: {
	        	partyId: partyId,
	            role: PLAYER_ROLES.GUESSER,
	            sender: userId
	        },
	        type: "QUIT"
	    };
	client.send(message, `/app/party/${partyId}/update`);
	let url = "http://localhost:8080/";
	window.location.replace(url);
}