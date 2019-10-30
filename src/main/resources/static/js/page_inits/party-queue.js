const title = document.getElementById('title');
const username = "user_" + Math.random().toFixed(3) * 1000;
title.innerHTML += username;
const client = new SocketConnector();

let roomID = null;
client.addStompListener('/user/channel/queueUp', data => {
    roomID = Number(data.content);
});

client.addStompListener(`/channel/test/${username}`, data => {
    console.log("SPECIFIC TO USER ", username + ":", data)
});

let selected = false;
const proposer = document.getElementById('proposer');
proposer.onclick = () => {
    if (!selected) {
        client.send(createSenderData(username, "proposer"), '/app/party/queueUp');
        selected = true;
    }
};

const guesser = document.getElementById('guesser');
guesser.onclick = () => {
    if (!selected) {
        client.send(createSenderData(username, "guesser"), '/app/party/queueUp');
        selected = true;
    }
};

const msgBox = document.getElementById('msg-box');
