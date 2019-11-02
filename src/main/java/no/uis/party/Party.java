package no.uis.party;

import no.uis.players.Player;
import org.springframework.messaging.simp.SimpMessageSendingOperations;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Random;

/**
 * Holds the game state data of the guesser and proposer.
 *
 * @author Alan Rostem
 */
public class Party {
    // TODO: Add controller logic when ready to merge with the front-end
    private Long id = Math.abs(new Random().nextLong());
    private Player guesser;
    private Player proposer;
    private PartyStatus currentStatus;

    /**
     * Sequential update method
     */
    public void update(SimpMessageSendingOperations messagingTemplate) {
        if (guesser != null) {
            guesser.update(messagingTemplate);
        }
        if (proposer != null) {
            proposer.update(messagingTemplate);
        }
    }

    public Long getId() {
        return id;
    }

    /**
     * Party status enumerate class
     *
     * @author Alan Rostem
     */
    public enum PartyStatus {
        WAITING_FOR_PLAYERS,
        READY_TO_PLAY,
        PLAYING_GAME,
        FINISHED_GAME,
    }

    /**
     * Initializes a party with status WAITING_FOR_PLAYERS.
     *
     * @author Alan Rostem
     */
    public Party() {
        this.currentStatus = PartyStatus.WAITING_FOR_PLAYERS;
    }

    /**
     * Method called when the party state has been changed to READY_TO_PLAY.
     *
     * @author Alan Rostem
     */
    public void onReady() {

    }

    /**
     * Method called when the party state has been changed to FINISHED_GAME.
     *
     * @author Alan Rostem
     */
    public void onFinished() {

    }

    /**
     * Sets a proposer to the party.
     *
     * @param proposer Player
     * @author Alan Rostem
     */
    public void setProposer(Player proposer) {
        this.proposer = proposer;
    }

    /**
     * Sets a guesser to the party.
     *
     * @param guesser Player
     * @author Alan Rostem
     */
    public void setGuesser(Player guesser) {
        this.guesser = guesser;
    }

    /**
     * Retrieve the current state of the party.
     *
     * @return Party.PartyStatus
     * @author Alan Rostem
     */
    public PartyStatus getStatus() {
        return currentStatus;
    }

    /**
     * Set the current state of the party. Respective event methods are called upon setting.
     *
     * @param status Party.PartyStatus
     * @author Alan Rostem
     */
    public void setStatus(PartyStatus status) {
        currentStatus = status;
        switch (status) {
            case READY_TO_PLAY:
                onReady();
                break;
            case FINISHED_GAME:
                onFinished();
                break;
        }
    }
}
