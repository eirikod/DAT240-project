package no.uis.imagegame;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.*;

import static java.lang.String.format;
import no.uis.websocket.SocketMessage;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import no.uis.party.PartyManager;
import no.uis.party.Party;
import no.uis.party.QueueController;
import no.uis.players.Player;
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

	@MessageMapping("/party/{partyId}/respToGuesser")
	public void respondToGuesser(@DestinationVariable String partyId, @Payload SocketMessage chatMessage,
						SimpMessageHeaderAccessor headerAccessor) {
    	Party party = PartyManager.getParty(partyId);



    	SocketMessage msg = new SocketMessage();
    	msg.setSender(party.getProposer().getId());
		msg.setType("JOIN_PARTY");

		HashMap<String, Object> guesserContent = new HashMap<>();
		guesserContent.put("role", "GUESSER");
		guesserContent.put("partyId", "" + partyId);
		String hashedLabel = "";
		try {
			hashedLabel = (String) chatMessage.getContent();
			//hashedLabel = generateHashedImageLabel((String) chatMessage.getContent());
		} catch (Exception e) {
			e.printStackTrace();
		}

		guesserContent.put("selectedlabel", hashedLabel);
		party.getGame().setImage(hashedLabel);
		msg.setContent(guesserContent);
		party.getGuesser().sendData(msg, messageTemplate);
	}

	private static String generateHashedImageLabel(String input)
	{
		String generatedPassword = null;
		byte[] salt = new byte[0];
		try {
			SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
			salt = new byte[16];
			sr.nextBytes(salt);
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			md.update(salt);
			byte[] bytes = md.digest(input.getBytes());
			StringBuilder sb = new StringBuilder();
			for(int i=0; i< bytes.length ;i++)
			{
				sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
			}
			generatedPassword = sb.toString();
		}
		catch (NoSuchAlgorithmException e)
		{
			e.printStackTrace();
		}
		return generatedPassword;
	}

	@MessageMapping("/party/{partyId}/addUser")
	public void addUser(@DestinationVariable String partyId, @Payload SocketMessage chatMessage,
						SimpMessageHeaderAccessor headerAccessor) {
		String currentRoomId = (String) headerAccessor.getSessionAttributes().put("room_id", partyId);
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
			@RequestParam(value = "partyId", required = false, defaultValue = "-1") String partyId) {

			String name = modelname.toString() != null ? modelname.toString() : "cinema";

			String[] files = labelReader.getImageFiles(name);
			String image_folder_name = getImageFolder(files);
			
//			ArrayList<String> imageLabels = getAllLabels(labelReader);

			Party party = PartyManager.getParty(partyId);
			Player proposer = party.getProposer();

			//TODO
//				model.addObject("highestscore", player.getHigherScore());
			model.addObject("userId", proposer.getId());
			model.addObject("partyId", partyId);

			//model.addObject("listlabels", imageLabels);

			// finds number of segments per image
			countTotalSegments = new File("src/main/resources/static/images/scattered_images/" + image_folder_name).list().length;
			countTotalSegments = countTotalSegments-1;
			countRemainingSegments = countTotalSegments;
			guessesLeft = 0;
			giveup = false;
			score = 1000;

			propSegment = new ArrayList<>();
			guesSegment = new ArrayList<>();

			for (int i = 0; i < countTotalSegments; ++i) {
				propSegment.add("images/scattered_images/" + image_folder_name + "/" + i + ".png");
			}

			model.addObject("listimages", propSegment);
			return model;
	}

    @MessageMapping("/party/{partyId}/update")
    public void receiveSocketUpdate(@DestinationVariable String partyId, SocketMessage message) {
        PartyManager.getParty(partyId).receiveUpdateFromFront(message, messageTemplate);
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
		Party party = PartyManager.getParty(partyId);

		Player guesser = party.getProposer();
		String userId = guesser.getId();
		model.addObject("listlabels", imageLabels);
		model.addObject("userId", userId);
		model.addObject("partyId", partyId);
		return model;
	}


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
		
//		Party party = partyManager.getParty(partyId);
//		Player guesser = party.getGuesser();
//		String userId = Long.toString(guesser.getId());
		model.addObject("userId", PartyManager.getParty(partyId).getGuesser().getId());
		model.addObject("partyId", partyId);
		
		String name = modelname.toString() != null ? modelname.toString() : "cinema";
		String[] files = labelReader.getImageFiles(name);
		String image_folder_name = getImageFolder(files);
		// finds number of segments per image
		countTotalSegments = new File("src/main/resources/static/images/scattered_images/" + image_folder_name).list().length;
		countTotalSegments = countTotalSegments-1;
		countRemainingSegments = countTotalSegments;
		guessesLeft = 3;
		propSegment = new ArrayList<>();
		for (int i = 0; i < countTotalSegments; ++i) {
			propSegment.add("images/scattered_images/" + image_folder_name + "/" + i + ".png");
		}
		model.addObject("listimages", propSegment);
		
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
