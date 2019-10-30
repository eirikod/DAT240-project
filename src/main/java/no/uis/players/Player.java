package no.uis.players;

import no.uis.websocket.SocketMessage;
import org.springframework.messaging.simp.SimpMessageSendingOperations;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Player {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	private String username;
	private PlayerType type;
	private PlayerStatus status;
	private int score;

	public enum PlayerType {
		GUESSER, PROPOSER
	};

	public enum PlayerStatus {
		WAITING, PLAYING, FINISHED
	};

	protected Player() {
	}

	public Player(String username, PlayerType pType) {
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

	public PlayerStatus getPlayerStatus() {
		return status;
	}

	public void setPlayerStatus(PlayerStatus status) {
		this.status = status;
	}

	public void update(SimpMessageSendingOperations messagingTemplate) {
		SocketMessage message = new SocketMessage();
		message.setContent("Haha this is a message!");
		messagingTemplate.convertAndSend("/channel/update/" + getUsername(),
				message);
	}
}
