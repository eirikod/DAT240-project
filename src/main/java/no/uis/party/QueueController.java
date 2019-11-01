package no.uis.party;

import no.uis.imagegame.ImageController;
import no.uis.players.User;
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
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.HashMap;

import static java.lang.String.format;

@Controller
public class QueueController {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketEventListener.class);

    final static String CONST_PLAY_MODE = "listPlayMode";
    final static String CONST_PLAYER_MODE = "listPlayerMode";
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
