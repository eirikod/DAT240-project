package no.uis.players;

import org.apache.logging.log4j.util.BiConsumer;
import org.springframework.messaging.simp.SimpMessageSendingOperations;

import java.util.HashMap;
import java.util.Map;

public class PlayerList {
    private HashMap<String, Player> players = new HashMap<>();

    public Player getPlayer(String username) {
        return players.get(username);
    }

    public void addPlayer(Player player) {
        if (!players.containsKey(player.getUsername())) {
            players.put(player.getUsername(), player);
        }
    }

    public void removePlayer(String username) {
        players.remove(username);
    }

    public boolean hasPlayer(String username) {
        return players.containsKey(username);
    }
}
