package no.uis.imagegame;

import no.uis.backend_pseudo_game.PartyManager;
import no.uis.backend_pseudo_game.dummy.DummyPlayer;
import org.junit.Test;
import static org.junit.Assert.*;

public class PartyManagerTest {

    @Test
    public void createPartyTest() {
        PartyManager partyManager = new PartyManager();
        DummyPlayer guesser = new DummyPlayer();
        guesser.setPlayerType(DummyPlayer.PlayerType.GUESSER);

        assertFalse(partyManager.isThereAnOpenParty());

        partyManager.queueUpPlayer(guesser);
        partyManager.update();

        assertTrue(partyManager.isThereAnOpenParty());

        assertEquals(1, partyManager.getPartyCount());
    }

    @Test
    public void queuePlayersTest() {
        PartyManager partyManager = new PartyManager();
        DummyPlayer guesser = new DummyPlayer();
        guesser.setPlayerType(DummyPlayer.PlayerType.GUESSER);

        DummyPlayer proposer = new DummyPlayer();
        proposer.setPlayerType(DummyPlayer.PlayerType.PROPOSER);

        partyManager.queueUpPlayer(guesser);
        assertTrue(partyManager.isQueueNotEmpty(DummyPlayer.PlayerType.GUESSER));
        partyManager.update();

        partyManager.queueUpPlayer(proposer);
        assertTrue(partyManager.isQueueNotEmpty(DummyPlayer.PlayerType.PROPOSER));
        partyManager.update();

        assertTrue(partyManager.areBothQueuesEmpty());
    }
}
