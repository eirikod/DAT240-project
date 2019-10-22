package no.uis.backend_pseudo_game;

import no.uis.backend_pseudo_game.dummy.DummyImageLabelReader;
import no.uis.backend_pseudo_game.dummy.DummyPlayer;
import no.uis.backend_pseudo_game.dummy.DummyPlayer.PlayerType;

import java.util.*;
import java.io.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;

public class Game {
	
	//Load list of images in my scattered_images folder
	@Value("classpath:/static/images/scattered_images/*")
	private Resource[] resources;
	
	DummyImageLabelReader labelReader = new DummyImageLabelReader("src/main/resources/static/label/label_mapping.csv",
			"src/main/resources/static/label/image_mapping.csv");
	Scanner sc = new Scanner(System.in);

    // replace with party
    private DummyPlayer proposer;
    private DummyPlayer guesser;
    private String image;
    private ArrayList<String> proposerSegments;
    private ArrayList<String> guesserSegments;
    
    private int score;
    private int guesses;
    
    /**
     * constructor for the game, init game by selecting photo and transferring all lable filenames to proposerSegments
     * @param dp1
     * @param dp2
     */
    public Game (DummyPlayer dp1, DummyPlayer dp2) {
    	this.proposer = dp1;
    	this.guesser = dp2;
    	this.image = selectImage();
    	score = 100;
    	
    	String[] files = labelReader.getImageFiles(image);
		String image_folder_name = getImageFolder(files);
		ArrayList<String> imageLabels = getAllLabels(labelReader);
		proposerSegments = new ArrayList<String>();
		for (int i = 0; i < imageLabels.size(); ++i) {
			proposerSegments.add("images/scattered_images/" + image_folder_name + "/" + i + ".png");
		}
    }
    
    /**
     * @return selected image or default image "cinema"
     */
    public String selectImage() {
    	System.out.println(labelReader.getAllLabels());
    	System.out.println("Choose picture\n");
    	String selectedLabel = sc.nextLine();
    	System.out.println(selectedLabel);
    	String choice = selectedLabel == null || labelReader.getImageFiles(selectedLabel) == null ? "cinema" : selectedLabel;
    	return choice;
    }
    
    /**
     * @param files
     * @return folder name of chosen image
     */
	private String getImageFolder(String[] files) {
		String image_folder_name = "";
		for (String file : files) {
			String folder_name = file + "_scattered";
			for (Resource r : resources) {
				if (folder_name.equals(r.getFilename())) {
					image_folder_name = folder_name;
					break;
				}
			}
		}
		return image_folder_name;
	}
	
	/**
	 * @param ilr (labelReader)
	 * @return filename for all labels of chosen image
	 */
	private ArrayList<String> getAllLabels(DummyImageLabelReader ilr) {
		ArrayList<String> labels = new ArrayList<String>();
		for (Resource r : resources) {
			String fileName = r.getFilename();
			String fileNameCorrected = fileName.substring(0, fileName.lastIndexOf('_'));
			String label = ilr.getLabel(fileNameCorrected);
			labels.add(label);
		}
		return labels;
	}
    
	/**
	 * starts the game
	 */
    public void play () {
    	while (!nextRound()) {
    		nextRound();
    		score--;
    	}
    	getScore();
  	 }
    
   /**
    * @return false if player is out of guesses or gives up
    * @return true if player guessed right
    * @see play()
    */
	public boolean nextRound() {
		chooseSegment();
		guesses = 3;
		boolean giveUp = false;
		boolean isCorrect = false;
		while (guesses != 0 || !giveUp || !isCorrect) {
			isCorrect = checkAnswer();
			giveUp = giveUp();
			guesses--;
		}
		if (isCorrect) {
			System.out.println("YOU WIN");
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * proposer chooses segment to show for guesser
	 * @see nextRound
	 */
	public void chooseSegment() {
		System.out.println("Choose segment by id");
		String imageID = sc.nextLine();
		while (true) {
			if (guesserSegments.contains(imageID)) {
				System.out.println("Already picked that segment, try again");
			} else {
				guesserSegments.add(imageID);
				System.out.println("added to the guesser segments");
				break;
			}
		}
	}
	
	/** 
	 * @return true/false if answer is correct
	 * @see nextRound
	 */
	public boolean checkAnswer() {
		System.out.println("Type in your guess");
		String guessInput = sc.nextLine();
		boolean choice = guessInput == this.image ? true : false;
		return choice;
	}
	
	/**
	 * @return true/false if player gives up this round
	 * @see nextRound
	 */
	public boolean giveUp() {
		System.out.println("Give up? Y/N");
		String forfeit = sc.nextLine();
		boolean choice = forfeit == "Y" ? true : false;
		return choice;	
	}
	
	/**
	 * write out score
	 */
	public void getScore() {
		System.out.println("Your score is" + score + "/100");
	}
	
	/**
	 * @param args
	 */
    public static void main(String[] args) {
    	DummyPlayer guesser = new DummyPlayer("halla", PlayerType.GUESSER);
        DummyPlayer proposer = new DummyPlayer("heisann", PlayerType.PROPOSER);
    	Game game = new Game(guesser, proposer);
    	game.play();
    }
}