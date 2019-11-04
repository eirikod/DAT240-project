package no.uis.party;

import no.uis.imagegame.GameLogic;
import no.uis.players.Player;
import no.uis.repositories.ScoreBoardRepository;
import no.uis.websocket.SocketMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessageSendingOperations;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import java.util.ArrayList;
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
    private GameLogic game = new GameLogic();

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

        game.update();
        if (game.isFinished()) {
            SocketMessage finishedMsg = new SocketMessage();
            finishedMsg.setSender("server");
            HashMap<String, Object> content = new HashMap<>();
            finishedMsg.setContent(content);
            content.put("score", game.getScore());
            content.put("time", game.getTime());
            content.put("gameState", game.getCurrentState().toString());
            content.put("state", "FINISHED");

            getGuesser().sendData(finishedMsg, messagingTemplate);
            getProposer().sendData(finishedMsg, messagingTemplate);
            setStatus(PartyStatus.FINISHED_GAME);
        }
    }

    public String getId() {
        return id;
    }

    public GameLogic getGame() {
        return game;
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
        game.addPlayers(guesser, proposer);
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

        game.receiveUpdatesFromFront(this, message);

        HashMap<String, Object> content = new HashMap<>();
        sockMess.setContent(content);
        sockMess.setType(message.getType());

        content.put("score", game.getScore());
        content.put("time", game.getTime());
        content.put("gameState", game.getCurrentState().toString());

        switch (message.getType()) {
            case "REQUEST_SEGMENT":
                content.put("requestSegment", "true");
                content.put("state", getProposer().getPlayerStatus().toString());
                sockMess.setSender(getGuesser().getId());
                getProposer().sendData(sockMess, smos);
                break;
            case "SEND_SEGMENT":
                content.put("segment", message.contentToMap().get("segment"));
                content.put("state", getGuesser().getPlayerStatus().toString());
                sockMess.setSender(getProposer().getId());
                getGuesser().sendData(sockMess, smos);
                break;
            case "SEND_GUESS":
                content.put("guess", message.contentToMap().get("guess"));
                content.put("state", getProposer().getPlayerStatus().toString());
                sockMess.setSender(getGuesser().getId());
                getProposer().sendData(sockMess, smos);
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

//    public int play(Player player,HashMap<String, Object> msg) {
//    	if (this.playerTurn != player) {
//    		return SR.SR_BAD_PLAYER;
//    	}
//    	if (player.getPlayerType()==Player.PlayerType.GUESSER) {
//    		playGuesser(msg);
//    		return SR.SR_OK;
//    	}
//    	else if(player.getPlayerType()==Player.PlayerType.PROPOSER) {
//    		playGuesser(msg);
//    		return SR.SR_OK;
//    	}
//    	else {
//    		return SR.SR_KO;
//    	}
//    }
//    private void playGuesser(Player player, HashMap<String, Object> msg) {
//    	
//    }
//    
//    private void playProposer(Player player, HashMap<String, Object> msg) {
//    	ImageId = msg.get("");
//    	if (this.imageIds.contains(imageId));
//    }
}
