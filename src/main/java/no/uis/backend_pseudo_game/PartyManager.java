package no.uis.backend_pseudo_game;

import no.uis.backend_pseudo_game.dummy.DummyPlayer;

import static no.uis.backend_pseudo_game.dummy.DummyPlayer.PlayerType.*;

import static no.uis.backend_pseudo_game.Party.PartyStatus.*;

import java.util.ArrayDeque;
import java.util.ArrayList;


/**
 * Singleton class that manages what party a player goes into depending on the role they select
 */
public class PartyManager {
    private ArrayDeque<DummyPlayer> proposerQueue = new ArrayDeque<>();
    private ArrayDeque<DummyPlayer> guesserQueue = new ArrayDeque<>();
    private ArrayList<Party> parties = new ArrayList<>();

    public Party createParty() {
        Party party = new Party();
        parties.add(party);
        return party;
    }

    /**
     * Method used to update the game sequentially. Party management core logic occurs here
     */
    public void update() {
        // TODO: Pop the queue and create parties if two players are ready
    }

    /**
     * Adds a player to the guesser queue if given PlayerType is GUESSER
     *
     * @param guesser Player with PlayerType GUESSER
     */
    private void queueUpGuesser(DummyPlayer guesser) {
        guesserQueue.add(guesser);
        // guesser.setPlayerStatus(DummyPlayer.PlayerStatus.WAITING);
        System.out.println("We queued a guesser! " + guesser.getUsername() + " Queue count: " + guesserQueue.size());
    }

    /**
     * Adds a player to the proposer queue if given PlayerType is PROPOSER
     *
     * @param proposer Player with PlayerType GUESSER
     */
    private void queueUpProposer(DummyPlayer proposer) {
        proposerQueue.add(proposer);
        // proposer.setPlayerStatus(DummyPlayer.PlayerStatus.WAITING);
        System.out.println("We queued a proposer named " + proposer.getUsername() + "! Queue count: " + proposerQueue.size());
    }

    /**
     * Queues up a player for a party. The player is added into a queue respective to its PlayerType
     *
     * @param player Either a proposer or guesser depending on the PlayerType
     * @see no.uis.backend_pseudo_game.dummy.DummyPlayer.PlayerType
     */
    public void queueUpPlayer(DummyPlayer player) {
        switch (player.getPlayerType()) {
            case PROPOSER:
                queueUpProposer(player);
                break;
            case GUESSER:
                queueUpGuesser(player);
                break;
        }
    }

    public static void main(String[] args) {
        PartyManager partyManager = new PartyManager();
        partyManager.queueUpPlayer(new DummyPlayer("alan", GUESSER));

    }
}
