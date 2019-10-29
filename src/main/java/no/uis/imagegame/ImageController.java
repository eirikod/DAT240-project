package no.uis.imagegame;

import java.io.*;
import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import no.uis.players.Player;
import no.uis.players.PlayerRepository;
import no.uis.players.User;
import no.uis.players.Player.PlayerType;


@Controller
public class ImageController {
	
	//Static parameters
	final static int HIGHER_SCORE = 100;
	final static String CONST_PLAY_MODE = "listPlayMode";
	final static String CONST_PLAYER_MODE = "listPlayerMode";
	
	
	//Load list of images in my scattered_images folder
	@Value("classpath:/static/images/scattered_images/*")
	private Resource[] resources;
	
	//Initialize my label reader
	ImageLabelReader labelReader = new ImageLabelReader("src/main/resources/static/label/label_mapping.csv",
			"src/main/resources/static/label/image_mapping.csv");
	
	private int score;
	private int guesses;
	
    private Player proposer;
    private Player guesser;
    private String image;
    private ArrayList<String> proposerImage;
    private ArrayList<String> guesserImage;
    private ArrayList<String> chosenSegments;
    private int countTotalSegments;
  
    
    /**
     * Returns player to correct view
     * @param model
     * @param player
     * @return players view
     */
    @RequestMapping("/game")
    public String Game(Model model,
    		@RequestParam(value = "player", required = true) Player player) {
    	return (player.getPlayerType() == PlayerType.GUESSER ? "guesser" : "proposerImageSelection");
    }
    
  /**
   * Proposer init, loads picture and player stats
   * @param model
   * @param name
   * @param id
   * @return view
   */

	@RequestMapping("/proposer")
	public String showImage(Model model,
			@RequestParam(value = "selectedlabel", required = false, defaultValue = "cinema") String name, //name of the image
			@RequestParam(value = "id", required = false, defaultValue = "-1") String id) //image chose by the proposer 
	{
		String[] files = labelReader.getImageFiles(name);
		String image_folder_name = getImageFolder(files);
		ArrayList<String> imageLabels = getAllLabels(labelReader);
		model.addAttribute("selectedlabel", name);
		model.addAttribute("listlabels", imageLabels);
		model.addAttribute("highestscore", HIGHER_SCORE);
		
		proposerImage = new ArrayList<String>();
		guesserImage = new ArrayList<String>();
		countTotalSegments = new File("src/main/resources/static/images/scattered_images/" + image_folder_name).list().length;
		
		for (int i = 0; i < countTotalSegments-1; i++) {
			proposerImage.add("images/scattered_images/" + image_folder_name + "/" + i + ".png");
		}
		model.addAttribute("listimages", proposerImage);
		return "proposer"; // view
	}
	
	/**
	 * Changes opacity of the proposer images that are chosen
	 * @return chosen segments by ID
	 */
	/*public String chosenSegments() {
		/TODO
	}
	*/
	
    
	/**
	 * Adds the chosen segment to the guessers view
	 * @param id
	 * @param name
	 * @return redirect
	 */
    @RequestMapping(value = "/proposer", method = RequestMethod.POST)
    public String newSegment(Model model,
    		@RequestParam (value="selectedLabel", required=false, defaultValue="cinema") String name,
    		@RequestParam (value="id", required=false, defaultValue="-1") String id) {
    	if (id != "-1") {
    		String[] files = labelReader.getImageFiles(name);
    		String image_folder_name = getImageFolder(files);
        	int segmentID = Integer.parseInt(id);
    		System.out.println(id);
    		guesserImage.add(segmentID, "images/scattered_images/" + image_folder_name + "/" + id  + ".png");
    		return "/redirect";
        } else {
        	return "/proposer";
        }
    }
  
 
	// if model attribute of image is here --> set up arraylist of segments, if not wait and check later
	//Guesser controller example
    
    /**
     * Guesser init, loads available segments
     * @param model
     * @param name
     * @param id
     * @return view
     */
	@GetMapping("/guesser")
	public String showImageGuesser(Model model,
			@RequestParam (value="selectedLabel", required=false, defaultValue="cinema") String name,
			@RequestParam (value="id", required=false, defaultValue="-1") String id) {
		guesserImage = new ArrayList<String>();
		String[] files = labelReader.getImageFiles(name);
		String image_folder_name = getImageFolder(files);
		for (int i = 0; i < countTotalSegments-1; i++) {
			if (guesserImage.get(i) == null & chosenSegments.get(i) != null) {
				guesserImage.add(i, "images/scattered_images/" + image_folder_name + "/" + i + ".png");
			}
		}
		model.addAttribute("listimagesproposed", guesserImage);
		return "guesser";//view
	}
	
	
	//Proposer Controller (example of a first part)
	@GetMapping("/proposerImageSelection")
	public String showLabels(Model model) {
		ArrayList<String> imageLabels = getAllLabels(labelReader);
		model.addAttribute("listlabels", imageLabels);
		return "proposer"; // view
	}

	//private method taking back the image folder
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

	//private methode returning all of the images label
	private ArrayList<String> getAllLabels(ImageLabelReader ilr) {
		ArrayList<String> labels = new ArrayList<String>();
		for (Resource r : resources) {
			String fileName = r.getFilename();
			String fileNameCorrected = fileName.substring(0, fileName.lastIndexOf('_'));
			String label = ilr.getLabel(fileNameCorrected);
			labels.add(label);
		}
		return labels;
	}

	//WelcomePage controller example
	@RequestMapping("/welcomePage")
	public String newEntry(Model model, User user,
			@RequestParam(value = "selectedPlayModelabel", required = false, defaultValue = "") String playMode,
			@RequestParam(value = "selectedPlayerModelabel", required = false, defaultValue = "") String playerMode){
		System.out.println("playMode : " + playMode);
		System.out.println("playerMode : " + playerMode);
		model.addAttribute("obj", user);
		
		ArrayList<String> lstPlayMode = new ArrayList<String>();
		lstPlayMode.add("GUESSER");
		lstPlayMode.add("PROPOSER");
		model.addAttribute(CONST_PLAY_MODE, lstPlayMode);
		
		ArrayList<String> lstPlayerMode = new ArrayList<String>();
		lstPlayerMode.add("SINGLE PLAYER");
		lstPlayerMode.add("MULTIPLE PLAYER");
		model.addAttribute(CONST_PLAYER_MODE, lstPlayerMode);
		
		System.out.println(user.getName());
		return "welcomePage";
	}

	@RequestMapping("/addPlayer")
	public String addPlayer(Model model, User user) {
		model.addAttribute("obj", user);
		System.out.println(user.getName());
		return "welcomePage";
	}
	
}
