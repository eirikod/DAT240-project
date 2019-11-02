package no.uis.imagegame;

import java.io.*;
import java.util.*;

import static java.lang.String.format;
import no.uis.websocket.SocketMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;

import org.springframework.messaging.core.MessageSendingOperations;
import org.springframework.messaging.handler.annotation.Header;

import org.springframework.messaging.handler.annotation.DestinationVariable;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessageSendingOperations;

import org.springframework.web.util.HtmlUtils;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import no.uis.players.Player;
import no.uis.players.User;
import no.uis.websocket.SocketMessage;
import no.uis.players.Player.PlayerType;


@Controller
public class ImageController {
	
	//Static parameters
	final static int HIGHER_SCORE = 100;

	final static String CONST_PLAY_MODE = "listPlayMode";
	final static String CONST_PLAYER_MODE = "listPlayerMode";
	final static String ADDR_CALLBACK_IMAGE = "/party/20/sendImageId";
	final static String ADDR_FRONT_IMAGE_CALLBACK = "/channel/update/54";

	final static String USER_ID = "54";
	final static String PARTY_ID = "20";


	//Load list of images in my scattered_images folder
	@Value("classpath:/static/images/scattered_images/*")
	private Resource[] resources;
	
	//Initialize my label reader
	ImageLabelReader labelReader = new ImageLabelReader("src/main/resources/static/label/label_mapping.csv",
			"src/main/resources/static/label/image_mapping.csv");
	
	@Autowired
	private SimpMessageSendingOperations messageTemplate;
	
	private int score;
	private int guesses;
	
    private Player proposer;
    private Player guesser;
    private String image;
    private String[] proposerImage;
    private String[] guesserImage;
    private String[] chosenSegments = new String[50];
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

	@MessageMapping("/party/{partyId}/addUser")
	public void addUser(@DestinationVariable String roomId, @Payload SocketMessage chatMessage,
						SimpMessageHeaderAccessor headerAccessor) {
		String currentRoomId = (String) headerAccessor.getSessionAttributes().put("room_id", roomId);
		System.out.println(roomId);
		if (currentRoomId != null) {
			SocketMessage leaveMessage = new SocketMessage();
			leaveMessage.setType("LEAVE");
			leaveMessage.setSender(chatMessage.getSender());
			messageTemplate.convertAndSend(format("/channel/%s", currentRoomId), leaveMessage);
		}
		headerAccessor.getSessionAttributes().put("username", chatMessage.getSender());
		messageTemplate.convertAndSend(format("/channel/%s", roomId), chatMessage);
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
			@RequestParam(value = "selectedlabel", required = false, defaultValue="cinema") String name, //name of the image
			@RequestParam(value = "id", required = false, defaultValue = "-1") String id) //image chose by the proposer 
	{
		String[] files = labelReader.getImageFiles(name);
		System.out.println(id);
		String image_folder_name = getImageFolder(files);
		ArrayList<String> imageLabels = getAllLabels(labelReader);
		model.addAttribute("selectedlabel", name);
		model.addAttribute("listlabels", imageLabels);
		model.addAttribute("highestscore", HIGHER_SCORE);
		model.addAttribute("selectedLabel", name);
		model.addAttribute("userId", USER_ID);
		model.addAttribute("partyId", PARTY_ID);
		
		countTotalSegments = new File("src/main/resources/static/images/scattered_images/" + image_folder_name).list().length;
		proposerImage = new String [countTotalSegments];
		guesserImage = new String [countTotalSegments];
		
		for (int i = 0; i < countTotalSegments-1; i++) {
			proposerImage[i] = "images/scattered_images/" + image_folder_name + "/" + i + ".png";
		}
		model.addAttribute("listimages", proposerImage);
		
		return "proposer";
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
    	System.out.println(name);
    	if (id != "-1") {
    		String[] files = labelReader.getImageFiles(name);
    		String image_folder_name = getImageFolder(files);
        	int segmentID = Integer.parseInt(id);
    		System.out.println(id);
    		guesserImage[segmentID] = "images/scattered_images/" + image_folder_name + "/" + id  + ".png";
        } 
    	return String.format("redirect:proposer?selectedLabel=%s",name);  

    }
    
    private int count=0;
    
	@MessageMapping(ADDR_CALLBACK_IMAGE)
	public void update(){
		System.out.println("update ---------------------------------------------------");
		SocketMessage sockMess = new SocketMessage();
		count++;
		if (count > 3) {
			sockMess.setContent((Player.PlayerStatus.FINISHED).toString());
		}
		else {
			sockMess.setContent((Player.PlayerStatus.PLAYING).toString());
		}
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		messageTemplate.convertAndSend(ADDR_FRONT_IMAGE_CALLBACK, sockMess);
		return;
  }
    
    /**
     * Guesser init, loads available segments
     * @param model
     * @param name
     * @param id
     * @return view
     */
	@RequestMapping("/guesser")
	public String showImageGuesser(Model model,
			@RequestParam (value="selectedLabel", required=false, defaultValue="cinema") String name,
			@RequestParam (value="id", required=false, defaultValue="-1") String id) {
		String[] files = labelReader.getImageFiles(name);
		String image_folder_name = getImageFolder(files);
		for (int i = 0; i < countTotalSegments-1; i++) {
			if (guesserImage[i] == null & chosenSegments[i] != null) {
				guesserImage[i] = "images/scattered_images/" + image_folder_name + "/" + i + ".png";
			}
		}
		model.addAttribute("listimagesproposed", guesserImage);
		return "guesser";//view
	}
	
	
	//Proposer Controller (example of a first part)
	@RequestMapping("/proposerImageSelection")
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

}
