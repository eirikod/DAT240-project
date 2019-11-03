package no.uis.party;

import no.uis.players.Player;
import no.uis.repositories.ScoreBoardRepository;
import no.uis.websocket.SocketMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessageSendingOperations;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Holds the game state data of the guesser and proposer.
 *
 * @author Alan Rostem
 */
public class Party {
    // TODO: Add controller logic when ready to merge with the front-end
    private String id = "" + Math.abs(new Random().nextLong());
    private Player guesser;
    private Player proposer;
    private PartyStatus currentStatus;

    @Autowired
    private ScoreBoardRepository scoreBoardRepository;

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

    public String getId() {
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

    enum GameMessageType {
        SEND_GUESS(),
        REQUEST_SEGMENT(),
        SEND_SEGMENT(),
        ;
    }

    public void receiveUpdateFromFront(SocketMessage message, SimpMessageSendingOperations smos) {
        System.out.println("FrontMsgUpdate ---------------------------------------------------");
        SocketMessage sockMess = new SocketMessage();

        String state = (Player.PlayerStatus.FINISHED).toString();
        String score = "8";
        String time = "12:23";

        HashMap<String, String> content = new HashMap<>();
        sockMess.setContent(content);

        content.put("state", state);
        content.put("score", score);
        content.put("time", time);

        switch (message.getType()) {
            case "SEND_GUESS":
                content.put("guess", (String) message.contentToMap().get("guess"));
                // content.put("state", getProposer().getPlayerStatus().toString());
                getProposer().sendData(sockMess, smos);
                break;
            case "REQUEST_SEGMENT":
                content.put("requestSegment", "true");
                getProposer().sendData(sockMess, smos);
                // content.put("state", getProposer().getPlayerStatus().toString());
                break;
            case "SEND_SEGMENT":
                content.put("segment", (String) ((Map) message.contentToMap()).get("segment"));
                // content.put("state", getGuesser().getPlayerStatus().toString());
                getGuesser().sendData(sockMess, smos);
                break;
            default:
                System.out.println("Invalid message type received on party: " + getId());
        }
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
     * Sets a proposer to the party.
     *
     * @author Alan Rostem
     */
    public Player getProposer() {
        return this.proposer;
    }

    /**
     * Sets a guesser to the party.
     *
     * @author Alan Rostem
     */
    public Player getGuesser() {
        return (this.guesser);
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
