package no.uis.imagegame;

import static org.junit.Assert.*;

import org.junit.Test;

import no.uis.backend_pseudo_game.Game;
import no.uis.backend_pseudo_game.dummy.DummyPlayer;

public class GameTest {
	
	/**
	 * Testing init of new game
	 * @author Eirik & Markus
	 */
	@Test
	public void newGame_instantiate() {
		DummyPlayer testPlayer1 = new DummyPlayer();
		DummyPlayer testPlayer2 = new DummyPlayer();
		Game testGame = new Game(testPlayer1, testPlayer2);
		assertEquals(true, testPlayer1 instanceof DummyPlayer);
		assertEquals(true, testPlayer2 instanceof DummyPlayer);
		assertEquals(true, testGame instanceof Game);
	}
}




