/**
 * Message type enum config
 * @type {Enum}
 */
const MSG_TYPES = new Enum(
    "SEND_GUESS",
    "REQUEST_SEGMENT",
    "SEND_SEGMENT",
);

/**
 * Player game state enum config
 * @type {Enum}
 */
const PLAYER_STATES = new Enum(
    "PLAYING",
    "WAITING",
    "FINISHED",
    "DECONNECTION"
);

/**
 * Player role enum config
 * @type {Enum}
 */
const PLAYER_ROLES = new Enum(
    "PROPOSER",
    "GUESSER",
);

/**
 * Time variable that gets incremented on the front and updated every time we receive messages from the back
 * @type {number}
 */
let time = 0;

/**
 * Control variable to stop the front end timer when the guesser is not playing
 * @type {boolean}
 */
let stopTimer = false;