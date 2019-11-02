package no.uis.players;

import no.uis.websocket.SocketMessage;
import org.springframework.messaging.simp.SimpMessageSendingOperations;

public class Player {
    private Long id;

    private String username;

    private PlayerType type;
    private PlayerStatus status = PlayerStatus.INACTIVE;
    private int score;

    public enum PlayerType {
        GUESSER, PROPOSER
    }

    public enum PlayerStatus {
        INACTIVE, WAITING, PLAYING, FINISHED
    }

    public Player(Long id, String username) {
        this.username = username;
        this.id = id;
    }

    public Player() {
    }

    public Player(String username, PlayerType pType) {
        this.setUsername(username);
        this.setPlayerType(pType);
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

    public PlayerStatus getPlayerStatus() {
        return status;
    }

    public void setPlayerStatus(PlayerStatus status) {
        this.status = status;
    }

    public void sendData(SocketMessage message, SimpMessageSendingOperations messageSendingOperations) {
        messageSendingOperations.convertAndSend("/channel/update/" + getId(),
                message);
        //System.out.println("We sent a message to " + getUsername());
    }

    public void update(SimpMessageSendingOperations messageSendingOperations) {

    }

    @Override
    public String toString() {
        return "Player [name=" + username + ", id=" + id + "]";
    }
}
