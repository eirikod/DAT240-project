package no.uis.players;

import no.uis.repositories.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import no.uis.players.Player.PlayerType;

@Controller
public class PlayerController {

    @Autowired
    private PlayerRepository playerRepository;

    @RequestMapping("/")
    public String home(Model model) {
        return "login-register";
    }

    @RequestMapping("reg-post")
    public String register(Model model,
                           @RequestParam(value = "reg_username") String reg_username,
                           @RequestParam(value = "reg_password") String reg_password,
                           @RequestParam(value = "reg_confirmpass") String reg_confirmpass
    ) {
        if (playerRepository.findByUsername(reg_username).size() == 0) {
            if (reg_confirmpass.equals(reg_password)) {
                createUser(reg_username, reg_password);
                model.addAttribute("username", playerRepository.findByUsername(reg_username).get(0).getUsername());
                return "showUser";
            }
            return "login-register"; // TODO: Tell user to confirm pass
        }
        return "login-register"; // TODO: Tell user that username exists
    }

    @RequestMapping("login-post")
    public String login(Model model,
                        @RequestParam(value = "login_username") String login_username,
                        @RequestParam(value = "login_password") String login_password) {
        if (playerRepository.findByUsername(login_username).size() == 1) {
            Player player = playerRepository.findByUsername(login_username).get(0);
            if (player.getPassword().equals(login_password)) {
                model.addAttribute("username", player.getUsername());
                return "showUser";
            }
        }
        return "login-register"; // TODO: Tell user that credentials were invalid
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

    private void createUser(String username, String password) {
        Player player = new Player(username, password);
        playerRepository.save(player);
        System.out.println("Created a new player: " + player);
    }

}
