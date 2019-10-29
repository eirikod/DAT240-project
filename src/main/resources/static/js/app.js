const client = new SocketConnector();
client.addStompListener('/welcome/client', data => {
   console.log("This is being logged from the new stomp listener! Data:", data);
});