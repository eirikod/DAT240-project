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
            User user = repository.findByUsername(socketMessage.getSender());
            if (!partyManager.isPlayerActive(user.getUsername())) {
                Player player = new Player(user.getId(), user.getUsername());
                player.setPlayerType(type);
                partyManager.queueUpPlayer(player);
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

        if (partyManager.isPlayerActive(username)) {
            Player player = partyManager.getActivePlayer(username);
            if (player.getGameStatus() == Player.GameStatus.PLAYING) {
                // TODO: Redirect them to the respective game page
                //return "redirect:";
            }
        }
        model.addAttribute("playerIsSearching", partyManager.isPlayerActive(username));

        makeScoresForTest();
        ArrayList<ScoreData> list = createTop5ScoreList();
        model.addAttribute("scoreBoard", list);
        return "welcomePage";
    }

    public ArrayList<ScoreData> createTop5ScoreList() {
        TreeSet<ScoreData> scoreDataSorted = new TreeSet<>((t0, t1) -> t1.score - t0.score);
        for (ScoreData scoreData : scoreBoardRepository.findAll()) {
            scoreDataSorted.add(scoreData);
        }

        Iterator iterator = scoreDataSorted.iterator();

        ArrayList<ScoreData> list = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            list.add(i, (ScoreData) iterator.next());
        }
        return list;
    }

    void makeScoresForTest() {
        scoreBoardRepository.save(new ScoreData(1L, "pro_hello", "guess_yoyo", 696));
        scoreBoardRepository.save(new ScoreData(2L, "nono", "ayyy", 890));
        scoreBoardRepository.save(new ScoreData(3L, "ayyy", "lmao", 105550));
        scoreBoardRepository.save(new ScoreData(4L, "blabla", "poop", 143200));
        scoreBoardRepository.save(new ScoreData(5L, "pro_hello", "something", 43));
        scoreBoardRepository.save(new ScoreData(6L, "something", "xD", 87));
        scoreBoardRepository.save(new ScoreData(7L, "something", "xD", 5));
        scoreBoardRepository.save(new ScoreData(8L, "something", "xD", 4));
        scoreBoardRepository.save(new ScoreData(9L, "something", "xD", 3));
        scoreBoardRepository.save(new ScoreData(10L, "something", "xD", 2));
        scoreBoardRepository.save(new ScoreData(11L, "something", "xD", 1));
    }

}
