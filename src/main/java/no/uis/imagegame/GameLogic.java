package no.uis.imagegame;

import no.uis.backend_pseudo_game.dummy.DummyImageLabelReader;
import no.uis.backend_pseudo_game.dummy.DummyPlayer;
import no.uis.backend_pseudo_game.dummy.DummyPlayer.PlayerType;
import no.uis.party.Party;
import no.uis.players.Player;
import no.uis.websocket.SocketMessage;
import org.springframework.messaging.simp.SimpMessageSendingOperations;

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

    ImageLabelReader labelReader = new ImageLabelReader("src/main/resources/static/label/label_mapping.csv",
            "src/main/resources/static/label/image_mapping.csv");

    Scanner sc = new Scanner(System.in);

    private static final int MAX_GUESSES = 3;

    private String image;
    private ArrayList<String> proposerSegments;
    private ArrayList<String> guesserSegments;

    private boolean isCorrect = false;
    private boolean lost = false;
    private int currentGuesses = 0;
    private Player proposer;
    private Player guesser;
    private GameState currentState = GameState.WAITING_TO_START;
    private boolean finished = false;


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
        });

        responseMapping.put("SEND_GUESS", (party, message) -> {
            if (checkAnswer((String) message.contentToMap().get("guess"))) {
                guesser.setPlayerStatus(Player.PlayerStatus.FINISHED);
                proposer.setPlayerStatus(Player.PlayerStatus.FINISHED);
                finished = true;
            }
        });

        responseMapping.put("REQUEST_SEGMENT", (party, message) -> nextRound());
    }

    public void addPlayers(Player guesser, Player proposer) {
        this.guesser = guesser;
        this.proposer = proposer;
    }

    public void setImage(String image) {
        this.image = image;
        proposerSegments = new ArrayList<>();
        guesserSegments = new ArrayList<>();
        for (int i = 0; i < 49; ++i) {
            proposerSegments.add(Integer.toString(i));
        }
        currentState = GameState.PLAYING;
    }

    /**
     * starts the game, continues until correct answer or lost
     *
     * @author Eirik & Markus
     */
    public void play() {
        while (!isCorrect && !lost) {
            nextRound();
        }
        getScore();
    }

    /**
     * @return false if out of guesses/gives up, true if right answer
     * @author Eirik & Markus
     * @see play()
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
        return isCorrect = guess.equals(this.image);
    }

    /**
     * @return true if player gives up round
     * @author Eirik & Markus
     */
    public boolean giveUp() {
        System.out.println("Give up? y/n");
        return (sc.nextLine().equals("y"));
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
            if (guesserSegments.size() == proposerSegments.size()) {
                currentState = GameState.LOST;
                guesser.setPlayerStatus(Player.PlayerStatus.FINISHED);
                proposer.setPlayerStatus(Player.PlayerStatus.FINISHED);
                finished = true;
                System.out.println("LoST GAME WOOOOOOOOOOOO!!!!!!!!!!1111!!!111ONE!!!!");
            }
        }
    }

    /**
     * Writes out score for the paired players
     *
     * @author Eirik & Markus
     */
    public int getScore() {
        int score = lost ? 0 : (proposerSegments.size() - guesserSegments.size()) * 2;
        System.out.println("Your score is " + score + "/100");
        return score;
    }

    public GameState getCurrentState() {
        return currentState;
    }

    public boolean isFinished() {
        return finished;
    }
}
