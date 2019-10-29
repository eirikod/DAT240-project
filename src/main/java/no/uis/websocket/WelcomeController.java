package no.uis.websocket;

import no.uis.websocket.Greeting;
import no.uis.websocket.HelloMessage;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.util.HtmlUtils;

@Controller
public class WelcomeController {

    @MessageMapping("/home")
    @SendTo("/welcome/client")
    public Greeting greeting(HelloMessage message) throws Exception {
        Thread.sleep(1000); // simulated delay
        return new Greeting("Hello, " + HtmlUtils.htmlEscape(message.getName()) + "!");
    }

    @GetMapping("/welcome")
    public String home() {
        return "wstest"; // TODO: Use the correct template
    }

}
