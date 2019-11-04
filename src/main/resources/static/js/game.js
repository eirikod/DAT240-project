const MSG_TYPES = new Enum(
    "SEND_GUESS",
    "REQUEST_SEGMENT",
    "SEND_SEGMENT",
    "QUIT"
);

const PLAYER_STATES = new Enum(
    "PLAYING",
    "WAITING",
    "FINISHED",
    "DECONNECTION"
);

const PLAYER_ROLES = new Enum(
    "PROPOSER",
    "GUESSER",
);

//Start dato, tid etc.  --> key: startDate
// save time when timer was started and measure difference

var startDate = sessionStorage.getItem('startDate');

if (startDate) {
    startDate = new Date(startDate);
} else {
    startDate = new Date();
    sessionStorage.setItem('startDate', startDate);  //key-value
}

let time = 0;
let stopTimer = false;