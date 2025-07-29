import javax.swing.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * DataSaver.java
 *
 * This program collects user data (First Name, Last Name, ID, Email, Year of Birth),
 * formats it into CSV records, and saves these records to a text file
 * specified by the user in the 'src' directory of the IntelliJ project.
 * It uses a basic SafeInput utility for validated user input.
 *
 * @author Gemini
 */
public class DataSaver {

    // Scanner for console input, used by SafeInput
    private static final Scanner CONSOLE_SCANNER = new Scanner(System.in);

    /**
     * Main method to run the Data Saver program.
     *
     * @param args Command line arguments (not used in this program).
     */
    public static void main(String[] args) {
        ArrayList<String> csvRecords = new ArrayList<>(); // List to store CSV formatted records
        boolean doneInputting = false;

        System.out.println("--- Data Collection for CSV File ---");

        // Loop to collect multiple records from the user
        do {
            System.out.println("\n--- Enter New Record ---");
            String firstName = SafeInput.getNonZeroLenString(CONSOLE_SCANNER, "Enter First Name");
            String lastName = SafeInput.getNonZeroLenString(CONSOLE_SCANNER, "Enter Last Name");

            // ID Number: 6 digits, zero-padded string (e.g., 000001)
            // Using a regex to ensure exactly 6 digits
            String idNumber = SafeInput.getRegExString(CONSOLE_SCANNER, "Enter ID Number (6 digits, e.g., 000001)", "^\\d{6}$");

            // Email: Basic email regex validation
            String email = SafeInput.getRegExString(CONSOLE_SCANNER, "Enter Email (e.g., user@example.com)", "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$");

            // Year of Birth: 4-digit integer, within a reasonable range
            int yearOfBirth = SafeInput.getRangedInt(CONSOLE_SCANNER, "Enter Year of Birth (e.g., 1978)", 1900, 2025); // Adjust range as needed

            // Format data into CSV record
            String csvRecord = String.format("%s,%s,%s,%s,%d",
                    firstName, lastName, idNumber, email, yearOfBirth);

            csvRecords.add(csvRecord); // Add the formatted record to the list
            System.out.println("Record added: " + csvRecord);

            doneInputting = !SafeInput.getYNConfirm(CONSOLE_SCANNER, "Do you want to add another record? (Y/N)");

        } while (!doneInputting);

        System.out.println("\n--- Data Collection Complete ---");

        if (csvRecords.isEmpty()) {
            System.out.println("No records were entered. Exiting without saving a file.");
            CONSOLE_SCANNER.close();
            return;
        }

        // Prompt for file name
        String fileName = SafeInput.getNonZeroLenString(CONSOLE_SCANNER, "Enter the name for the CSV file (e.g., mydata)");
        if (!fileName.toLowerCase().endsWith(".csv")) {
            fileName += ".csv"; // Ensure .csv extension
        }

        // Determine the file path in the 'src' directory
        Path projectRoot = Paths.get(System.getProperty("user.dir"));
        Path srcDirectory = projectRoot.resolve("src");
        Path filePath = srcDirectory.resolve(fileName);

        try {
            // Ensure the 'src' directory exists. If not, create it.
            // This is important if the program is run from a context where 'src' isn't guaranteed.
            if (!Files.exists(srcDirectory)) {
                Files.createDirectories(srcDirectory);
                System.out.println("Created directory: " + srcDirectory.toAbsolutePath());
            }

            // Use try-with-resources to ensure the BufferedWriter is automatically closed
            // StandardOpenOption.CREATE: Creates the file if it doesn't exist.
            // StandardOpenOption.TRUNCATE_EXISTING: If file exists, truncate it to 0 bytes.
            // (Use StandardOpenOption.APPEND if you wanted to add to an existing file)
            try (BufferedWriter writer = Files.newBufferedWriter(
                    filePath,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING)) {

                for (String record : csvRecords) {
                    writer.write(record);
                    writer.newLine(); // Add a new line after each record
                }
                System.out.println("Data successfully saved to: " + filePath.toAbsolutePath());

            } catch (IOException e) {
                System.err.println("An I/O error occurred while writing the file: " + e.getMessage());
                e.printStackTrace();
            }

        } catch (SecurityException e) {
            System.err.println("Security Error: Permission denied to create directory or write file. " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("An unexpected error occurred: " + e.getMessage());
            e.printStackTrace();
        } finally {
            CONSOLE_SCANNER.close(); // Close the scanner when done with all input
        }
    }

    /**
     * Private static nested class for SafeInput utility methods.
     * These methods provide validated user input from the console.
     */
    private static class SafeInput {

        /**
         * Gets a non-zero length string from the user.
         *
         * @param pipe   Scanner object to read input.
         * @param prompt Message to display to the user.
         * @return A non-empty string.
         */
        public static String getNonZeroLenString(Scanner pipe, String prompt) {
            String retString = "";
            do {
                System.out.print("\n" + prompt + ": ");
                retString = pipe.nextLine().trim();
            } while (retString.length() == 0);
            return retString;
        }

        /**
         * Gets an integer within a specified range from the user.
         *
         * @param pipe   Scanner object to read input.
         * @param prompt Message to display to the user.
         * @param low    The lower bound of the acceptable range (inclusive).
         * @param high   The upper bound of the acceptable range (inclusive).
         * @return An integer within the specified range.
         */
        public static int getRangedInt(Scanner pipe, String prompt, int low, int high) {
            int retVal = low - 1; // Initialize outside range to force loop
            String trash;
            boolean done = false;

            do {
                System.out.print("\n" + prompt + " [" + low + " - " + high + "]: ");
                if (pipe.hasNextInt()) {
                    retVal = pipe.nextInt();
                    pipe.nextLine(); // Clear the newline from the buffer
                    if (retVal >= low && retVal <= high) {
                        done = true;
                    } else {
                        System.out.println("Input out of range. Please enter a value between " + low + " and " + high + ".");
                    }
                } else {
                    trash = pipe.nextLine(); // Consume invalid input
                    System.out.println("Invalid input. Please enter an integer. You entered: " + trash);
                }
            } while (!done);
            return retVal;
        }

        /**
         * Gets a string that matches a specified regular expression.
         *
         * @param pipe   Scanner object to read input.
         * @param prompt Message to display to the user.
         * @param regEx  The regular expression to match against.
         * @return A string that matches the regular expression.
         */
        public static String getRegExString(Scanner pipe, String prompt, String regEx) {
            String retString;
            boolean done = false;
            Pattern pattern = Pattern.compile(regEx);
            Matcher matcher;

            do {
                System.out.print("\n" + prompt + ": ");
                retString = pipe.nextLine().trim();
                matcher = pattern.matcher(retString);

                if (matcher.matches()) {
                    done = true;
                } else {
                    System.out.println("Invalid input. Does not match the required format (" + regEx + "). Please try again.");
                }
            } while (!done);
            return retString;
        }

        /**
         * Gets a Y/N (Yes/No) confirmation from the user.
         *
         * @param pipe   Scanner object to read input.
         * @param prompt Message to display to the user.
         * @return True if 'Y' or 'y' is entered, false if 'N' or 'n' is entered.
         */
        public static boolean getYNConfirm(Scanner pipe, String prompt) {
            String response;
            boolean done = false;
            boolean confirmed = false;

            do {
                System.out.print("\n" + prompt + " (Y/N): ");
                response = pipe.nextLine().trim().toUpperCase();
                if (response.equals("Y")) {
                    confirmed = true;
                    done = true;
                } else if (response.equals("N")) {
                    confirmed = false;
                    done = true;
                } else {
                    System.out.println("Invalid input. Please enter 'Y' or 'N'.");
                }
            } while (!done);
            return confirmed;
        }
    }
}
