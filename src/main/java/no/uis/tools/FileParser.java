package no.uis.tools;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class FileParser {
    /**
     * Simple lambda interface to implicitly create a callback function
     * in the parseFile method.
     *
     * @author Alan Rostem
     */
    @FunctionalInterface
    public interface LineCallback {
        void apply(String s);
    }

    /**
     * Custom file parsing using lambdas. The callback parameter is called for every line in the given
     * text file.
     *
     * @param filePath          Absolute file path
     * @param lineParseCallback Lambda which takes a String as parameter
     * @author Alan Rostem
     * @see LineCallback
     */
    public static void parseText(String filePath, LineCallback lineParseCallback) {
        String line;
        try (FileReader fileReader = new FileReader(filePath)) {
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            // Print out to the console that the file reading went well
            System.out.println("FileParser [" + filePath + "]: Successfully read file! Parsing...");
            try {
                while ((line = bufferedReader.readLine()) != null) {
                    lineParseCallback.apply(line); // Apply the callback function for every line
                }
                System.out.println("FileParser [" + filePath + "]: Successfully parsed!");
            } catch (Exception e) {
                System.out.println("FileParser [" + filePath + "]: Failed to parse!");
                e.printStackTrace();
            }
            bufferedReader.close(); // Close file after being done
            // Handle the errors:
        } catch (FileNotFoundException e) {
            System.out.println("FileParser [" + filePath + "]: Could not open file!");
        } catch (IOException e) {
            System.out.println("FileParser [" + filePath + "]: Error reading file at!");
        }
    }

    public static void writeTo(String fileName, String data) {
        try {
            Files.write(Paths.get(fileName), data.getBytes(), StandardOpenOption.APPEND);
            System.out.println("Successfully wrote to file!");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
