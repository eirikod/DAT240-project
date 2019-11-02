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
        User user = new User(username, password);
        System.out.println(user);
        playerRepository.save(user);
        System.out.println("Created a new user: " + user);
        return user;
    }

    private boolean userExists(String username) {
        return playerRepository.findByUsername(username) != null;
    }
}
