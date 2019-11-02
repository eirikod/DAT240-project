package no.uis.imagegame;


import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;


@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class imageControllerTest {
	
	@Autowired
	private MockMvc mvc;
	
	@Test
	public void guesserModelAndViewTest() {
		try {
			mvc.perform(MockMvcRequestBuilders.get("/proposer").accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8"))
			.andExpect(status().isFound());
			
			//mvc.perform(MockMvcRequestBuilders.get("/proposer?"));
			
		} catch (Exception e) {
			System.out.println("test failed");
			e.printStackTrace();
			// TODO: handle exception
		}
		
	}
	
	
	
	

}
