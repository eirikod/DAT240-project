package no.uis.backend_pseudo_game.dummy;

/**
 * Dummy class of the player. Use this to construct the game code in a similar fashion
 * to connecting real users to the game.
 */
// TODO: Add more documentation
// TODO: Create event handling (such as onFinishedGame, etc.)
public class DummyPlayer {
    private Long id;
    private String username;
    private PlayerType type;
    private PlayerStatus status;
    private int score;

    public enum PlayerType {
        GUESSER, PROPOSER
    }

    public enum PlayerStatus {
        WAITING, PLAYING, FINISHED
    }

    public DummyPlayer() {
        // Creating a random ID for the player internally.
        // TODO: Remove this on release if we intend to generate IDs from Spring Boot
        this.id = (long)(Math.random() * Long.MAX_VALUE);
        this.username = "user_" + this.id;
    }

    public DummyPlayer(String username, PlayerType pType) {
        this.setUsername(username);
        this.setPlayerType(pType);
        this.setPlayerStatus(PlayerStatus.WAITING);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public PlayerType getPlayerType() {
        return type;
    }

    public void setPlayerType(PlayerType playerType) {
        this.type = playerType;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    /**
     * Getter for the player status
     * @return The current player status enum value
     */
    public PlayerStatus getPlayerStatus() {
        return status;
    }

    /**
     * Set the player's current status
     * @param status Player status enum
     */
    public void setPlayerStatus(PlayerStatus status) {
        this.status = status;
    }
}
