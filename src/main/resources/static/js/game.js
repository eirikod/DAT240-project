const MSG_TYPES = new Enum(
    "SEND_GUESS",
    "REQUEST_SEGMENT",
    "SEND_SEGMENT",
);

const PLAYER_STATES = new Enum(
    "PLAYING",
    "WAITING",
    "FINISHED"
);

const PLAYER_ROLES = new Enum(
    "PROPOSER",
    "GUESSER",
);