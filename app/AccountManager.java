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
    // username already taken
    if (accounts.containsKey(username)) return false; 
    // limit total accounts
    if (accounts.size() >= maxAccounts) return false; 

    // enforce limits on starting chips to prevent abuse (CWE-770)
    double minChips = 1;
    double maxChips = 5000;
    //check if starting chips is within min and max
    if (startingChips < minChips) startingChips = minChips;
    if (startingChips > maxChips) startingChips = maxChips;
    
    // create new player profile and account
    Player newPlayer = new Player(nextPlayerId++, username, startingChips);
    accounts.put(username, new Account(username, password, answerSec, newPlayer));
    return true;
    }

    public Account login(String username, String password) {
        Account acc = accounts.get(username);
        if (acc != null && acc.login(password)) return acc;
        return null;
    }

    public boolean deleteAccount(String username, String password) {
        Account acc = accounts.get(username);
        if (acc != null && acc.checkPassword(password)) {
            accounts.remove(username);
            return true;
        }
        //failed
        return false;
    }

    public boolean recoverPassword(String username, String answerSec) {
        Account acc = accounts.get(username);
        if (acc != null && acc.passwordRecovery(answerSec)) return true;
        //failed
        return false;
    }

    //show all accounts
    public List<Account> getAllAccounts() {
        return new ArrayList<>(accounts.values());
    }
}