package no.uis.party;

import no.uis.players.Player;
import no.uis.websocket.SocketMessage;
import no.uis.websocket.WebSocketEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
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

    /*
    QueueController() {
        TickExecution updater = new TickExecution(1000L, this::update);
        updater.execute();
    }

    private void update() {
        for (Player player : playerList) {
            SocketMessage message = new SocketMessage();
            message.setContent("Haha this is a message!");
            messagingTemplate.convertAndSend(
                    "/channel/test/" + player.getUsername(),
                    message);
        }
    }

     */
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

    @MessageMapping(destinaitionName + "/{roomId}/sendMessage/")
    public void sendMessage(@DestinationVariable String roomId, @Payload SocketMessage socketMessage) {
        String url = format("/channel/%s", roomId);
        messagingTemplate.convertAndSend(url, socketMessage);
    }

    @MessageMapping(destinaitionName + "/{roomId}/addUser")
    public void addUser(@DestinationVariable String roomId, @Payload SocketMessage socketMessage,
                        SimpMessageHeaderAccessor headerAccessor) {
        String currentRoomId = (String) headerAccessor.getSessionAttributes().put("room_id", roomId);
        if (currentRoomId != null) {
            SocketMessage leaveMessage = new SocketMessage();
            leaveMessage.setType("LEAVE");
            leaveMessage.setSender(socketMessage.getSender());
            messagingTemplate.convertAndSend(format("/channel/%s", currentRoomId), leaveMessage);
        }
        headerAccessor.getSessionAttributes().put("username", socketMessage.getSender());
        messagingTemplate.convertAndSend(format("/channel/%s", roomId), socketMessage);
    }

    @GetMapping("/party")
    public String home() {
        return "partyQueue";
    }
}
