package no.uis.backend_pseudo_game;

import no.uis.players.Player;

public class Party {
    // TODO: Add controller logic when ready to merge with the front-end

    private Player guesser;
    private Player proposer;
    private PartyStatus currentStatus;

    public enum PartyStatus {
        WAITING_FOR_PLAYERS,
        READY_TO_PLAY,
        PLAYING_GAME,
        FINISHED_GAME,
    }

    public Party() {
        this.currentStatus = PartyStatus.WAITING_FOR_PLAYERS;
    }

    public void setProposer(Player proposer) {
        this.proposer = proposer;
    }

    public void setGuesser(Player guesser) {
        this.guesser = guesser;
    }

    public PartyStatus getStatus() {
        return currentStatus;
    }
}

