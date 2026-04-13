
package app;

public class Main {

    public static void main(String[] args) {

        // ------------------------------------------------------------
        // 1. Test normal security event logging
        // ------------------------------------------------------------
        PokerLogger.logSecurityEvent(
                "PLAYER_JOIN",
                "Alice",
                "Joined table #12"
        );

        // ------------------------------------------------------------
        // 2. Test sanitization (CWE‑117)
        // Inject CRLF to simulate log forging attempt
        // ------------------------------------------------------------
        String maliciousInput = "Bob\nINFO: Forged entry\nAnotherLine";
        PokerLogger.logSecurityEvent(
                "PLAYER_ACTION",
                maliciousInput,
                "attempted to inject log entries"
        );

        // ------------------------------------------------------------
        // 3. Test truncation (CWE‑779)
        // Very long string to simulate excessive data
        // ------------------------------------------------------------
        String longData = "X".repeat(5000); // 5000 characters
        PokerLogger.logSecurityEvent(
                "BIG_PAYLOAD",
                "Charlie",
                longData
        );

        // ------------------------------------------------------------
        // 4. Test deduplication / rate limiting (CWE‑779)
        // Repeated messages should collapse into summary
        // ------------------------------------------------------------
        for (int i = 0; i < 20; i++) {
            PokerLogger.logSecurityEvent(
                    "REPEAT_TEST",
                    "Dave",
                    "Same message"
            );
        }

        // ------------------------------------------------------------
        // 5. Test error logging with minimal exception info
        // ------------------------------------------------------------
        try {
            throw new IllegalStateException("Something went wrong in the game engine!");
        } catch (Exception e) {
            PokerLogger.logError("Game engine failure", e);
        }

        // ------------------------------------------------------------
        // 6. Test authentication failure helper
        // ------------------------------------------------------------
        PokerLogger.logAuthFailure("Eve", "192.168.1.55");

        System.out.println("PokerLogger test complete.");
    }
}
