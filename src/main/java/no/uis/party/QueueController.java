package no.uis.party;

import no.uis.tools.TickExecution;
import no.uis.players.Player;
import no.uis.websocket.SocketMessage;
import no.uis.websocket.WebSocketEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.HashMap;

import static java.lang.String.format;

@Controller
public class QueueController {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketEventListener.class);

    private static final String destinaitionName = "/party";
    private static PartyManager partyManager = new PartyManager();
    private static HashMap<String, Player> players = new HashMap<>();
    private static ArrayList<Player> playerList = new ArrayList<>();

    QueueController() {
        TickExecution updater = new TickExecution(1000L, this::update);
        updater.execute();
    }

    private void update() {
        partyManager.update(messagingTemplate);
    }

    @Autowired
    private SimpMessageSendingOperations messagingTemplate;

    @MessageMapping(destinaitionName + "/queueUp")
    public void queueUp(SocketMessage socketMessage) {
        if (!players.containsKey(socketMessage.getSender())) {
            Player.PlayerType type;
            if ("proposer".equals(socketMessage.getContent())) {
                type = Player.PlayerType.PROPOSER;
            } else {
                type = Player.PlayerType.GUESSER;
            }
            Player player = new Player(socketMessage.getSender(), type);
            playerList.add(player);
            players.put(socketMessage.getSender(), player);
            partyManager.queueUpPlayer(player);
        }
    }

    @GetMapping("/party")
    public String home() {
        return "partyQueue";
    }
}
