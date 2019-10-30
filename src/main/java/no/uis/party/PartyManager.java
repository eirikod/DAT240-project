package no.uis.party;

import no.uis.backend_pseudo_game.tools.TickExecution;
import no.uis.players.Player;
import static no.uis.players.Player.PlayerType.*;


import static no.uis.party.Party.PartyStatus.*;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Singleton class that manages what party a player goes into depending on the role they select
 *
 * @author Alan Rostem
 */
public class PartyManager {
    private ArrayDeque<Player> proposerQueue = new ArrayDeque<>();
    private ArrayDeque<Player> guesserQueue = new ArrayDeque<>();
    private ArrayList<Party> parties = new ArrayList<>();

    private Party currentOpenParty;
    private Player currentlyWaitingProposer;
    private Player currentlyWaitingGuesser;

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
    public boolean isPlayerWaitingForParty(Player.PlayerType type) {
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
     * Returns true if an open party exists
     *
     * @return boolean
     * @author Alan Rostem
     */
    public boolean isThereAnOpenParty() {
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

        // Update all parties and remove those that are finished
        for (Party party : parties) {
            party.update();
            if (party.getStatus() == FINISHED_GAME) {
                parties.remove(party);
            }
        }
    }

    /**
     * Get the guesser waiting for a game
     *
     * @return Player
     * @author Alan Rostem
     */
    public Player getCurrentlyWaitingGuesser() {
        return currentlyWaitingGuesser;
    }

    /**
     * Get the proposer waiting for a game
     *
     * @return Player
     * @author Alan Rostem
     */
    public Player getCurrentlyWaitingProposer() {
        return currentlyWaitingProposer;
    }


    /**
     * Check if both respective player queues are empty
     *
     * @return boolean
     * @author Alan Rostem
     */
    public boolean areBothQueuesEmpty() {
        return proposerQueue.size() == 0 && guesserQueue.size() == 0;
    }

    /**
     * Get the number of open/playing parties
     * @return int
     */
    public int getPartyCount() {
        return parties.size();
    }

    public boolean isQueueNotEmpty(Player.PlayerType type) {
        switch (type) {
            case PROPOSER:
                return proposerQueue.size() > 0;
            case GUESSER:
                return guesserQueue.size() > 0;
            default:
                return false;
        }
    }

    /**
     * Adds a player to the guesser queue if given PlayerType is GUESSER
     *
     * @param guesser Player with PlayerType GUESSER
     * @author Alan Rostem
     */
    private void queueUpGuesser(Player guesser) {
        guesserQueue.add(guesser);
        System.out.println("We queued a guesser named " + guesser.getUsername() + "! Queue count: " + guesserQueue.size());
    }

    /**
     * Adds a player to the proposer queue if given PlayerType is PROPOSER
     *
     * @param proposer Player with PlayerType GUESSER
     * @author Alan Rostem
     */
    private void queueUpProposer(Player proposer) {
        proposerQueue.add(proposer);
        System.out.println("We queued a proposer named " + proposer.getUsername() + "! Queue count: " + proposerQueue.size());
    }

    /**
     * Queues up a player for a party. The player is added into a queue respective to its PlayerType
     *
     * @param player Either a proposer or guesser depending on the PlayerType
     * @author Alan Rostem
     * @see no.uis.backend_pseudo_game.dummy.Player.PlayerType
     */
    public void queueUpPlayer(Player player) {
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
        Player[] players = new Player[11];

        for (int i = 0; i < players.length; i++) {
            Player player;
            if (i % 2 == 0) {
                player = new Player("username_" + i, GUESSER);
            } else {
                player = new Player("username_" + i, PROPOSER);
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
