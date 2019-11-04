package no.uis.imagegame;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.*;

public class ImageLabelReader {
    private HashMap<String, Integer> imageMapping;
    private HashMap<Integer, ArrayList<String>> reverseImageMapping;
    private HashMap<Integer, String> labelMapping;
    private HashMap<String, Integer> reverseLabelMapping;
    private HashMap<String, String> hashedLabelMapping;
    private HashMap<String, String> reversehashedLabelMapping;

    public ImageLabelReader(String labelMappingFile, String imageMappingFile) {
        this.imageMapping = new HashMap<String, Integer>();
        this.labelMapping = new HashMap<Integer, String>();
        this.hashedLabelMapping = new HashMap<String, String>();
        this.reversehashedLabelMapping = new HashMap<String, String>();
        this.reverseImageMapping = new HashMap<Integer, ArrayList<String>>();
        this.reverseLabelMapping = new HashMap<String, Integer>();

        try {
            Scanner imageScanner = new Scanner(new File(imageMappingFile));
            while (imageScanner.hasNextLine()) {
                String line = imageScanner.nextLine();
                String[] splittedLine = line.split(" ");
                int value = Integer.parseInt(splittedLine[1]);
                String key = splittedLine[0];
                this.imageMapping.put(key, value);
                String hash = generateHashedImageLabel(key);
                this.hashedLabelMapping.put(hash, key);
                if (!this.reversehashedLabelMapping.containsKey(key)) {
                    this.reversehashedLabelMapping.put(key, hash);
                }
                if (!this.reverseImageMapping.containsKey(value)) {
                    this.reverseImageMapping.put(value, new ArrayList<String>());
                }
                this.reverseImageMapping.get(value).add(key);
            }
            imageScanner.close();
            Scanner labelScanner = new Scanner(new File(labelMappingFile));
            while (labelScanner.hasNextLine()) {
                String line = labelScanner.nextLine();
                String[] splittedLine = line.split(" ", 2);
                int key = Integer.parseInt(splittedLine[0]);
                String value = splittedLine[1];
                this.labelMapping.put(key, value);
                this.reverseLabelMapping.put(value, key);
            }
            labelScanner.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    //Given an image file get it's label
    //Note that each image has a single label
    //If the given image file is not found return null
    public String getLabel(String imageFileName) {
        if (this.imageMapping.containsKey(imageFileName)) {
            int labelKey = this.imageMapping.get(imageFileName);
            String label = this.labelMapping.get(labelKey);
            return label;
        }
        return null;
    }

    //Given a label get all the image files corresponding to this label
    //Note that there could be multiple images for a given label
    //If the given label is not found return null
    public String[] getImageFiles(String label) {
        if (this.reverseLabelMapping.containsKey(label)) {
            int key = this.reverseLabelMapping.get(label);
            ArrayList<String> list = this.reverseImageMapping.get(key);
            String[] imageFiles = list.toArray(new String[0]);
            return imageFiles;
        }
        return null;
    }

    public String getHashFromImageLabel(String hash) {
        return reversehashedLabelMapping.get(hash);
    }

    public String getImageLabelFromHash(String input) {
        return hashedLabelMapping.get(input);
    }

    private String generateHashedImageLabel(String input) {
        String generatedLabel = null;
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
            for (int i = 0; i < bytes.length; i++) {
                sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
            }
            generatedLabel = sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return generatedLabel;
    }
}