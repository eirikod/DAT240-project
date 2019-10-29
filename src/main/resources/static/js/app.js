const client = new SocketConnector();
client.addStompListener('/topic/greetings', data => {
   console.log("This is being logged from the new stomp listener! Data:", data);
});