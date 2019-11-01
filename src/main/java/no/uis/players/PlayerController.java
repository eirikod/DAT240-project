package no.uis.players;

import no.uis.tools.FileParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import no.uis.players.Player.PlayerType;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Controller
public class PlayerController {
    private static final String PLAYER_DATA_FILE_PATH = "src/main/resources/data/player_data.txt";
//    @Autowired
//    PlayerRepository entryRepository;

    static HashMap<String, Player> playerHashMap = new HashMap<>();

    public static Player getPlayer(String username) {
        return playerHashMap.get(username);
    }

    PlayerController() {
        FileParser.parseText(PLAYER_DATA_FILE_PATH, (String line) -> {
            if (line.length() > 0) {
                String[] strings = line.split(",");
                String username = strings[0];
                String password = strings[1];
                Player player = new Player(username, password);
                playerHashMap.put(username, player);
            }
        });
    }

    @RequestMapping("/")
    public String home(Model model) {
        return "login-register";
    }

    @RequestMapping("reg-post")
    public String showUser(Model model,
                           @RequestParam(value = "reg_username") String reg_username,
                           @RequestParam(value = "reg_password") String reg_password,
                           @RequestParam(value = "reg_confirmpass") String reg_confirmpass
    ) {
        if (!playerHashMap.containsKey(reg_username)) {
            if (reg_confirmpass.equals(reg_password)) {
                model.addAttribute("username", reg_username);
                registerUser(reg_username, reg_password);
                return "showUser";
            }
            return "login-register"; // TODO: Tell user to confirm pass
        }
        return "login-register"; // TODO: Tell user that username exists
    }

    private static void registerUser(String username, String password) {
        Player player = new Player(username, password);
        playerHashMap.put(username, player);
        String playerData = username + "," + password + "\n";
        FileParser.writeTo(PLAYER_DATA_FILE_PATH, playerData);
        System.out.println("Registered a new user: " + playerHashMap.get(username));
    }

    @RequestMapping(value = "/player", method = RequestMethod.GET)
    public String newEntry(Model model) {
        model.addAttribute("pageTitle", "New Player");
        model.addAttribute("givenAction", "/player");
        model.addAttribute("givenUserName", "");
        model.addAttribute("givenPlayerType", "");
        return "player";
    }

    @RequestMapping(value = "/player", method = RequestMethod.POST)
    public String addEntry(@RequestParam String username, @RequestParam PlayerType type) {
    	Player newEntry = new Player(username, type);
        return "redirect:/";
    }

}
