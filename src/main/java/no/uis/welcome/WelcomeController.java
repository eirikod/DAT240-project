package no.uis.welcome;

import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.util.HtmlUtils;

@Controller
public class WelcomeController {
    
    @MessageMapping("/home")
    @SendToUser("/broker/chat")
    public Greeting greeting(HelloMessage message) throws Exception {
        Thread.sleep(100); // simulated delay
        return new Greeting(HtmlUtils.htmlEscape(message.getMessage()));
    }

    @MessageExceptionHandler
    @SendToUser("/broker/errors")
    public String handleException(Throwable exception) {
        return exception.getMessage();
    }

    @GetMapping("/welcome")
    public String home() {
        return "wstest"; // TODO: Use the correct template
    }

}
