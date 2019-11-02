package no.uis.party;

import no.uis.repositories.PlayerRepository;
import no.uis.tools.TickExecution;
import no.uis.players.Player;
import no.uis.websocket.SocketMessage;
import no.uis.websocket.WebSocketEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.Socket;
import java.util.ArrayList;

import static java.lang.String.format;

@Controller
public class QueueController {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketEventListener.class);

    @Autowired
    PlayerRepository repository;

    final static String CONST_PLAY_MODE = "listPlayMode";
    final static String CONST_PLAYER_MODE = "listPlayerMode";
    private static final String DESTINATION = "/party";
    private static PartyManager partyManager = new PartyManager();

    QueueController() {
        TickExecution updater = new TickExecution(1000L, this::update);
        updater.execute();
    }

    private void update() {
        partyManager.update(messagingTemplate);
    }

    @Autowired
    private SimpMessageSendingOperations messagingTemplate;

    @MessageMapping(DESTINATION + "/queueUp")
    public void queueUp(SocketMessage socketMessage) {
        if (repository.findByUsername(socketMessage.getSender()) != null) {
            Player.PlayerType type;
            if ("PROPOSER".equals(socketMessage.getContent())) {
                type = Player.PlayerType.PROPOSER;
            } else {
                type = Player.PlayerType.GUESSER;
            }
            Player player = repository.findByUsername(socketMessage.getSender());
            player.setPlayerType(type);
            partyManager.queueUpPlayer(player);
        }
    }

    @MessageMapping("/party/{roomID}/hello")
    public void test(@DestinationVariable String roomID, SocketMessage message) {
        System.out.println("Hello! ---------------------- room: " + roomID + " | " + message.getContent());
    }


    //WelcomePage controller example
    @RequestMapping("/welcomePage")
    public String newEntry(Model model,
                           @RequestParam(value = "username") String username,
                           @RequestParam(value = "id") String id,
                           @RequestParam(value = "selectedPlayModelabel", required = false, defaultValue = "") String playMode,
                           @RequestParam(value = "selectedPlayerModelabel", required = false, defaultValue = "") String playerMode){
        System.out.println("playMode : " + playMode);
        System.out.println("playerMode : " + playerMode);
        ArrayList<String> listPlayerRole = new ArrayList<String>();
        listPlayerRole.add("GUESSER");
        listPlayerRole.add("PROPOSER");
        model.addAttribute(CONST_PLAY_MODE, listPlayerRole);

        ArrayList<String> listPlayerMode = new ArrayList<String>();
        listPlayerMode.add("SINGLE PLAYER");
        listPlayerMode.add("MULTIPLE PLAYER");
        model.addAttribute(CONST_PLAYER_MODE, listPlayerMode);

        model.addAttribute("username", username);
        model.addAttribute("id", id);
        return "welcomePage";
    }

}
