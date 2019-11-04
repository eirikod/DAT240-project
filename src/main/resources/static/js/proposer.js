/**
 * proposer.js
 * file managing the event on the proposer.html
 *
 */

const client = new SocketConnector();//Web socket client

/*
 * party state label
 */
const msg = {
    msgMyTurn: "It's your turn",
    msgNotMyTurn: "wait for your turn",
    msgPartyFinished: "congrats! You win!",
    msgPartyDeco: "Deconnection! You can come back to the home page"
};

/*
 * Enum statement
 */
const enumState = {
    myTurn: "PLAYING",
    waiting: "WAITING",
    finished: "FINISHED",
    deconected: "DECONNECTION"
};

/*
 * funtion used on websocket connexion
 * @author Gregoire
 */
client.onConnect = function () {
    console.log("----------------------------------------------")
    route = `/app/party/${partyId}/respToGuesser`;
    console.log(route);
    client.send({sender: `${userId}`, content: `${label}`, type: 'JOIN'}, route);
    console.log("pass")
};

var userId = "";

var partyId = "";

var myTurn = true;

var state = enumState.myTurn;

var score = "0";

updateState();//initialize the page state

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
 *callback called by the back-end
 * @param msg - object or string
 * @author Guillien Grégoire
 */
function update(msg) {
    console.log("receive a message via the websocket");
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
 *update the images segments picked if the page is reloaded
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
 *update the state feature
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
 *update optionnal features
 * @author Guillien Grégoire
 */
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
    $("#home").click(function () {
        home();
    });
    $("#disconnect").click(function () {
    	disconnect();
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
        element.className = "alpha-mask";
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
})

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