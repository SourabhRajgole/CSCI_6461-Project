import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class FileManager {

    /// Reads lines from a file and returns them as an ArrayList of string arrays
    public static ArrayList<String[]> readInput(String filename) {
        ArrayList<String> inputText = new ArrayList<>();// Stores all input lines
        ArrayList<String[]> Instructions = new ArrayList<>();  // Stores parsed lines as an array of words


        try (Scanner fileScanner = new Scanner(new File(filename))) {
            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine().trim();// Remove unnecessary spaces
             // Ignore empty lines and comments (lines starting with ';')
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

     // Splitting each line into an array of words for further processing
        for (String str : inputText) {
            String[] temp = str.split(" ");
            Instructions.add(temp);
        }
        return Instructions;
    }

    // Writes content to a file line by line
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
