// This is a simple real-time chatting program to test the web sockets

const client = new SocketConnector();

const messages = document.getElementById('messages');

const input = document.getElementById("chat-input");

let username = "user_" + Math.random().toFixed(3) * 1000;

input.addEventListener("keydown", event => {
   if (event.keyCode === 13) {
      const chatMessage = {
         sender: username,
         content: input.value,
         type: 'CHAT'
      };
      client.send(chatMessage, `${topic}/sendMessage`);
      input.value = "";
   }
});

let topic = "";
let currentSubscription;

function enterRoom(newRoomId) {
   topic = `/app/chat/${newRoomId}`;

   if (currentSubscription) {
      currentSubscription.unsubscribe();
   }
   currentSubscription = client.subscribe(`/channel/${newRoomId}`, onMessageReceived);

   client.send({sender: username, type: 'JOIN'}, `${topic}/addUser`);
}

function onMessageReceived(data) {
   const message = document.createElement('span');
   message.innerHTML = data;
   messages.appendChild(message);
   messages.appendChild(document.createElement('br'));
}

function range(length) {
   let arr = [];
   arr.length = length;
   arr.fill(null, 0, length);
   return arr;
}

for (let i in range(3)) {
   const button = document.createElement('button');
   button.innerHTML = "Room" + (Number(i) + 1);
   document.body.appendChild(button);
   button.onclick = () => {
      enterRoom((Number(i) + 1));
   }
}
