package ui;

import exception.InvalidInputException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

/**
 * Console input sanitation and date/time parsing utility.
 * Maintained by Member 1 (Mohit Srivastav).
 */
public class InputHelper {
    public static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
    private final Scanner sc;

    public InputHelper(Scanner sc) {
        this.sc = sc;
    }

    /** Reads a trimmed line of text after displaying the given prompt. */
    public String readLine(String prompt) {
        System.out.print(prompt);
        return sc.nextLine().trim();
    }

    /** Reads and parses an integer, throwing InvalidInputException on non-numeric input. */
    public int readInt(String prompt) {
        String s = readLine(prompt);
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            throw new InvalidInputException("Please enter a valid number.");
        }
    }

    /** Reads and parses a LocalDateTime in dd-MM-yyyy HH:mm format. */
    public LocalDateTime readDateTime(String prompt) {
        String s = readLine(prompt + " (dd-MM-yyyy HH:mm): ");
        if (s.isEmpty()) throw new InvalidInputException("Date and time are required.");
        try {
            return LocalDateTime.parse(s, FMT);
        } catch (DateTimeParseException e) {
            throw new InvalidInputException("Invalid format. Use dd-MM-yyyy HH:mm, e.g. 25-12-2026 10:30");
        }
    }
}
