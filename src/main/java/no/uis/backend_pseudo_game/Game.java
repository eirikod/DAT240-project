package no.uis.backend_pseudo_game;

import no.uis.backend_pseudo_game.dummy.DummyImageLabelReader;
import no.uis.backend_pseudo_game.dummy.DummyPlayer;
import no.uis.backend_pseudo_game.dummy.DummyPlayer.PlayerType;

import java.util.*;
import java.io.*;

/**
 * 
 * @author Eirik & Markus
 *
 */

public class Game {

	DummyImageLabelReader labelReader = new DummyImageLabelReader("src/main/resources/static/label/label_mapping.csv",
			"src/main/resources/static/label/image_mapping.csv");
	
	Scanner sc = new Scanner(System.in);

    private DummyPlayer proposer;
    private DummyPlayer guesser;
    private String image;
    private ArrayList<String> proposerSegments;
    private ArrayList<String> guesserSegments;
    
    private boolean isCorrect = false;
    private boolean lost = false;
    
    /**
     * constructor for the game, init game by selecting photo and transferring all lable filenames to proposerSegments
     * @param dp1
     * @param dp2
     */
    public Game (DummyPlayer dp1, DummyPlayer dp2) {
    	this.proposer = dp1;
    	this.guesser = dp2;
    	this.image = selectImage();
    	
		proposerSegments = new ArrayList<String>();
		guesserSegments = new ArrayList<String>();
		for (int i = 0; i < 49; ++i) {
			proposerSegments.add(Integer.toString(i));
		}
    }
    
    /**
     * @return selected image or default image "cinema"
     */
    public String selectImage() {
    	System.out.println(labelReader.getAllLabels());
    	System.out.println("Choose picture\n");
    	String selectedLabel = sc.nextLine();
    	return (selectedLabel == null || labelReader.getImageFiles(selectedLabel) == null ? "cinema" : selectedLabel);
    }
    
	/**
	 * starts the game
	 */
    public void play () {
    	while (!isCorrect | !lost) {
    		nextRound();
    	}
    	getScore();
  	 }
    
   /**
    * @return false if player is out of guesses or gives up
    * @return true if player guesses right
    * @see play()
    */
	public boolean nextRound() {
		chooseSegment();
		int guesses = 3;
		while (guesses > 0 & !isCorrect) {
			--guesses;
			if (giveUp()) {
				break;
			}
			checkAnswer();
		}
		return isCorrect;
	}
	
	/** 
	 * @return true if answer is correct
	 * @see nextRound
	 */
	public void checkAnswer() {
		System.out.println("Type in your guess"); 
		if (sc.nextLine().equals(this.image)) {
			isCorrect = true;
		}
	}
	
	/**
	 * @return true/false if player gives up this round
	 * @see nextRound
	 */
	public boolean giveUp() {
		System.out.println("Give up? y/n");
		return (sc.nextLine().equals("y") ? true : false);
	}
	
	/**
	 * proposer chooses segment to show for guesser
	 * @see nextRound
	 */
	public void chooseSegment() {
		System.out.println("Choose segment by id");
		String segmentID = sc.nextLine();
		if (guesserSegments.contains(segmentID)) {
			System.out.println("Already picked that segment, try again");
			if (guesserSegments.size() == proposerSegments.size()) {
				lost = true;
				System.out.println("You're all out of segments and lost the game");
			} else {
				chooseSegment();
			}
		} else {
			guesserSegments.add(segmentID);
			System.out.println(segmentID + " added to the guesser segments");
		}
	}
	
	/**
	 * write out score
	 */
	public void getScore() {
		int score = lost ? 0 : (proposerSegments.size() - guesserSegments.size())*2;
		System.out.println("Your score is " + score + "/100");
	}
	
	/**
	 * test the game logic
	 * @param args
	 */
	 public static void main(String[] args) {
	    	DummyPlayer guesser = new DummyPlayer("halla", PlayerType.GUESSER);
	        DummyPlayer proposer = new DummyPlayer("heisann", PlayerType.PROPOSER);
	    	Game game = new Game(guesser, proposer);
	    	game.play();
	 }
}
