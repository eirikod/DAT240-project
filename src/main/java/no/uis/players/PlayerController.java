package no.uis.players;

import no.uis.repositories.PlayerRepository;
import no.uis.repositories.ScoreBoardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import no.uis.players.Player.PlayerType;

import java.util.*;

@Controller
public class PlayerController {

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private ScoreBoardRepository scoreBoardRepository;


    @RequestMapping("/scoreboard")
    public String scoreboardTest(Model model) {
        scoreBoardRepository.save(new ScoreData(1L, "pro_hello", "guess_yoyo", 696));
        scoreBoardRepository.save(new ScoreData(2L, "nono", "ayyy", 890));
        scoreBoardRepository.save(new ScoreData(3L, "ayyy", "lmao", 105550));
        scoreBoardRepository.save(new ScoreData(4L, "blabla", "poop", 143200));
        scoreBoardRepository.save(new ScoreData(5L, "pro_hello", "something", 43));
        scoreBoardRepository.save(new ScoreData(6L, "something", "xD", 87));

        Comparator<ScoreData> comparator = (scoreData, t1) -> t1.score - scoreData.score;

        Iterable<ScoreData> originalList = scoreBoardRepository.findAll();
        TreeSet<ScoreData> newList = new TreeSet<>(comparator);
        for (ScoreData scoreData : originalList) {
            newList.add(scoreData);
        }
        Iterator iterator = newList.iterator();

        String[] list = new String[3];
        for (int i = 0; i < list.length; i++) {
            list[i] = iterator.next().toString();
        }
        model.addAttribute("scoreList", list);
        return "scoreTest";
    }

    @RequestMapping("/")
    public String home(Model model) {
        return "loginPage";
    }

    @RequestMapping("reg-post")
    public String register(Model model,
                           @RequestParam(value = "reg_username") String reg_username,
                           @RequestParam(value = "reg_password") String reg_password,
                           @RequestParam(value = "reg_confirmpass") String reg_confirmpass
    ) {
        if (!userExists(reg_username)) {
            if (reg_confirmpass.equals(reg_password)) {
                User player = createUser(reg_username, reg_password);
                return "redirect:welcomePage?username=" + reg_username + "&id=" + player.getId();
            }
            model.addAttribute("invalid_message", "Please confirm your password.");
            return "loginPage";
        }
        model.addAttribute("invalid_message", "This username already exists!");
        return "loginPage";
    }

    @RequestMapping("login-post")
    public String login(Model model,
                        @RequestParam(value = "login_username") String login_username,
                        @RequestParam(value = "login_password") String login_password) {
        if (userExists(login_username)) {
            User player = playerRepository.findByUsername(login_username);
            if (player.getPassword().equals(login_password)) {
                model.addAttribute("username", player.getUsername());
                return "redirect:welcomePage?username=" + login_username + "&id=" + player.getId();
            }
        }
        model.addAttribute("invalid_message", "Username or password is incorrect!");
        return "loginPage";
    }

    private User createUser(String username, String password) {
        User player = new User(username, password);
        playerRepository.save(player);
        System.out.println("Created a new user: " + player);
        return player;
    }

    private boolean userExists(String username) {
        return playerRepository.findByUsername(username) != null;
    }
}
