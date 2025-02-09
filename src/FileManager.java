import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class FileManager {

    // Read lines from a file and return them as an ArrayList of strings
    public static ArrayList<String[]> readInput(String filename) {
        ArrayList<String> inputText = new ArrayList<>();//contains all the input lines
        ArrayList<String[]> Instructions = new ArrayList<>(); // contains all the lines in array form, separated by
        // space

        try (Scanner fileScanner = new Scanner(new File(filename))) {
            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine().trim();
                if (!line.isEmpty() && !line.startsWith(";")) {
                    inputText.add(line);
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println("Error: File not found - " + filename);
            e.printStackTrace();
        } catch (OutOfMemoryError e) {
            System.err.println("Error: Out of memory while reading file - " + filename);
            e.printStackTrace();
        }

        for (String str : inputText) {
            String[] temp = str.split(" ");
            Instructions.add(temp);
        }
        return Instructions;
    }

    // Write content to a file
    public static void writeToFile(String filename, ArrayList<String> content) {
        try (FileWriter writer = new FileWriter(new File(filename))) {
            for (String line : content) {
                writer.write(line + "\n");
            }
        } catch (IOException e) {
            System.err.println("Error writing to file: " + filename);
            e.printStackTrace();
        }
    }
}
