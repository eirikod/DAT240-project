package no.uis.imagegame;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableAutoConfiguration
@ComponentScan({
        "no.uis.websocket",
        "no.uis.imagegame",
        "no.uis.players",
        "no.uis.party",
        "no.uis.configs",
})
public class ImagegameApplication implements WebMvcConfigurer {

    public static void main(String[] args) {
        SpringApplication.run(ImagegameApplication.class, args);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**").addResourceLocations("classpath:/static/");
    }

}
