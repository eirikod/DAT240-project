package no.uis.imagegame;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
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
	
	final static int HIGHER_SCORE = 100;
	final static String CONST_PLAY_MODE = "listPlayMode";
	final static String CONST_PLAYER_MODE = "listPlayerMode";
	
	
	//Load list of images in my scattered_images folder
	@Value("classpath:/static/images/scattered_images/*")
	private Resource[] resources;
	
	//Initialize my label reader
	ImageLabelReader labelReader = new ImageLabelReader("src/main/resources/static/label/label_mapping.csv",
			"src/main/resources/static/label/image_mapping.csv");

	
	@RequestMapping("/proposer")
	public String showImage(Model model,
			@RequestParam(value = "selectedlabel", required = false, defaultValue = "cinema") String name, //name of the image
			@RequestParam(value = "id", required = false, defaultValue = "-1") String id) //image chosed by the proposer 
	{
		String[] files = labelReader.getImageFiles(name);
		System.out.println(id);
		String image_folder_name = getImageFolder(files);
		ArrayList<String> imageLabels = getAllLabels(labelReader);
		model.addAttribute("selectedlabel", name);
		model.addAttribute("listlabels", imageLabels);
		model.addAttribute("highestscore", HIGHER_SCORE);
		ArrayList<String> images = new ArrayList<String>();
		for (int i = 0; i < 49; ++i) {
			images.add("images/scattered_images/" + image_folder_name + "/" + i + ".png");
		}
		model.addAttribute("listimages", images);
		return "proposer"; // view
	}
	
	@RequestMapping("/guesser")
	public String showImage(Model model,
			@RequestParam(value = "submettedGuess", required = false, defaultValue = "") String guess) {
		System.out.println(guess);
		ArrayList<String> images = new ArrayList<String>();
		String[] files = labelReader.getImageFiles("cinema");
		String image_folder_name = getImageFolder(files);
		images.add("images/scattered_images/" + image_folder_name + "/" + 25 + ".png");
		images.add("images/scattered_images/" + image_folder_name + "/" + 24 + ".png");
		images.add("images/scattered_images/" + image_folder_name + "/" + 23 + ".png");
		model.addAttribute("listimagesproposed", images);
		return "guesser";//view
	}
	
//	@GetMapping("/showImage")
//	public String showImage(Model model,
//			@RequestParam(value = "selectedlabel", required = false, defaultValue = "cinema") String name) {
//		String[] files = labelReader.getImageFiles(name);
//		String image_folder_name = getImageFolder(files);
//		ArrayList<String> imageLabels = getAllLabels(labelReader);
//		model.addAttribute("listlabels", imageLabels);
//		ArrayList<String> images = new ArrayList<String>();
//		for (int i = 0; i < 49; ++i) {
//			images.add("images/scattered_images/" + image_folder_name + "/" + i + ".png");
//		}
//		model.addAttribute("listimages", images);
//		return "welcome"; // view
//	}
	
	@GetMapping("/game")
	public String game(Model model, @RequestParam(value = "id", required = true, defaultValue = "-1") String name) {
		System.out.println(name);
		return "proposer";//View
	}

	@GetMapping("/proposerImageSelection")
	public String showLabels(Model model) {
		ArrayList<String> imageLabels = getAllLabels(labelReader);
		model.addAttribute("listlabels", imageLabels);
		return "proposer"; // view
	}

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
	
	@RequestMapping("/welcomePage/playerRole_playMode")
	public String gameParamater(
			@RequestParam(value = "selectedPlayModelabel", required = false, defaultValue = "") String playMode,
			@RequestParam(value = "selectedPlayerModelabel", required = false, defaultValue = "") String playerMode){
		System.out.println("playMode : " + playMode);
		System.out.println("playerMode : " + playerMode);
		return "welcomePage";
	}
	
	@RequestMapping("/addPlayer")
	public String addPlayer(Model model, User user) {
		model.addAttribute("obj", user);
		System.out.println(user.getName());
		return "welcomePage";
	}
	
}
