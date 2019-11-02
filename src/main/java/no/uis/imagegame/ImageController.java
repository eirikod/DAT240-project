package no.uis.imagegame;
import java.io.*;
import java.util.*;

import static java.lang.String.format;
import no.uis.websocket.SocketMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.HtmlUtils;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import no.uis.party.PartyManager;
import no.uis.party.Party;
import no.uis.party.QueueController;
import no.uis.players.Player;
import no.uis.players.User;
import no.uis.websocket.SocketMessage;
import no.uis.players.Player.PlayerType;
import no.uis.repositories.PlayerRepository;


@Controller
public class ImageController {

	//Static parameters
	final static int HIGHER_SCORE = 100;

	final static String CONST_PLAY_MODE = "listPlayMode";
	final static String CONST_PLAYER_MODE = "listPlayerMode";

	final static String ADDR_CALLBACK_IMAGE = "/party/20/sendGuess";
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

	@Autowired
    private PlayerRepository playerRepository;
	
	private int score;
	private int guessesLeft;

//    private Player proposer;
//    private Player guesser;
    private String image;
    private ArrayList<String> propSegment;
    private ArrayList<String> guesSegment;
    private ArrayList<String> chosenSegments;
    private int countTotalSegments;
    private boolean giveup;
//    display remaining segments in frontend?
    private int countRemainingSegments;

    /**
     * Returns player to correct view
     * @author Eirik
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
	public void addUser(@DestinationVariable String partyId, @Payload SocketMessage chatMessage,
						SimpMessageHeaderAccessor headerAccessor) {
		System.out.println("FUCKKKKKKKKKKKKKKKKKKKKKKKKK YEAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
		String currentRoomId = (String) headerAccessor.getSessionAttributes().put("room_id", partyId);
		System.out.println(partyId);
		if (currentRoomId != null) {
			SocketMessage leaveMessage = new SocketMessage();
			leaveMessage.setType("LEAVE");
			leaveMessage.setSender(chatMessage.getSender());
			messageTemplate.convertAndSend(format("/channel/%s", currentRoomId), leaveMessage);
		}
		headerAccessor.getSessionAttributes().put("username", chatMessage.getSender());
		messageTemplate.convertAndSend(format("/channel/%s", partyId), chatMessage);
	}

  /**
   * Proposer init, loads picture and player stats
   * @author Eirik
   * @param model
   * @param modelname
   * @param id
   * @return view
   */
	@RequestMapping("/proposer")
	public ModelAndView showImage(ModelAndView model,
			@ModelAttribute("selectedlabel") Object modelname,
			@RequestParam(value = "partyId", required = false, defaultValue = "-1") String partyId,
			@RequestParam(value = "userId", required = false, defaultValue = "-1") Long userId) {

			System.out.println("NO NOT HERRRRRRE----------------------------------------------");
		
			String name = modelname.toString() != null ? modelname.toString() : "cinema";

			String[] files = labelReader.getImageFiles(name);
			String image_folder_name = getImageFolder(files);
			
//			ArrayList<String> imageLabels = getAllLabels(labelReader);


			PartyManager partyManager = QueueController.getPartyManager();
			Party party = partyManager.getParty(partyId);
			Player proposer = party.getProposer();
			
			User player = playerRepository.findById(userId);

			//TODO
//				model.addObject("highestscore", player.getHigherScore());
			model.addObject("userId", userId);
			model.addObject("partyId", partyId);
//			model.addObject("listlabels", imageLabels);
			
			// finds number of segments per image
			countTotalSegments = new File("src/main/resources/static/images/scattered_images/" + image_folder_name).list().length;
			countTotalSegments = countTotalSegments-1;
			countRemainingSegments = countTotalSegments;
			guessesLeft = 3;

			propSegment = new ArrayList<String>();
			guesSegment = new ArrayList<String>();

			for (int i = 0; i < countTotalSegments; ++i) {
				propSegment.add("images/scattered_images/" + image_folder_name + "/" + i + ".png");
			}

			model.addObject("listimages", propSegment);
			return model;
	}

	@MessageMapping("/party/{partyId}/sendImageId")
	public void updateProposerView(@DestinationVariable String partyId,
									SocketMessage message){
		System.out.println("update ---------------------------------------------------");
		System.out.println(message);
		Object id = message.getContent();
		String str_id = (String) id;
		System.out.println(id);
		SocketMessage sockMess = new SocketMessage();
		HashMap content = new HashMap();
		String state = (Player.PlayerStatus.FINISHED).toString();
		String score ="8";
		String time = "12:23";
		
		PartyManager partyManager = QueueController.getPartyManager();
		Party party = partyManager.getParty(partyId);
		Player proposer = party.getProposer();
		String userId = Long.toString(proposer.getId());
		//String state = (proposer.getPlayerStatus()).toString();
		content.put("state", state);
		content.put("score", score);
		content.put("time", time);
		sockMess.setContent(content);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		proposer.sendData(sockMess, messageTemplate);
//		messageTemplate.convertAndSend("/party/"+partyId+"/sendImageId/%s".format(userId), sockMess);
		return;
  }
	@MessageMapping(ADDR_CALLBACK_IMAGE)
	public void update(SocketMessage message){
		System.out.println("update ---------------------------------------------------");
		System.out.println(message);
		Object id = message.getContent();
		String str_id = (String) id;
		System.out.println(id);
		SocketMessage sockMess = new SocketMessage();
		HashMap content = new HashMap();
		String state = (Player.PlayerStatus.FINISHED).toString();
		String score ="8";
		String time = "12:23";
		content.put("state", state);
		content.put("score", score);
		content.put("time", time);
		String segmentId = "14";
		content.put("segment", segmentId);
		sockMess.setContent(content);
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
	   * Proposer init, loads picture and player stats
	   * @author Eirik
	   * @param model
	   * @param modelname
	   * @param id
	   * @return view
	   */
		@RequestMapping("/toto")
		public ModelAndView imageGame(
//				ModelAndView model,
//				@ModelAttribute("selectedlabel") Object modelname,
				@RequestParam(value = "id", required = false, defaultValue = "-1") String id,
				@RequestParam(value = "partyId", required = false, defaultValue = "-1") String partyId) {

				ModelAndView model = new ModelAndView("proposer");
				
				System.out.println("imageGame");
				
				PartyManager partyManager = QueueController.getPartyManager();
				Party party = partyManager.getParty(partyId);
				Player proposer = party.getProposer();
				String userId = Long.toString(proposer.getId());
				
				String name = "cinema";

				String[] files = labelReader.getImageFiles(name);
				String image_folder_name = getImageFolder(files);
				ArrayList<String> imageLabels = getAllLabels(labelReader);
				
				System.out.println("party id : " + partyId + "/ user id : " + userId);

				model.addObject("highestscore", HIGHER_SCORE);
				model.addObject("userId", userId);
				model.addObject("partyId", partyId);
				model.addObject("listlabels", imageLabels);
				
				// finds number of segments per image
				countTotalSegments = new File("src/main/resources/static/images/scattered_images/" + image_folder_name).list().length;
				countTotalSegments = countTotalSegments-1;
				countRemainingSegments = countTotalSegments;
				guessesLeft = 3;

				return model;
		}
	
	/**
	 *  Lets user choose a picture
	 * @author Eirik
	 * @return model
	 */
	@RequestMapping("/proposerImageSelection")
	public ModelAndView showLabels(
			@RequestParam(value = "partyId", required = false, defaultValue = "-1") String partyId) {
		ModelAndView model = new ModelAndView("proposerImageSelection");
		ArrayList<String> imageLabels = getAllLabels(labelReader);
		PartyManager partyManager = QueueController.getPartyManager();
		Party party = partyManager.getParty(partyId);
		Player proposer = party.getProposer();
		String userId = Long.toString(proposer.getId());
		model.addObject("listlabels", imageLabels);
		model.addObject("userId", userId);
		model.addObject("partyId", partyId);
		return model;
	}


	/**
	 * Adds the chosen segment to the guessers view
	 * @author Eirik
	 * @param id
	 * @param name
	 * @return redirect
	 */
	
    @RequestMapping(value = "/proposer", method = RequestMethod.POST)
    public ModelAndView newSegment(ModelAndView model, 
    		@ModelAttribute ("selectedlabel") String name,
    		@RequestParam (value="id", required=false, defaultValue="-1") String id) {
    	if (!id.equals("-1") && (guessesLeft == 0 || giveup)) {
    		String[] files = labelReader.getImageFiles(name);
    		String image_folder_name = getImageFolder(files);
    		guesSegment.add("images/scattered_images/" + image_folder_name + "/" + id  + ".png");
    		model.addObject("listimagesproposed", guesSegment);

    		guessesLeft = 3;
    		giveup = false;
    		model.addObject("infotext", "NEW SEGMENT ADDED");
    		model.addObject("proposerinfo", "added new segment");
        } else {
        	model.addObject("proposerinfo", "wait your turn");
        }
    	model.addObject("listimages", propSegment);
		model.addObject("listimagesproposed", guesSegment);
    	return model;
    }

    private int count=0;
    

//    /**
//     * Guesser init, loads available segments
//     * @author Eirik
//     * @param model
//     * @param name
//     * @param id
//     * @return model
//     */
//	@RequestMapping(value = "/guesser")
//	public ModelAndView showImageGuesser(ModelAndView model,
//			@ModelAttribute ("selectedlabel") String name,
//			@RequestParam (value="SubmittedGuess", required=false, defaultValue="-1") String guess,
//			@RequestParam(value = "partyId", required = false, defaultValue = "-1") String partyId) {
//		model.addObject("listimagesproposed", guesSegment);
//		model.addObject("infotext", "WAIT FOR SEGMENT");
//		
//		return model;
//	}

    /**
     * Guesser init, loads available segments
     * @author Eirik
     * @param model
     * @param name
     * @param id
     * @return model
     */
	@RequestMapping(value = "/guesser")
	public ModelAndView showImageGuesser(
			@ModelAttribute("selectedlabel") Object modelname,
			@RequestParam(value = "partyId", required = false, defaultValue = "-1") String partyId) {
		
		ModelAndView model = new ModelAndView("guesser");
		
		PartyManager partyManager = QueueController.getPartyManager();
//		Party party = partyManager.getParty(partyId);
//		Player guesser = party.getGuesser();
//		String userId = Long.toString(guesser.getId());
		model.addObject("userId", USER_ID);
		model.addObject("partyId", PARTY_ID);
		
		String name = modelname.toString() != null ? modelname.toString() : "cinema";
		String[] files = labelReader.getImageFiles(name);
		String image_folder_name = getImageFolder(files);
		// finds number of segments per image
		countTotalSegments = new File("src/main/resources/static/images/scattered_images/" + image_folder_name).list().length;
		countTotalSegments = countTotalSegments-1;
		countRemainingSegments = countTotalSegments;
		guessesLeft = 3;
		propSegment = new ArrayList<String>();
		for (int i = 0; i < countTotalSegments; ++i) {
			propSegment.add("images/scattered_images/" + image_folder_name + "/" + i + ".png");
		}
		model.addObject("listimages", propSegment);
		
		return model;
	}
    
	/**
	 * Guesser view, processes guesses and if ready for new segment
	 * @author Eirik
	 * @param model
	 * @param name
	 * @param guess
	 * @return
	 */
	@RequestMapping(value = "/guesser", method = RequestMethod.POST)
	public ModelAndView newGuess (ModelAndView model,
			@ModelAttribute ("selectedlabel") String name,
			@RequestParam (value = "SubmittedGuess", required = false, defaultValue = "-1") String guess,
			// implement boolean button
			@RequestParam (value = "nextround", defaultValue = "false") boolean nextround) {
		if (!guess.equals("-1") && guessesLeft > 0) {
			model.addObject("guessesleft", "Guesses left: " + guessesLeft);
			--guessesLeft;
			
			// add animation eg. shaking guess if wrong?
			if (guess.equals(image)) {
				model.addObject("infotext", "YOU WIN");
				model.addObject("proposerinfo", "YOU WIN");
				//TODO boolean hidden field activate "YOU WIN"
				//TODO stop timer and calculate score
				// return scoreboard?
			} else {
				// user gives up round, waits for new segment
				if (nextround == true) {
					giveup = true;
					guessesLeft = 0;
					model.addObject("infotext", "GIVING UP, WAIT FOR NEW SEGMENT");
					model.addObject("proposerinfo", "choose new segment");
				}
			}
		} else {
			// proposer picks a new segment
			model.addObject("infotext", "OUT OF GUESSES, WAIT FOR NEW SEGMENT");
			model.addObject("proposerinfo", "choose new segment");
		}
		model.addObject("listimagesproposed", guesSegment);
		return model;
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
