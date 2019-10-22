package no.uis.backend_pseudo_game;

import no.uis.backend_pseudo_game.dummy.DummyPlayer;
import no.uis.players.Player;

public class Party {
    // TODO: Add controller logic when ready to merge with the front-end

    private DummyPlayer guesser;
    private DummyPlayer proposer;
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

    public void setProposer(DummyPlayer proposer) {
        this.proposer = proposer;
    }

    public void setGuesser(DummyPlayer guesser) {
        this.guesser = guesser;
    }

    public PartyStatus getStatus() {
        return currentStatus;
    }

    public void setStatus(PartyStatus status) {
        currentStatus = status;
    }
}