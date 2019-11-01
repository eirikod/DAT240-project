/**
 * proposer.js
 * class js managing the event on the proposer.html
 * 
 */

const client = new SocketConnector();

var userId = "";

var partyId = "";

/**
*Send the image's id to the back-end using the web-socket channel
* @param id - image's id
* @author Grégoire Guillien
*/
function sendImageId(id){
	console.log(id);
	console.log("sendImageId used");
	client.send("/app/welcomePage", {}, JSON.stringify({'selectedPlayModelabel': $("#dropOperator").val(), 'selectedPlayerModelabel': $("#dropOperator2").val()}));
	const message = {
		content: JSON.stringify({'idImage': id})
	};
	client.send(chatMessage, `${topic}/sendImageId`);
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
	client.addStompListener(`/channel/update/${userId}`, update);
	console.log("Subscribe ends--------------------");
}


function update(){
	console.log("yop")
	//TODO
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
	console.log(target)
	if (target.className === "myButton") {
		console.log(target);
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
		console.log("button clicked");
		console.log(target);
		var element = document.getElementById(target.value).parentElement;
		target.disabled=true;
		target.className="";
	}
})
