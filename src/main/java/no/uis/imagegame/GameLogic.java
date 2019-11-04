package no.uis.imagegame;

import no.uis.backend_pseudo_game.dummy.DummyImageLabelReader;
import no.uis.backend_pseudo_game.dummy.DummyPlayer;
import no.uis.backend_pseudo_game.dummy.DummyPlayer.PlayerType;
import no.uis.party.Party;
import no.uis.players.Player;
import no.uis.websocket.SocketMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.messaging.simp.SimpMessageSendingOperations;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * defining the basic game logic
 *
 * @author Eirik & Markus
 */
public class GameLogic {
    private static final int MAX_GUESSES = 3;
    private static final int LOSS_INTERVAL = 10; // Seconds

    private String image;
    private ArrayList<String> proposerSegments;
    private ArrayList<String> guesserSegments;

    private Player proposer;
    private Player guesser;
    private GameState currentState = GameState.WAITING_TO_START;
    private boolean finished = false;
    private int time = 0;
    private int currentGuesses = 0;
    private int remainingPoints = 1000;

    public String getImageName() {
        return image;
    }

    public enum GameState {
        WAITING_TO_START,
        PLAYING,
        WIN,
        LOST
    }

    private HashMap<String, GameResponseFunction> responseMapping = new HashMap<>();

    @FunctionalInterface
    public interface GameResponseFunction {
        void apply(Party party, SocketMessage message);
    }

    public GameLogic() {
        responseMapping.put("SEND_SEGMENT", (party, message) -> {
            guesser.setPlayerStatus(Player.PlayerStatus.PLAYING);
            proposer.setPlayerStatus(Player.PlayerStatus.WAITING);
            chooseSegment((String) message.contentToMap().get("segment"));
            time = 0;
        });

        responseMapping.put("SEND_GUESS", (party, message) -> {
            if (checkAnswer((String) message.contentToMap().get("guess"))) {
                guesser.setPlayerStatus(Player.PlayerStatus.FINISHED);
                proposer.setPlayerStatus(Player.PlayerStatus.FINISHED);
                currentState = GameState.WIN;
                finished = true;
            }
        });

        responseMapping.put("REQUEST_SEGMENT", (party, message) -> giveUp());
    }

    public void addPlayers(Player guesser, Player proposer) {
        this.guesser = guesser;
        this.proposer = proposer;
    }

    public void setImage(String image, int imageCount) {
        this.image = image;
        guesserSegments = new ArrayList<>();
        proposerSegments = new ArrayList<>();
        for (int i = 0; i < imageCount; ++i) {
            proposerSegments.add(Integer.toString(i));
        }
        System.out.println("Segment count for " + image + ": " + proposerSegments.size());
        currentState = GameState.PLAYING;
    }

    /**
     * @author Eirik & Markus
     */
    public void nextRound() {
        currentGuesses = 0;
        guesser.setPlayerStatus(Player.PlayerStatus.WAITING);
        proposer.setPlayerStatus(Player.PlayerStatus.PLAYING);
    }

    /**
     * checks answer
     *
     * @author Eirik & Markus
     */
    public boolean checkAnswer(String guess) {
        currentGuesses++;
        if (currentGuesses == MAX_GUESSES) {
            nextRound();
        }
        return guess.equals(this.image);
    }

    /**
     * @author Eirik & Markus
     */
    public void giveUp() {
        nextRound();
    }

    /**
     * chooses new segment for new round
     *
     * @author Eirik & Markus
     */
    public void chooseSegment(String segmentID) {
        if (!guesserSegments.contains(segmentID)) {
            guesserSegments.add(segmentID);
            System.out.println(segmentID + " added to the guesser segments");
        }
    }

    public void receiveUpdatesFromFront(Party party, SocketMessage message) {
        if (currentState == GameState.PLAYING) {
            responseMapping.get(message.getType()).apply(party, message);
        }
    }

    public void update() {
        if (currentState == GameState.PLAYING) {
            if (proposer.getPlayerStatus() != Player.PlayerStatus.PLAYING) {
                time++;
            }

            if (time % LOSS_INTERVAL == 0) {
                remainingPoints--;
            }

            if (guesserSegments.size() == proposerSegments.size() || remainingPoints == 0) {
                currentState = GameState.LOST;
                guesser.setPlayerStatus(Player.PlayerStatus.FINISHED);
                proposer.setPlayerStatus(Player.PlayerStatus.FINISHED);
                finished = true;
            }
        }
    }

    /**
     * Writes out score for the paired players
     *
     * @author Eirik & Markus
     */
    public int getScore() {
        int score = (int) ((1f - (float)guesserSegments.size() / (float)proposerSegments.size()) * remainingPoints);
        System.out.println("Your score is " + score + "/1000");
        return score;
    }

    public int getTime() {
        return time;
    }

    public ArrayList<String> getGuesserSegments() {
        return guesserSegments;
    }

    public GameState getCurrentState() {
        return currentState;
    }

    public boolean isFinished() {
        return finished;
    }
}
