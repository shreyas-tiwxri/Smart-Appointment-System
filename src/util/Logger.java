package util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Simple sassy logger that prints colourful messages to the console.
 * All methods are static for easy use throughout the codebase.
 *
 * The logger supports four levels (INFO, SUCCESS, WARN, ERROR) and automatically
 * adds a timestamp and a little attitude.
 */
public final class Logger {
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final String RESET = "\u001B[0m";
    private static final String INFO = "\u001B[36m";    // Cyan
    private static final String SUCCESS = "\u001B[32m"; // Green
    private static final String WARN = "\u001B[33m";    // Yellow
    private static final String ERROR = "\u001B[31m";   // Red

    private Logger() { /* util class */ }

    private static String ts() {
        return LocalDateTime.now().format(TIME_FMT);
    }

    public static void info(String msg) {
        System.out.println(INFO + "[" + ts() + "] 🤓 INFO: " + msg + RESET);
    }

    public static void success(String msg) {
        System.out.println(SUCCESS + "[" + ts() + "] 🎉 SUCCESS: " + msg + RESET);
    }

    public static void warn(String msg) {
        System.out.println(WARN + "[" + ts() + "] ⚠️ WARN: " + msg + RESET);
    }

    public static void error(String msg) {
        System.err.println(ERROR + "[" + ts() + "] ❌ ERROR: " + msg + RESET);
    }
}

