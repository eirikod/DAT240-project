package no.uis.players;

import no.uis.websocket.SocketMessage;
import org.springframework.messaging.simp.SimpMessageSendingOperations;

public class Player {
    private String id;

    private String username;

    private PlayerType type;
    private PlayerStatus status;
    private GameStatus gameStatus = GameStatus.INACTIVE;
    private int score;

    public enum PlayerType {
        GUESSER, PROPOSER
    }

    public enum PlayerStatus {
        WAITING, PLAYING, FINISHED
    }

    public enum GameStatus {
        QUEUEING, PLAYING, INACTIVE
    }

    public Player(String id, String username) {
        this.username = username;
        this.id = id;
    }

    public Player() {
    }

    public Player(String username, PlayerType pType) {
        this.setUsername(username);
        this.setPlayerType(pType);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
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

    public PlayerStatus getPlayerStatus() {
        return status;
    }

    public void setPlayerStatus(PlayerStatus status) {
        this.status = status;
    }

    public GameStatus getGameStatus() {
        return gameStatus;
    }

    public void setGameStatus(GameStatus gameStatus) {
        this.gameStatus = gameStatus;
    }

    public void sendData(SocketMessage message, SimpMessageSendingOperations messageSendingOperations) {
        messageSendingOperations.convertAndSend("/channel/update/" + getId(),
                message);
    }

    public void update(SimpMessageSendingOperations messageSendingOperations) {

    }

    @Override
    public String toString() {
        return "Player [name=" + username + ", id=" + id + "]";
    }
}
