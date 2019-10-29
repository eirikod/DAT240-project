// This is a simple real-time chatting program to test the web sockets

const client = new SocketConnector();

const messages = document.getElementById('messages');

const input = document.getElementById("chat-input");

let randomName = "user_" + Math.random().toFixed(3) * 1000;

input.addEventListener("keydown", event => {
   if (event.keyCode === 13) {
      client.send({ message: randomName + ": " + input.value});
      input.value = "";
   }
});

client.addStompListener('/user/broker/chat', data => {
   const message = document.createElement('span');
   message.innerHTML = data;
   messages.appendChild(message);
   messages.appendChild(document.createElement('br'));
});