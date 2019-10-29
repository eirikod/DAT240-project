const client = new SocketConnector();
client.addStompListener('/broker/chat', data => {
   console.log("This is being logged from the new stomp listener! Data:", data);
});