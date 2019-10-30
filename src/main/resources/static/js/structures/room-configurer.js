let topic = "";
let subscription = "";

function enterRoom(client, username, destination, roomID, callback) {
    topic = `/app/${destination}/${roomID}`;
    subscription = `/channel/${destination}/${roomID}`;
    client.subscribe(subscription, callback);
    client.send({sender: username, type: 'JOIN'}, `${topic}/addUser`);
}

function createSenderData(username, input) {
    return {
        sender: username,
        content: input,
        type: 'MSG'
    };
}

function sendToRoom(client, username, input) {
    const chatMessage = {
        sender: username,
        content: input,
        type: 'MSG'
    };
    client.send(chatMessage, `${topic}/sendMessage`);
}