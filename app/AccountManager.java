package app;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class AccountManager {
    private Authentication auth = new Authentication();
    /**
     * CWE-283: Unverified Ownership
     *  This ensures every account has a
     * verified owner and game data cannot be accessed without going
     * through the correct account.
     */
    private final Map<String, Account> accounts = new HashMap<>();
    
    /**
     * CWE-770: Allocation of Resources Without Limits or Throttling
     * gives a max of the total number of accounts that can be registered.
     * Without this, an attacker could flood the system with
     * registrations
     */
    private final int maxAccounts = 100; 
    private int nextPlayerId = 1;

    private static final String USER_FILE = "user.txt";
    
    //loads accounts from file on startup
    public AccountManager() {
        loadAccountsFromFile();
    }


    /**
     * Reads the user file created by {@link Authentication} and creates
     * an Account and Player for every saved user. Called once at startup
     * so that players who registered in a previous session do not need
     * to register again.
     * File format written by Authentication     *
     */

    private void loadAccountsFromFile() {
    File userFile = new File(USER_FILE);
    if (!userFile.exists()) {
        return; 
    }
    try (BufferedReader br = new BufferedReader(new FileReader(userFile))) {
        String line;
        while ((line = br.readLine()) != null) {
            String[] parts = line.split("\\|", 6);
            if (parts.length != 6) continue; 

            String username = parts[0];
            String key = username.toLowerCase();
            if (accounts.containsKey(key)) continue; 

            Player player = new Player(nextPlayerId++, username, 1000.0);
            accounts.put(key, new Account(username, player));
        }
    } catch (IOException e) {
        System.out.println("Error: could not load accounts " + e.getMessage());
    }
}

    /**
     * Registers a new user by delegating credential storage to
     * {@link Authentication} and then creating an Account
     * and Player profile on success.
     *
     * CWE-708: Incorrect Ownership Assignment
     * The startingChips value is stopped to a valid range before being
     * assigned to the Player. Without this check, a caller could pass
     * an large chip amount
     *
     * CWE-770: Allocation of Resources Without Limits or Throttling
     * Registration is rejected once maxAccounts is reached to prevent
     * account creation that could overload memory.
     *
     * @param username      username
     * @param password      the password
     * @param answerSec     the answer to the security question
     * @param startingChips the requested starting chip count
     * @return true if registration succeeded, false otherwise
     */
    public boolean register(String username, String password, String answerSec, double startingChips) {
        if (username == null || username.isBlank() ||
            password == null || password.isBlank() ||
            answerSec == null || answerSec.isBlank()) {
            return false;
        }

        if (Double.isNaN(startingChips) || Double.isInfinite(startingChips)) {
            return false;
        }

        String key = username.toLowerCase();

        if (accounts.containsKey(key)) return false;
        if (accounts.size() >= maxAccounts) return false;

        double minChips = 1;
        double maxChips = 5000;
        startingChips = Math.max(minChips, Math.min(maxChips, startingChips));

        try {
            if (!auth.register(username, password, answerSec)) return false;
        } catch (Exception e) {
            return false;
        }

        Player newPlayer = new Player(nextPlayerId++, username, startingChips);
        accounts.put(key, new Account(username, newPlayer));    
        return true;
    }


    /**
     * Authenticates a user using {@link Authentication} and opens a game session.
     *
     * CWE-837: Improper Enforcement of a Single, Unique Action
     * After credentials are verified, {@link Account#login()} is called to
     * enforce that only one active session exists per account at a time.
     * If the account is already logged in, the login is rejected.
     *
     * @param username the user's username
     * @param password the user's password
     * @param answer   the answer to {@link Authentication#QUESTION}
     * @return the active Account if login succeeded, null otherwise
     */

    public Account login(String username, String password, String answer) {
        if (username == null || password == null || answer == null) return null;

        try {
            if (!auth.login(username, password, answer)) return null;
        } catch (Exception e) {
            return null;
        }

        Account acc = accounts.get(username.toLowerCase());

        if (acc != null && acc.login()) {
            return acc;
        }

        return null;
    }

    /**
     * Ends the active session for the given user.
     * @param username the username to log out
     * @return true if the user was logged in and is now logged out,
     *         false if the user was not found or not logged in
     */
    public boolean logout(String username) {
        if (username == null) return false;
        Account acc = accounts.get(username.toLowerCase());    
        if (acc != null && acc.isLoggedIn()) {
            acc.logout();
            return true;
    }
    return false;
    }

    /**
     * Deletes an account after checking through {@link Authentication}.     *
     *
     * @param username the username of the account to delete
     * @param password the user's password
     * @param answer   the answer to the security question
     * @return true if the account was successfully deleted, false otherwise
     */
    public boolean deleteAccount(String username, String password, String answer) {
        if (username == null || password == null || answer == null) return false;

        try {
            if (!auth.login(username, password, answer)) return false;
        } catch (Exception e) {
            return false;
        }

        String key = username.toLowerCase();
        Account acc = accounts.get(key);

        if (acc != null) {
            acc.logout();
        }
 
        accounts.remove(key);
        return true;

    }


}