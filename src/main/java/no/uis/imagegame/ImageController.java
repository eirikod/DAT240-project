package no.uis.imagegame;

import java.io.*;
import java.util.*;

import static java.lang.String.format;

import no.uis.websocket.SocketMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import no.uis.party.PartyManager;
import no.uis.party.Party;
import no.uis.players.Player;
import no.uis.repositories.PlayerRepository;


@Controller
public class ImageController {

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


    /**
     * Send the server respond to the guesser view 
     *
     * @param partyId
     * @param chatMessage
     * @param headerAccessor
     * @param id
     * @return void
     * @author Allan
     */
    @MessageMapping("/party/{partyId}/respToGuesser")
    public void respondToGuesser(@DestinationVariable String partyId, @Payload SocketMessage chatMessage,
                                 SimpMessageHeaderAccessor headerAccessor) {
        Party party = PartyManager.getParty(partyId);
        if (party == null) {
            return;
        }

        SocketMessage msg = new SocketMessage();
        msg.setSender(party.getProposer().getId());
        msg.setType("JOIN_PARTY");

        HashMap<String, Object> guesserContent = new HashMap<>();
        guesserContent.put("role", "GUESSER");
        guesserContent.put("partyId", "" + partyId);
        guesserContent.put("selectedlabel", chatMessage.getContent());
        msg.setContent(guesserContent);
        party.getGuesser().sendData(msg, messageTemplate);
    }

    /**
     * Initialize the websocket handler and send a response to the client subscription 
     *
     * @param partyId
     * @param chatMessage
     * @param hhheaderAccessor
     * @return void
     * @author Allan & Gregoire
     */

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
     *
     * @param model
     * @param modelname: image name
     * @param partyId
     * @param userName
     * @return view
     * @author Eirik & Gregoire
     */
    @RequestMapping("/proposer")
    public ModelAndView showImage(ModelAndView model,
                                  @ModelAttribute("selectedlabel") Object modelname,
                                  @RequestParam(value = "partyId", required = false, defaultValue = "-1") String partyId,
                                  @RequestParam(value = "username", required = false, defaultValue = "-1") String userName) {

        String name = modelname.toString() != null ? modelname.toString() : "cinema";

        String[] files = labelReader.getImageFiles(name);
        String image_folder_name = getImageFolder(files);
        Party party = PartyManager.getParty(partyId);
        Player proposer = party.getProposer();

        model.addObject("userId", proposer.getId());
        model.addObject("partyId", partyId);
        model.addObject("username", userName);


        // finds number of segments per image
        int countTotalSegments = -1 + Objects.requireNonNull(new File("src/main/resources/static/images/scattered_images/" + image_folder_name).list()).length;
        PartyManager.getParty(partyId).getGame().setImage(name, countTotalSegments);
        ArrayList<String> imageSegments = new ArrayList<>();

        for (int i = 0; i < countTotalSegments; ++i) {
            imageSegments.add("images/scattered_images/" + image_folder_name + "/" + i + ".png");
        }

        model.addObject("listimages", imageSegments);
        return model;
    }

    /**
     * Receive the client message using update
     *
     * @param partyId
     * @param message
     * @return void
     * @author Allan & Gregoire
     */
    @MessageMapping("/party/{partyId}/update")
    public void receiveSocketUpdate(@DestinationVariable String partyId, SocketMessage message) {
        PartyManager.getParty(partyId).receiveUpdateFromFront(message, messageTemplate);
    }

	/**
	 *  ProposerImageSelction View init ; allows the user choose a picture
	 * @author Eirik & Gregoire
	 * @param partyId
	 * @param userName
	 * @return model
	 */
	@RequestMapping("/proposerImageSelection")
	public ModelAndView showLabels(
			@RequestParam(value = "partyId", required = false, defaultValue = "-1") String partyId,
			@RequestParam(value = "username", required = false, defaultValue = "-1") String userName) {
		ModelAndView model = new ModelAndView("proposerImageSelection");
		ArrayList<String> imageLabels = getAllLabels(labelReader);
		Party party = PartyManager.getParty(partyId);

		Player guesser = party.getProposer();
		String userId = guesser.getId();
		model.addObject("listlabels", imageLabels);
		model.addObject("userId", userId);
		model.addObject("partyId", partyId);
		model.addObject("username", userName);
		return model;
	}

    /**
     * Guesser view init ; Allows the guesser playing
     *
     * @param modelname: image name
     * @param partyId
     * @param userName
     * @return model
     * @author Eirik & Gregoire
     */
    @RequestMapping(value = "/guesser")
    public ModelAndView showImageGuesser(
            @RequestParam(value = "partyId", required = false, defaultValue = "-1") String partyId,
            @RequestParam(value = "username", required = false, defaultValue = "-1") String userName) {

        ModelAndView model = new ModelAndView("guesser");
        model.addObject("userId", PartyManager.getParty(partyId).getGuesser().getId());
        model.addObject("partyId", partyId);
        model.addObject("username", userName);

        String name = PartyManager.getParty(partyId).getGame().getImageName();
        String[] files = labelReader.getImageFiles(name);
        String image_folder_name = getImageFolder(files);
        // finds number of segments per image
        int countTotalSegments = -1 + Objects.requireNonNull(new File("src/main/resources/static/images/scattered_images/" + image_folder_name).list()).length;
        ArrayList<String> imageSegments = new ArrayList<>();
        for (int i = 0; i < countTotalSegments; ++i) {
            imageSegments.add("images/scattered_images/" + image_folder_name + "/" + i + ".png");
        }

        model.addObject("listimages", imageSegments);
        return model;
    }

	/**
     * @param files
     * @return image_folder_name
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
     * @param ilr
     * @return labels
     */
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
