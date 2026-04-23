package app;

public class Account {
    private final String username;
    //CWE-837: Improper Enforcement of a Single, Unique Action
    //Tracks whether this account currently has an active session.
    //Without this, a user could open multiple sessions    
    private boolean loggedIn;
    //CWE-837: Improper Enforcement of a Single, Unique Action
    //tracks login attempts to enforce single session (CWE-837)
    private int loginCount;

    //CWE-283: Unverified Ownership
    //each account is associated with one player profile (CWE-283)
    private final Player player;
    // CWE-770: limit resources
    private static final int LOGIN_MAX = 1; 

    //CWE-640: Weak Password Recovery Mechanism for Forgotten Password
    //tracks recovery attempts to prevent brute-force (CWE-640)
    private int recoveryAttempts;
    //CWE-640: Weak Password Recovery Mechanism for Forgotten Password
    //when recovery lockout began
    private long lockoutTime = 0;


    /**
     * Constructs a new Account for the given username and player profile.
     * @param username username for this account
     * @param player   the Player profile 
     */
    public Account(String username, Player player) {
        this.username = username;
        this.loggedIn = false;
        this.recoveryAttempts = 0;
        this.loginCount = 0;
        this.player = player;
    }

    /**
     * Starts an session
     * CWE-837: Improper Enforcement of a Single, Unique Action
     * Login is rejected if a session is already active or the login limit
     * has been reached. 
     * This prevents multiple sessions at once and
     * repeated login calls
     * @return true if the session was started successfully, false otherwise
     */
    public boolean login() {
        if (loggedIn || loginCount >= LOGIN_MAX) {
            return false;
        }

        loggedIn = true;
        loginCount = 1;
        return true;
    }

    /**
     * Ends the current session and resets the login counter.
     * CWE-837: Improper Enforcement of a Single, Unique Action
     * Resetting loginCount on logout allows the user to log back in
     * during a future session without being permanently blocked.
     */
    public void logout() {
        loggedIn = false;
        //reset login count on logout to allow future logins (CWE-837)
        loginCount = 0;
    }

    /**
     * Returns whether this account currently has an active session.
     * @return true if logged in, false otherwise
     */
    public boolean isLoggedIn() { return loggedIn; }

    /**
     * Returns the username associated with this account.
     * @return the username 
     */
    public String getUsername() { return username; }

        /**
     * Returns the Player profile associated with this account.
     * CWE-283: Unverified Ownership
     * Only the owner can access the Player profile through this method. 
     * The hasPlayer method can be used to verify ownership before allowing access.
     * @return the Player object
     */
    public Player getPlayer() { return player; }

    /**
     * Verifies that the given Player object belongs to this account.
     * CWE-283: Unverified Ownership
     * Without this check, one account could potentially access or modify
     * the player profile of another account. 
     * @param p the Player to check ownership of
     * @return true if this account owns the given Player, false otherwise
     */

    public boolean hasPlayer(Player p) {
        return this.player == p;
    }

    /**
     * Checks whether a password recovery attempt is allowed.
     * CWE-640: Weak Password Recovery Mechanism for Forgotten Password
     * After 3 failed recovery attempts, the account is locked out for
     * 5 minutes. Without this, an attacker could repeatedly
     * guess security question answers until access is gained.
     * @return true if a recovery attempt is allowed, false otherwise
     */

    public boolean canAttemptRecovery() {
        if (recoveryAttempts >= 3) {
            if (System.currentTimeMillis() - lockoutTime < 5 * 60 * 1000) {
                System.out.println("Too many recovery attempts. Try again later.");
                return false;
            }
            recoveryAttempts = 0;
        }
        return true;
    }

    /**
     * Records a failed recovery attempt and starts the lockout timer
     * if the maximum number of attempts has been reached.
     * CWE-640: Weak Password Recovery Mechanism for Forgotten Password
     * Tracking failures and enforcing a lockout period prevents
     * attacks on the security question recovery mechanism.
     */

    public void recordFailedRecovery() {
        recoveryAttempts++;
        if (recoveryAttempts >= 3) {
            lockoutTime = System.currentTimeMillis();
        }
    }
}