/**
 * proposer.js
 * class js managing the event on the proposer.html
 * 
 */

const client = new SocketConnector();

/*
 * party state label
 */
const msg= {
		msgMyTurn: "It's your turn",
		msgNotMyTurn: "wait your turn",
		msgPartyFinished: "you loose"
};
	
/*
 * Enum statement
 */
const enumState= {
		myTurn: "PLAYING",
		waiting: "WAITING",
		finished: "FINISHED"
};

/*
 * funtion trigerred on websocket connexion
 */
client.onConnect = function(){
	route = `app/party/${partyId}/addUser`;
	client.send({sender: "", type: 'JOIN'}, route);
}

var userId = "";

var partyId = "";

var myTurn = true;

var state = enumState.myTurn;

updateState();

/**
*Send the image's id to the back-end using the web-socket channel
* @param id - image's id
* @author Grégoire Guillien
*/
function sendImageId(id){
	console.log(id);
	console.log("sendImageId used");
	const message = {
		content:id
	};
//	client.send(message, `app/party/queueUp`);
	client.send(message, `/app/party/${partyId}/sendImageId`);
}

/**
*Subscribe to the websocket
* @param userId - Id of the user
* @param partyId - Id of the party
* @author Guillien Grégoire
*/
function subscribe(user_id, party_id){
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
function update(msg){
	console.log("update appele");
	console.log(msg);
	state = msg;
	updateState();
	//TODO
}

/**
*update the state features
* @author Guillien Grégoire
*/
function updateState(){
	
	switch(state){
		case enumState.myTurn:
			console.log("my turn");
			$( "#proposerPopUp" ).text(msg.msgMyTurn);
			break;

		case enumState.waiting:
			console.log("Not my turn");
			$( "#proposerPopUp" ).text(msg.msgNotMyTurn);
			break;
	
		case enumState.finished:
			console.log("Party finished");
			$( "#proposerPopUp" ).text(msg.msgPartyFinished);
			break;
			
		default:
			break
		
	}
}

/**
*Fonctions managing events on id html object
* @author Grégoire Guillien
*/
$(function () {
    $("form").on('submit', function (e) {
    	console.log(e);
   //     e.preventDefault();
    });
    $( "#disconnect" ).click(function() { disconnect(); });
    $( "#endPlayRound" ).click(function() { sendNotif(); });
});

/**
*Manage the 'mouseover' events
* @param String - trigger event
* @param target - html object
* @author Grégoire Guillien
*/
document.addEventListener("mouseover", ({ target }) => {
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
document.addEventListener("mouseout", ({ target }) => {
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
document.addEventListener("click", ({ target }) => {
	if (target.className === "myButton") {
		console.log("tour de jeu", myTurn);
		if(state === enumState.myTurn){
			console.log(target);
			var element = document.getElementById(target.value).parentElement;
			sendImageId(target.value);
			target.disabled=true;
			target.className="";
			myTurn = false;
			state = enumState.waiting;
			updateState();
		}
		else{
			state = enumState.waiting;
			updateState();
		}
	}
})