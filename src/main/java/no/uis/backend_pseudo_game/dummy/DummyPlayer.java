package no.uis.backend_pseudo_game.dummy;

/**
 * Dummy class of the player. Use this to construct the game code in a similar fashion
 * to connecting real users to the game.
 */

// TODO: Create event handling (such as onFinishedGame, etc.)
public class DummyPlayer {
    private Long id;
    private String username;
    private PlayerType type;
    private PlayerStatus status;
    private int score;

    /**
     * Defines the various enums for PlayerType
     */
    public enum PlayerType {
        GUESSER, PROPOSER
    }

    /**
     * Defines the various enums for PlayerStatus
     */
    public enum PlayerStatus {
        WAITING, PLAYING, FINISHED
    }

    /**
     * Creating a random id for the player internally
     */
    public DummyPlayer() {
        // TODO: Remove this on release if we intend to generate IDs from Spring Boot
        this.id = (long)(Math.random() * Long.MAX_VALUE);
        this.username = "user_" + this.id;
    }

    /**
     * Creates DummyPlayer object
     * @param username updates the username
     * @param pType updates the PlayerType
     */
    public DummyPlayer(String username, PlayerType pType) {
        this.setUsername(username);
        this.setPlayerType(pType);
        this.setPlayerStatus(PlayerStatus.WAITING);
    }

    /**
     * Getter for player id
     * @return the specific player id
     */
    public Long getId() {
        return id;
    }

    /**
     * Setter for player id
     * @param updates the specific player id
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Getter for the players username
     * @return the specific username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Set the players username
     * @param get the specific username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Getter for the player enum type
     * @return PlayerType GUESSER or PROPOSER
     */
    public PlayerType getPlayerType() {
        return type;
    }

    /**
     * Set the player enum PlayerType
     * @param PlayerType GUESSER or PROPOSER
     */
    public void setPlayerType(PlayerType playerType) {
        this.type = playerType;
    }

    /**
     * Getter for the score
     * @return the score for player
     */
    public int getScore() {
        return score;
    }

    /**
     * Setter for the score
     * @param score
     */
    public void setScore(int score) {
        this.score = score;
    }

    /**
     * Getter for the player status
     * @return The current player status enum value
     * @author Alan Rostem
     */
    public PlayerStatus getPlayerStatus() {
        return status;
    }

    /**
     * Set the player's current status. // TODO: Call respective necessary event methods.
     * @param status Player status enum
     * @author Alan Rostem
     */
    public void setPlayerStatus(PlayerStatus status) {
        this.status = status;
    }
}
