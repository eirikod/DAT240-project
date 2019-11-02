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
import no.uis.imagegame.ImageController.Greeting;
import no.uis.imagegame.ImageController.HelloMessage;
import no.uis.imagegame.ImageController.User2;
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
	final static String ADDR_CALLBACK_IMAGE = "app/party/20/sendImageId";
	final static String ADDR_FRONT_IMAGE_CALLBACK = "/channel/update/20";

	//Load list of images in my scattered_images folder
	@Value("classpath:/static/images/scattered_images/*")
	private Resource[] resources;

	//Initialize my label reader
	ImageLabelReader labelReader = new ImageLabelReader("src/main/resources/static/label/label_mapping.csv",
			"src/main/resources/static/label/image_mapping.csv");

	@Autowired
	private SimpMessageSendingOperations messageTemplate;

	private int score;
	private int guessesLeft;

//    private Player proposer;
//    private Player guesser;
    private String image;
    private ArrayList<String> propSegment;
    private ArrayList<String> guesSegment;
    private ArrayList<String> chosenSegments;
    private int countTotalSegments;
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
   * @author Eirik
   * @param model
   * @param name
   * @param id
   * @return view
   */
	@RequestMapping("/proposer")
	public ModelAndView showImage(ModelAndView model,
			@ModelAttribute("selectedlabel") Object modelname,
			@RequestParam(value = "id", required = false, defaultValue = "-1") String id) {

			String name = modelname.toString() != null ? modelname.toString() : "cinema";

			String[] files = labelReader.getImageFiles(name);
			String image_folder_name = getImageFolder(files);
			ArrayList<String> imageLabels = getAllLabels(labelReader);

			model.addObject("highestscore", HIGHER_SCORE);

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


	/**
	 *  Lets user choose a picture
	 * @author Eirik
	 * @return model
	 */
	@RequestMapping("/proposerImageSelection")
	public ModelAndView showLabels() {
		ModelAndView model = new ModelAndView("proposerImageSelection");
		ArrayList<String> imageLabels = getAllLabels(labelReader);
		model.addObject("listlabels", imageLabels);
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
    public ModelAndView newSegment(ModelAndView model, @ModelAttribute ("selectedlabel") String name,
    		@RequestParam (value="id", required=false, defaultValue="-1") String id) {
    	if (!id.equals("-1") & guessesLeft > 0) {
    		String[] files = labelReader.getImageFiles(name);
    		String image_folder_name = getImageFolder(files);
        	int segmentID = Integer.parseInt(id);
    		System.out.println(id);
    		guesSegment.add("images/scattered_images/" + image_folder_name + "/" + id  + ".png");
//    		removes button for image segment
//    		needs to find better solution, removes button on proposer side, but makes index go out of bound or not get segment to guesser
//    		propSegment.remove(segmentID);
        } else {
        	//TODO add flash message for user that its not their turn
        }
    	model.addObject("listimages", propSegment);
    	return model;
    }

	@MessageMapping("/party/20/sendImageId")
	public void update(){
		System.out.println("update ---------------------------------------------------");
		SocketMessage sockMess = new SocketMessage();
		messageTemplate.convertAndSend(ADDR_FRONT_IMAGE_CALLBACK, sockMess);
		return;
  }

    /**
     * Guesser init, loads available segments
     * @author Eirik
     * @param model
     * @param name
     * @param id
     * @return model
     */
	@RequestMapping(value = "/guesser")
	public ModelAndView showImageGuesser(ModelAndView model,
			@ModelAttribute ("selectedlabel") String name,
			@RequestParam (value="SubmittedGuess", required=false, defaultValue="-1") String guess) {
		model.addObject("listimagesproposed", guesSegment);
		System.out.println(name);
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
			@RequestParam (value="SubmittedGuess", required=false, defaultValue="-1") String guess) {
		model.addObject("listimagesproposed", guesSegment);
		if (!guess.equals("-1") & guessesLeft > 0) {
			System.out.println("guesses left: " + guessesLeft);
			--guessesLeft;

			if (guess == name) {
				System.out.println("You win");
				//TODO flash message redirect attribute
				return model;
			} else {
				System.out.println("Wrong guess " + guess);
				System.out.println("right answer: " + name);
			}
		} else {
			System.out.println("You're out of guesses, wait for new segment");
		}
		return model;
	}
	//TODO
	// proposer choose a segment (newSegment)
	// 3 guesses or give up or correct



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

	@RequestMapping("/addPlayer")
	public String addPlayer(Model model, User user) {
		model.addAttribute("obj", user);
		System.out.println(user.getName());
		return "welcomePage";
	}

	public class MonThread extends Thread {

		private ImageController image_controller;

		public MonThread(ImageController imageController) {
			super();
			this.image_controller = imageController;
		}

		public void run() {
			try {
				System.out.println("Début du thread");
				this.sleep(10000);
				System.out.println("Fin du thread");
				HelloMessage message = new HelloMessage("coucou");
				this.image_controller.greeting("coucou");
			} catch (Exception e) {
				// TODO: handle exception
				System.out.println(e);
			}
		}
	}

	@RequestMapping("/hello")
	public String hello() {
		return "index";
	}

	@MessageMapping("/hello")
    @SendTo("/channel/greetings")
    public Greeting greeting(String message) throws Exception {
		System.out.println("greetings");
		System.out.println("Avant délai ---------------------------------------------------");
		System.out.println(message);
        Thread.sleep(1000); // simulated delay
		System.out.println("Après délai ---------------------------------------------------");
		return new Greeting("Hello, " + HtmlUtils.htmlEscape(message) + "!");
    }

	@MessageMapping("/welcomePage")
    @SendTo("/channel/lunch")
    public String search(String message) throws Exception {
		System.out.println("search called");
		System.out.println("Avant délai ---------------------------------------------------");
		System.out.println(message);
        Thread.sleep(1000); // simulated delay
		System.out.println("Après délai ---------------------------------------------------");
		String url = "http://localhost:8080/proposer";
		return url;
    }

//	@MessageMapping("/welcomePage")
//    @SendTo("/channel/state")
//    public String changeState(String message) throws Exception {
//		System.out.println("changeState called");
//		System.out.println("Avant délai ---------------------------------------------------");
//		System.out.println(message);
//        Thread.sleep(1000); // simulated delay
//		System.out.println("Après délai ---------------------------------------------------");
//		String url = "http://localhost:8080/proposer";
//		return url;
//    }

	@MessageMapping("/notif")
    @SendTo("/channel/notif")
    public String notif(String message) throws Exception {
		System.out.println("notif");
		System.out.println("Avant délai ---------------------------------------------------");
		System.out.println(message);
        Thread.sleep(1000); // simulated delay
		System.out.println("Après délai ---------------------------------------------------");
		String url = "COUCOU";
		return url;
    }

	protected class User2{
		public boolean isLogged = true;
		public int highestScore = 41;
		public String name = "SuperMan";
		public String map = "Casino";
	}

	protected class HelloMessage {

	    private String name;

	    public HelloMessage() {
	    }

	    public HelloMessage(String name) {
	        this.name = name;
	    }

	    public String getName() {
	        return name;
	    }

	    public void setName(String name) {
	        this.name = name;
	    }
	}

	protected class Greeting {

	    private String content;

	    public Greeting() {
	    }

	    public Greeting(String content) {
	        this.content = content;
	    }

	    public String getContent() {
	        return content;
	    }

	}
}
