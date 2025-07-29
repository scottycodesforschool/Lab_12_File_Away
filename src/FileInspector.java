import javax.swing.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays; // Needed for Arrays.stream

/**
 * FileInspector.java
 *
 * This program allows a user to select a text file using a JFileChooser,
 * reads the file line by line, echoes its content to the console,
 * and then generates a summary report including the file name,
 * total number of lines, words, and characters.
 *
 * It utilizes Java NIO for file operations and try-with-resources
 * for robust resource management.
 *
 * @author Gemini
 */
public class FileInspector {

    /**
     * Main method to run the File Inspector program.
     *
     * @param args Command line arguments (not used in this program).
     */
    public static void main(String[] args) {
        JFileChooser chooser = new JFileChooser();
        Path selectedFilePath = null; // Path object to store the selected file's path

        // Counters for the summary report
        long lineCount = 0;
        long wordCount = 0;
        long charCount = 0;

        try {
            // Set the current directory of the JFileChooser to the 'src' folder
            // This assumes a standard IntelliJ project structure where 'src' is
            // a direct child of the user.dir (project root).
            Path projectRoot = Paths.get(System.getProperty("user.dir"));
            Path srcDirectory = projectRoot.resolve("src");

            // Ensure the src directory exists before setting it, otherwise default to project root
            if (Files.exists(srcDirectory) && Files.isDirectory(srcDirectory)) {
                chooser.setCurrentDirectory(srcDirectory.toFile());
            } else {
                // Fallback to project root if src directory is not found or is not a directory
                System.out.println("Warning: 'src' directory not found at " + srcDirectory.toAbsolutePath() + ". Opening file chooser in project root.");
                chooser.setCurrentDirectory(projectRoot.toFile());
            }

            // Show the file open dialog
            if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                // Get the selected file and convert it to a Path object
                File selectedFile = chooser.getSelectedFile();
                selectedFilePath = selectedFile.toPath();

                System.out.println("--- Reading File: " + selectedFilePath.getFileName() + " ---");

                // Use try-with-resources to ensure the BufferedReader is automatically closed
                try (BufferedReader reader = Files.newBufferedReader(selectedFilePath)) {
                    String line;
                    // Read the file line by line until the end
                    while ((line = reader.readLine()) != null) {
                        // Echo the line to the screen
                        System.out.println(line);

                        // Increment line count
                        lineCount++;

                        // Add characters in the line to total character count
                        // Note: This counts all characters including spaces, punctuation, etc.
                        charCount += line.length();

                        // Count words in the line
                        // Trim the line to handle leading/trailing spaces
                        // Split by one or more whitespace characters (\\s+)
                        // Filter out empty strings that might result from multiple spaces or empty lines
                        String[] wordsInLine = line.trim().split("\\s+");
                        long actualWords = Arrays.stream(wordsInLine)
                                .filter(word -> !word.isEmpty())
                                .count();
                        wordCount += actualWords;
                    }
                    System.out.println("\n--- End of File Content ---");

                    // Print the summary report
                    System.out.println("\n--- File Summary Report ---");
                    System.out.println("File Name: " + selectedFilePath.getFileName());
                    System.out.println("Full Path: " + selectedFilePath.toAbsolutePath());
                    System.out.println("Number of Lines: " + lineCount);
                    System.out.println("Number of Words: " + wordCount);
                    System.out.println("Number of Characters: " + charCount);
                    System.out.println("---------------------------\n");

                } catch (NoSuchFileException e) {
                    System.err.println("Error: The selected file does not exist: " + e.getFile());
                    e.printStackTrace();
                } catch (IOException e) {
                    System.err.println("An I/O error occurred while reading the file: " + e.getMessage());
                    e.printStackTrace();
                }

            } else {
                // User cancelled the file chooser dialog
                System.out.println("File selection cancelled. No file was processed.");
            }

        } catch (SecurityException e) {
            System.err.println("Security Error: Permission denied to access file or directory. " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            // Catch any other unexpected exceptions
            System.err.println("An unexpected error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
