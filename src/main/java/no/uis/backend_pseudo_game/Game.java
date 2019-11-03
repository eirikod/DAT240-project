package no.uis.backend_pseudo_game;

import no.uis.backend_pseudo_game.dummy.DummyImageLabelReader;
import no.uis.backend_pseudo_game.dummy.DummyPlayer;
import no.uis.backend_pseudo_game.dummy.DummyPlayer.PlayerType;

import java.util.*;

/**
 * defining the basic game logic
 * @author Eirik & Markus
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
     * Constructor, initialize with 2 players and transfer all filename to proposer
     * @author Eirik & Markus
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
     * @author Eirik & Markus
     * @return selected image if exists or default image
     */
    public String selectImage() {
    	System.out.println(labelReader.getAllLabels());
    	System.out.println("Choose picture\n");
    	String selectedLabel = sc.nextLine();
    	return (selectedLabel == null || labelReader.getImageFiles(selectedLabel) == null ? "cinema" : selectedLabel);
    }
    
	/**
	 * starts the game, continues until correct answer or lost
	 * @author Eirik & Markus
	 */
    public void play () {
    	while (!isCorrect && !lost) {
    		nextRound();
    	}
    	getScore();
  	 }
    
   /**
    * @author Eirik & Markus
    * @return false if out of guesses/gives up, true if right answer
    * @see play()
    */
	public boolean nextRound() {
		chooseSegment();
		int guesses = 3;
		while (guesses > 0 && !isCorrect) {
			--guesses;
			if (giveUp()) {
				break;
			}
			checkAnswer();
		}
		return isCorrect;
	}
	
	/** 
	 * checks answer
	 * @author Eirik & Markus
	 */
	public void checkAnswer() {
		System.out.println("Type in your guess"); 
		if (sc.nextLine().equals(this.image)) {
			isCorrect = true;
		}
	}
	
	/**
	 * @author Eirik & Markus
	 * @return true if player gives up round
	 */
	public boolean giveUp() {
		System.out.println("Give up? y/n");
		return (sc.nextLine().equals("y") ? true : false);
	}
	
	/**
	 * chooses new segment for new round
	 * @author Eirik & Markus
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
	 * Writes out score for the paired players
	 * @author Eirik & Markus
	 */
	public void getScore() {
		int score = lost ? 0 : (proposerSegments.size() - guesserSegments.size())*2;
		System.out.println("Your score is " + score + "/100");
	}
	
	/**
	 * Tests the game logic
	 * @author Eirik & Markus
	 * @param args
	 */
	 public static void main(String[] args) {
	    	DummyPlayer guesser = new DummyPlayer("halla", PlayerType.GUESSER);
	        DummyPlayer proposer = new DummyPlayer("heisann", PlayerType.PROPOSER);
	    	Game game = new Game(guesser, proposer);
	    	game.play();
	 }
}
