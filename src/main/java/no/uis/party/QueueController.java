package no.uis.party;

import no.uis.players.ScoreData;
import no.uis.players.User;
import no.uis.repositories.PlayerRepository;
import no.uis.repositories.ScoreBoardRepository;
import no.uis.tools.TickExecution;
import no.uis.players.Player;
import no.uis.websocket.SocketMessage;
import no.uis.websocket.WebSocketEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

@Controller
public class QueueController {
    @Autowired
    PlayerRepository repository;

    @Autowired
    private ScoreBoardRepository scoreBoardRepository;

    final static String CONST_PLAY_MODE = "listPlayMode";
    final static String CONST_PLAYER_MODE = "listPlayerMode";
    private static final String DESTINATION = "/party";

    QueueController() {
        TickExecution updater = new TickExecution(1000L, this::update);
        updater.execute();
    }

    private void update() {
        PartyManager.update(messagingTemplate, scoreBoardRepository);
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
            User user = repository.findByUsername(socketMessage.getSender());
            if (!PartyManager.isPlayerActive(user.getUsername())) {
                Player player = new Player(user.getId(), user.getUsername());
                player.setPlayerType(type);
                PartyManager.queueUpPlayer(player);
            }
        }
    }

    //WelcomePage controller example
    @RequestMapping("/welcomePage")
    public String newEntry(Model model,
                           @RequestParam(value = "username") String username,
                           @RequestParam(value = "id") String id,
                           @RequestParam(value = "selectedPlayModelabel", required = false, defaultValue = "") String playMode,
                           @RequestParam(value = "selectedPlayerModelabel", required = false, defaultValue = "") String playerMode) {
        ArrayList<String> listPlayerRole = new ArrayList<String>();
        listPlayerRole.add("GUESSER");
        listPlayerRole.add("PROPOSER");
        model.addAttribute(CONST_PLAY_MODE, listPlayerRole);

        ArrayList<String> listPlayerMode = new ArrayList<>();
        listPlayerMode.add("SINGLE PLAYER");
        listPlayerMode.add("MULTIPLE PLAYER");
        model.addAttribute(CONST_PLAYER_MODE, listPlayerMode);

        model.addAttribute("username", username);
        model.addAttribute("id", id);

        if (PartyManager.isPlayerActive(username)) {
            Player player = PartyManager.getActivePlayer(username);
            if (player.getGameStatus() == Player.GameStatus.PLAYING) {
                // TODO: Redirect them to the respective game page
                //return "redirect:";
            }
        }
        model.addAttribute("playerIsSearching", PartyManager.isPlayerActive(username));

        ArrayList<ScoreData> list = createTop5ScoreList();
        model.addAttribute("scoreBoard", list);
        return "welcomePage";
    }

    public ArrayList<ScoreData> createTop5ScoreList() {
        TreeSet<ScoreData> scoreDataSorted = new TreeSet<>((t0, t1) -> t1.score - t0.score);
        if (((List) scoreBoardRepository.findAll()).size() == 0) {
            return new ArrayList<>();
        }
        for (ScoreData scoreData : scoreBoardRepository.findAll()) {
            scoreDataSorted.add(scoreData);
        }

        int max = 5;
        Iterator iterator = scoreDataSorted.iterator();
        if (scoreDataSorted.size() < 5) {
            max = scoreDataSorted.size();
        }

        ArrayList<ScoreData> list = new ArrayList<>();
        for (int i = 0; i < max; i++) {
            list.add(i, (ScoreData) iterator.next());
        }
        return list;
    }

}
