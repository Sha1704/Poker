package app;

public class Account {
    private final String username;
    private String password;
    //security question answer for password recovery
    private final String answerSec;
    //tracks if user is currently logged in to prevent multiple sessions (CWE-837)
    private boolean loggedIn;
    //tracks recovery attempts to prevent brute-force (CWE-640)
    private int recoveryAttempts;
    //tracks login attempts to enforce single session (CWE-837)
    private int loginCount;
    //each account is associated with one player profile (CWE-283)
    private final Player player;
    // CWE-770: limit resources
    private static final int LOGIN_MAX = 1; 
    private long lockoutTime = 0;

    public Account(String username, String password, String answerSec, Player player) {
        this.username = username;
        this.password = password;
        this.answerSec = answerSec;
        this.loggedIn = false;
        this.recoveryAttempts = 0;
        this.loginCount = 0;
        this.player = player;
    }

    public boolean setPassword(String newPassword) {
    if (newPassword == null || newPassword.isBlank()) return false;
    this.password = newPassword;
    return true;
}

    // CWE-837: enforce single unique action
    public boolean login(String passwordAttempt) {
        if (loggedIn || loginCount >= LOGIN_MAX) {
            System.out.println("Already logged in or login limit reached.");
            //prevent multiple logins to protect account integrity (CWE-837)
            return false;
        }

        if (password.equals(passwordAttempt)) {
            loggedIn = true;
            loginCount = 1;
            return true;
        }
        //wrong password
        return false;
    }

    public void logout() {
        loggedIn = false;
        //reset login count on logout to allow future logins (CWE-837)
        loginCount = 0;
    }

    public boolean isLoggedIn() { return loggedIn; }
    public String getUsername() { return username; }
    public Player getPlayer() { return player; }

    // Secure password check for deletion or sensitive actions
    public boolean checkPassword(String attempt) {
        return password.equals(attempt);
    }

    // CWE-283: verify ownership
    public boolean hasPlayer(Player p) {
        return this.player == p;
    }

    // CWE-640: password recovery with throttling
    public boolean passwordRecovery(String answerAttempt) {
        // Increment recovery attempts and check for lockout
        if (recoveryAttempts >= 3) {
        if (System.currentTimeMillis() - lockoutTime < 5 * 60 * 1000) {
            System.out.println("Too many recovery attempts. Try again later.");
            return false;
        }
        // cooldown expired, reset
        recoveryAttempts = 0;
        }

        if (!answerSec.equals(answerAttempt)) {
        recoveryAttempts++;
            if (recoveryAttempts >= 3) lockoutTime = System.currentTimeMillis();
            System.out.println("Recovery failed.");
            return false;
        }

        //reset recovery attempts on successful recovery to allow future recoveries
        recoveryAttempts = 0;
        System.out.println("Recovery successful.");
        return true;
    }
}