package no.uis.imagegame;


import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
//import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import antlr.debug.GuessingEvent;
import no.uis.players.Player;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;



@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
//@ContextConfiguration("/applicationContext.xml")
public class imageControllerTest {
	
	@Autowired
	private MockMvc mvc;
	
	@Test
	public void testGame() throws Exception {
		
		
		//Player guesser = new Player("Test", Player.PlayerType.GUESSER);
		
		
		mvc.perform(MockMvcRequestBuilders.get("/").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
		.andExpect(content().string("WELCOME TO GUESS WHAT?"));
			
		
	}

}
