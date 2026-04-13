package app;
import java.util.*;

public class AccountManager {
    //CWE-283: association of user accounts with player profiles
    private final Map<String, Account> accounts = new HashMap<>();
    //CWE-770: allocation of resources without limits or throttling
    private final int maxAccounts = 100; 
    private int nextPlayerId = 1;


    //CWE-708: incorrect ownership assignment
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

    Player newPlayer = new Player(nextPlayerId++, username, startingChips);
    accounts.put(key, new Account(username, password, answerSec, newPlayer));
    return true;
    }

    public Account login(String username, String password) {
        if (username == null || password == null) return null;
        Account acc = accounts.get(username.toLowerCase());        
        if (acc != null && acc.login(password)) return acc;
        return null;
    }

    public boolean logout(String username) {
        if (username == null) return false;
        Account acc = accounts.get(username.toLowerCase());    
        if (acc != null && acc.isLoggedIn()) {
        acc.logout();
        return true;
    }
    return false;
    }

    public boolean deleteAccount(String username, String password) {
        if (username == null || password == null) return false;
        Account acc = accounts.get(username.toLowerCase());        
        if (acc != null && acc.checkPassword(password)) {
        accounts.remove(username.toLowerCase());
        return true;
        }
        //failed
        return false;
    }

    public boolean recoverPassword(String username, String answerSec, String newPassword) {
    if (username == null || answerSec == null || newPassword == null || newPassword.isBlank()) return false;

    Account acc = accounts.get(username.toLowerCase());

    if (acc != null && acc.passwordRecovery(answerSec)) {
        return acc.setPassword(newPassword);
    }

    return false;
    }

    //show all accounts
    public List<Account> getAllAccounts() {
        return new ArrayList<>(accounts.values());
    }
}