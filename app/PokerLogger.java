package app;

import java.time.Instant;
import java.util.logging.*;

public final class PokerLogger {

    private static final Logger LOGGER = Logger.getLogger(PokerLogger.class.getName());
    // Simple deduplication state
    private static String lastMessage = null;
    private static int repeatCount = 0;
    private static long lastLogTime = 0;
    private static final long FLUSH_INTERVAL_MS = 10_000; // 10 seconds


    static {
        configure();
    }

    private PokerLogger() {}

    /**
     * Configure logger with safe defaults.
     */
    private static void configure() {
        try {
            LOGGER.setUseParentHandlers(false);

            Handler console = new ConsoleHandler();
            console.setFormatter(new SimpleFormatter());
            console.setLevel(Level.INFO);

            Handler file = new FileHandler("audit.log", 5_000_000, 5, true);
            file.setFormatter(new SimpleFormatter());
            file.setLevel(Level.ALL);

            LOGGER.addHandler(console);
            LOGGER.addHandler(file);
            LOGGER.setLevel(Level.ALL);

        } catch (Exception e) {
            System.err.println("Logger initialization failed.");
        }
    }

    // ----------------------------------------------------------------------
    //  CWE‑117: Neutralize untrusted data before logging
    // ----------------------------------------------------------------------
    private static String sanitize(String input) {
        if (input == null) return "[null]";

        // Remove control characters and escape sequences
        String cleaned = input.replaceAll("[\\r\\n\\t]", " ");

        // Remove suspicious log‑forging prefixes
        cleaned = cleaned.replaceAll("^(\\s*at\\s+|\\s*\\*\\s*)", "");

        return cleaned;
    }

    // ----------------------------------------------------------------------
    //  CWE‑223 / CWE‑778: Ensure security‑relevant info is logged
    //  CWE‑224: Do not obscure important details
    // ----------------------------------------------------------------------
    public static void logSecurityEvent(String eventType, String username, String details) {
        String msg = String.format(
            "time=%s | event=%s | user=%s | details=%s",
            Instant.now(),
            sanitize(eventType),
            sanitize(username),
            truncate(sanitize(details), 200) //truncates details
        );

        logWithDedup(Level.INFO, msg);

    }

    // ----------------------------------------------------------------------
    //  CWE‑779: Avoid logging excessive or sensitive data
    // ----------------------------------------------------------------------
    public static void logError(String message, Throwable t) {
        // Only log high‑level error info, not full stack traces or sensitive data
        String sanitizedMessage = sanitize(message);

        logWithDedup(Level.SEVERE, "Error: " + sanitizedMessage);

        // Log minimal exception info
        if (t != null) {
            logWithDedup(Level.SEVERE, "Exception type: " + t.getClass().getSimpleName());
        }
    }

    /**
     * Example: Log an authentication failure without exposing passwords.
     */
    public static void logAuthFailure(String username, String sourceIp) {
        logSecurityEvent(
            "AUTH_FAILURE",
            username,
            "sourceIp=" + sanitize(sourceIp)
        );
    }

    // Limit size of logged strings to prevent excessive data (CWE‑779)
    private static String truncate(String input, int maxLength) {
        if (input == null) return "[null]";
        if (input.length() <= maxLength) return input;
        return input.substring(0, maxLength) + "...[truncated]";
    }
    // Depuplication logic to prevent log flooding (CWE‑779)
    private static synchronized void logWithDedup(Level level, String message) {
        long now = System.currentTimeMillis();

        if (message.equals(lastMessage)) {
            repeatCount++;
            if (now - lastLogTime >= FLUSH_INTERVAL_MS) {
                LOGGER.log(level, "Previous message repeated " + repeatCount + " times");
                repeatCount = 0;
                lastLogTime = now;
            }
            return;
        }

        // Flush previous repeats
        if (repeatCount > 0 && lastMessage != null) {
            LOGGER.log(level, "Previous message repeated " + repeatCount + " times");
        }

        // Log new message
        LOGGER.log(level, message);
        lastMessage = message;
        repeatCount = 0;
        lastLogTime = now;
    }


}
