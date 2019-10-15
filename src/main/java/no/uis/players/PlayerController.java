package no.uis.players;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import no.uis.players.Player.PlayerType;

import java.util.List;


@Controller
public class PlayerController {
//    @Autowired
//    PlayerRepository entryRepository;

    @RequestMapping("/")
    public String home(Model model){
//        model.addAttribute("entries", allEntries);
        return "home";
    }

    @RequestMapping(value = "/player", method = RequestMethod.GET)
    public String newEntry(Model model) {
        model.addAttribute("pageTitle", "New Player");
        model.addAttribute("givenAction", "/player");
        model.addAttribute("givenUserName", "");
        model.addAttribute("givenPlayerType", "");
        return "entry";
    }

    @RequestMapping(value = "/player", method = RequestMethod.POST)
    public String addEntry(@RequestParam String username, @RequestParam PlayerType type) {
    	Player newEntry = new Player(username, type);
        return "redirect:/";
    }

}
