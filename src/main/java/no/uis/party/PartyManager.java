package no.uis.party;

import no.uis.players.Player;
import no.uis.websocket.SocketMessage;
import org.springframework.messaging.simp.SimpMessageSendingOperations;

import static no.uis.players.Player.PlayerType.*;
import static no.uis.party.Party.PartyStatus.*;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;

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
    private void openParty() {
        currentOpenParty = new Party();
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
    public void update(SimpMessageSendingOperations messagingTemplate) {
        if (!isThereAnOpenParty()) {
            openParty();
            System.out.println("Party opened: " + currentOpenParty.getId() + ". Parties created: " + parties.size());
        }
        // If both types of player are waiting, add them to the newly created party
        if (currentlyWaitingGuesser != null && currentlyWaitingProposer != null) {
            currentOpenParty.setGuesser(currentlyWaitingGuesser);
            currentOpenParty.setProposer(currentlyWaitingProposer);

            SocketMessage proposerMsg = new SocketMessage();
            proposerMsg.setSender("server");

            HashMap<String, Object> proposerContent = new HashMap<>();
            proposerContent.put("role", "PROPOSER");
            proposerContent.put("partyId", "" + currentOpenParty.getId());
            proposerContent.put("otherPlayerName", currentlyWaitingGuesser.getUsername());
            proposerMsg.setContent(proposerContent);

            proposerMsg.setType("JOIN_PARTY");
            currentlyWaitingProposer.sendData(proposerMsg, messagingTemplate);

            SocketMessage guesserMsg = new SocketMessage();
            guesserMsg.setSender("" + currentOpenParty.getId());
            HashMap<String, Object> guesserContent = new HashMap<>();
            guesserContent.put("role", "GUESSER");
            guesserContent.put("partyId", "" + currentOpenParty.getId());
            guesserContent.put("otherPlayerName", currentlyWaitingProposer.getUsername());
            guesserMsg.setContent(guesserContent);
            guesserMsg.setType("JOIN_PARTY");
            currentlyWaitingGuesser.sendData(guesserMsg, messagingTemplate);

            currentlyWaitingGuesser = null; // Guesser no longer waiting
            currentlyWaitingProposer = null; // Proposer no longer waiting
            currentOpenParty.setStatus(READY_TO_PLAY);
            parties.add(currentOpenParty);
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
        // Update all parties and remove those that are finished
        for (Party party : parties) {
            party.update(messagingTemplate);
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
     * Get the number of open/playing parties
     *
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
     * @see no.uis.players.Player.PlayerType
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
}
