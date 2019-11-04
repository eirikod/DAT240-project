/**
 * proposer.js
 * file managing the event on the proposer.html
 *
 */

const client = new SocketConnector();//Web socket client

/*
 * Player game state labels for display.
 */
const msg = {
    msgMyTurn: "It's your turn",
    msgNotMyTurn: "wait for your turn",
    msgPartyFinished: "congrats! You win!",
    msgPartyDeco: "Deconnection! You can come back to the home page"
};

/*
 * Enum for the player game state
 */
const enumState = {
    myTurn: "PLAYING",
    waiting: "WAITING",
    finished: "FINISHED",
    deconected: "DECONNECTION"
};

client.onConnect = function () {
    console.log("----------------------------------------------")
    route = `/app/party/${partyId}/respToGuesser`;
    console.log(route);
    client.send({sender: `${userId}`, content: `${label}`, type: 'JOIN'}, route);
    console.log("pass")
};

/**
 * User ID given by the back
 * @type {string}
 */
var userId = "";

/**
 * Party ID given by the back
 * @type {string}
 */
var partyId = "";

/**
 * Control variable for sending messages when it is the proposers turn
 * @type {boolean}
 */
var myTurn = true;

/**
 * Player game state
 * @type {string}
 */
var state = enumState.myTurn;

/**
 * Score display variable
 * @type {string}
 */
var score = "0";

updateState();//initialize the page state

/**
 * Send the image's id to the back-end using the web-socket channel
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
    client.send(message, `/app/party/${partyId}/update`);
}

/**
 * Subscribe to the websocket
 * @param user_id - Id of the user
 * @param party_id - Id of the party
 * @author Guillien Grégoire
 */
function subscribe(user_id, party_id) {
    userId = user_id;
    partyId = party_id;
    client.addStompListener(`/channel/update/${userId}`, update);
    console.log("Subscribe to the websocket--------------------");
}


/**
 * Callback method for the /channel/update/{userId} destination subscription
 * @param msg - object or string
 * @author Guillien Grégoire
 */
function update(msg) {
    console.log("receive a message via the websocket");
    state = msg.content.state;
    score = msg.content.score;
    time = Number(msg.content.time);
    if (msg.content.segments != null) {
        console.log("update segments");
        segments = msg.content.segments;
        updateSegments();
    }
    updateState();
    updateFeatures();
}

/**
 * Update the images segments picked if the page is reloaded
 * @author Guillien Grégoire
 */
function updateSegments(){
	segments.forEach(function(element){
		console.log(element);
		console.log(document.getElementById(element));
		document.getElementById(element).className="beta-mask";
		var lstBtn=document.getElementsByClassName("myButton");
		for (let item of lstBtn) {
		    console.log(item.id);
		    if(item.value ===element){
		    	console.log(item);
		    	item.className="";
		    }
		}
	});
}

/**
 * Update the page based on the state received from the back.
 * @author Guillien Grégoire
 */
function updateState() {

    switch (state) {
        case enumState.myTurn:
            stopTimer = true;
            $("#proposerPopUp").text(msg.msgMyTurn);
            break;

        case enumState.waiting:
            stopTimer = false;
            $("#proposerPopUp").text(msg.msgNotMyTurn);
            break;

        case enumState.finished:
            stopTimer = true;
            $("#proposerPopUp").text(msg.msgPartyFinished);
            client.disconnect();
            break;
            
        case enumState.deconected:
        	state=enumState.finished;
        	console.log("deconetcion!")
            stopTimer = true;
            $("#proposerPopUp").text(msg.msgPartyDeco);
            client.disconnect();
            break;

        default:
            break

    }
}

/**
 * Update score and timer on the page
 * @author Grégoire Guillien
 */
function updateFeatures() {
    $("#time").text(time);
    $("#score").text(score);

}

/**
 * Functions managing events on id html object
 * @author Grégoire Guillien
 */
$(function () {
    $("#disconnect").click(function () {
        disconnect();
    });
    $("#endPlayRound").click(function () {
        sendNotif();
    });
    $("#home").click(function () {
        home();
    });
    $("#disconnect").click(function () {
    	disconnect();
    });
});

/**
 * Manage the 'mouseover' events
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
 * Manage the 'mouseout' events
 * @param String - trigger event
 * @param target - html object
 * @author Grégoire Guillien
 */
document.addEventListener("mouseout", ({target}) => {
    if (target.className === "myButton") {
        var element = document.getElementById(target.value).parentElement;
        element.className = "alpha-mask";
    }
});

/**
 * Manage the 'click' events
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
            time = 0;
            updateState();
        } else if (state === enumState.finished) {
            var element = document.getElementById(target.value).parentElement;
            client.disconnect();
            target.disabled = true;
        } else {
            state = enumState.waiting;
            updateState();
        }
    }
});

/**
 * Quit and go back to the welcome page
 * @author Grégoire Guillien
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