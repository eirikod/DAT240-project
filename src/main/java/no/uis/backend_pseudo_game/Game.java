package no.uis.backend_pseudo_game;

import no.uis.backend_pseudo_game.dummy.DummyPlayer;

import java.util.Scanner;

import static no.uis.backend_pseudo_game.dummy.DummyPlayer.PlayerType;

public class Game {

    public static void main(String[] args) {
        DummyPlayer guesser = new DummyPlayer("hello", PlayerType.GUESSER);
        DummyPlayer proposer = new DummyPlayer("hello", PlayerType.PROPOSER);
        System.out.println(guesser.getId());

        // Scanner scanner = new Scanner(System.in);
        // String input = scanner.next();
    }
}
