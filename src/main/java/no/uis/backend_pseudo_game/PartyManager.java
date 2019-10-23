package no.uis.backend_pseudo_game;

import no.uis.backend_pseudo_game.dummy.DummyPlayer;
import no.uis.backend_pseudo_game.tools.TickExecution;

import static no.uis.backend_pseudo_game.dummy.DummyPlayer.PlayerType.*;

import static no.uis.backend_pseudo_game.Party.PartyStatus.*;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Singleton class that manages what party a player goes into depending on the role they select
 *
 * @author Alan Rostem
 */
public class PartyManager {
    private ArrayDeque<DummyPlayer> proposerQueue = new ArrayDeque<>();
    private ArrayDeque<DummyPlayer> guesserQueue = new ArrayDeque<>();
    private ArrayList<Party> parties = new ArrayList<>();

    private Party currentOpenParty;
    private DummyPlayer currentlyWaitingProposer;
    private DummyPlayer currentlyWaitingGuesser;

    /**
     * Creates a new party and pushes them into the array list where we update them in the future.
     *
     * @return Party
     * @author Alan Rostem
     */
    private Party createParty() {
        Party party = new Party();
        parties.add(party);
        return party;
    }

    /**
     * Checks if the given type of player is waiting to be put into a party
     *
     * @param type PlayerType
     * @return boolean
     * @author Alan Rostem
     */
    private boolean isPlayerWaitingForParty(DummyPlayer.PlayerType type) {
        switch (type) {
            case PROPOSER:
                return currentlyWaitingProposer != null;
            case GUESSER:
                return currentlyWaitingGuesser != null;
            default:
                return false;
        }
    }

    /**
     * Checks if there is an open party available
     *
     * @return boolean
     * @author Alan Rostem
     */
    private boolean isThereAnOpenParty() {
        return currentOpenParty != null;
    }

    /**
     * Method used to update the game sequentially. Party management core logic occurs here
     *
     * @author Alan Rostem
     */
    public void update() {
        if (!areBothQueuesEmpty()) {
            if (!isThereAnOpenParty()) {
                currentOpenParty = createParty();
                System.out.println("Party opened! Parties created: " + parties.size());
            }
            // If both types of player are waiting, add them to the newly created party
            if (isPlayerWaitingForParty(GUESSER) && isPlayerWaitingForParty(PROPOSER)) {
                currentOpenParty.setGuesser(currentlyWaitingGuesser);
                currentOpenParty.setProposer(currentlyWaitingProposer);
                currentlyWaitingGuesser = null; // Guesser no longer waiting
                currentlyWaitingProposer = null; // Proposer no longer waiting
                currentOpenParty.setStatus(READY_TO_PLAY);
                currentOpenParty = null; // Party is now closed
                System.out.println("Both users put into party. Next!");
            } else {
                // If one or the other waiting player is non-existent, take them
                // out of the queue and set them if the queues are not empty.
                if (isQueueNotEmpty(GUESSER)) {
                    currentlyWaitingGuesser = guesserQueue.pop();
                    System.out.println("The guesser " + currentlyWaitingGuesser.getUsername() + " has waited long enough!");
                }

                if (isQueueNotEmpty(PROPOSER)) {
                    currentlyWaitingProposer = proposerQueue.pop();
                    System.out.println("The proposer " + currentlyWaitingProposer.getUsername() + " has waited long enough!");
                }
            }
        }
    }

    /**
     * Check if both respective player queues are empty
     *
     * @return boolean
     * @author Alan Rostem
     */
    private boolean areBothQueuesEmpty() {
        return proposerQueue.size() == 0 && guesserQueue.size() == 0;
    }

    private boolean isQueueNotEmpty(DummyPlayer.PlayerType type) {
        switch (type) {
            case PROPOSER:
                return proposerQueue.size() > 0;
            case GUESSER:
                return guesserQueue.size() > 0;
            default:
                return true;
        }
    }

    /**
     * Adds a player to the guesser queue if given PlayerType is GUESSER
     *
     * @param guesser Player with PlayerType GUESSER
     * @author Alan Rostem
     */
    private void queueUpGuesser(DummyPlayer guesser) {
        guesserQueue.add(guesser);
        System.out.println("We queued a guesser named " + guesser.getUsername() + "! Queue count: " + guesserQueue.size());
    }

    /**
     * Adds a player to the proposer queue if given PlayerType is PROPOSER
     *
     * @param proposer Player with PlayerType GUESSER
     * @author Alan Rostem
     */
    private void queueUpProposer(DummyPlayer proposer) {
        proposerQueue.add(proposer);
        System.out.println("We queued a proposer named " + proposer.getUsername() + "! Queue count: " + proposerQueue.size());
    }

    /**
     * Queues up a player for a party. The player is added into a queue respective to its PlayerType
     *
     * @param player Either a proposer or guesser depending on the PlayerType
     * @author Alan Rostem
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
        DummyPlayer[] players = new DummyPlayer[11];

        for (int i = 0; i < players.length; i++) {
            DummyPlayer player;
            if (i % 2 == 0) {
                player = new DummyPlayer("username_" + i, GUESSER);
            } else {
                player = new DummyPlayer("username_" + i, PROPOSER);
            }
            players[i] = player;
        }

        AtomicInteger testIndex = new AtomicInteger();

        TickExecution playerQueueing = new TickExecution(2000L, () -> {
            if (testIndex.get() < players.length)
                partyManager.queueUpPlayer(players[testIndex.getAndIncrement()]);
        });

        TickExecution partyUpdater = new TickExecution(100L, partyManager::update);

        playerQueueing.execute();
        partyUpdater.execute();
    }
}
