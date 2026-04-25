package app;

import java.util.*;

public class Main {

    /**
     * Starts and runs an interactive Texas Hold'em console game session.
     *
     * The method prompts for the human player's name and the total number of players, initializes the deck,
     * dealer, one human player and the specified number of bot players, then enters a hand loop that
     * drives dealing, player and bot actions, showdowns, and chip accounting until the human is out of chips
     * or the user chooses to stop. All input and game progress are performed via standard input/output.
     */
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        AccountManager manager = new AccountManager();

        // NEW: separate sign‑in method
        Account loggedIn = authenticateUser(scanner, manager);

        if (loggedIn == null) {
            System.out.println("Goodbye.");
            scanner.close();
            return;
        }

        System.out.println("=== Texas Hold'em Poker ===\n");

        System.out.print("Welcome, " + loggedIn.getUsername() + "!\n");

        System.out.print("Enter number of players (2-6): ");
        int numPlayers = 2; //CWE-182
        try {
            numPlayers = Integer.parseInt(scanner.nextLine().trim());
            numPlayers = Math.max(2, Math.min(6, numPlayers)); //CWE-229
        } catch (NumberFormatException e) {
            PokerLogger.logError("Failed to parse number of players ", e);
            numPlayers = 2;
        }

        Deck deck = new Deck();
        Dealer dealer = new Dealer(deck);

        Player human = loggedIn.getPlayer();
        dealer.addPlayer(human);

        for (int i = 1; i < numPlayers; i++) {
            dealer.addPlayer(new Player(i, "Bot " + i, 1000.0));
        }

        System.out.println("\nGame started! Each player begins with 1000 chips.");
        System.out.println("Blinds: 5/10\n");

        boolean keepPlaying = true;
        int handNumber = 0;

        while (keepPlaying) {
            handNumber++;
            System.out.println("=== Hand #" + handNumber + " ===\n");

            dealer.startHand();

            if (dealer.getGameState().equals("FINISHED")) {
                System.out.println("Not enough players to continue.");
                break;
            }

            printGameState(dealer, human);

            while (!dealer.getGameState().equals("SHOWDOWN")
                    && !dealer.getGameState().equals("FINISHED")) {

                if (dealer.isWaitingForAction()) {
                    Player current = dealer.getCurrentPlayer();
                    if (current == null) {
                        break;
                    }

                    if (current == human) {
                        Player.PokerAction action = getPlayerAction(scanner, dealer, human);
                        double raiseAmount = 0;
                        if (action == Player.PokerAction.RAISE) {
                            raiseAmount = getRaiseAmount(scanner, dealer);
                        }
                        dealer.submitAction(action, raiseAmount);
                    } else {
                        Player.PokerAction botAction = getBotAction(dealer, current);
                        double raiseAmount = 0;
                        if (botAction == Player.PokerAction.RAISE) {
                            raiseAmount = dealer.getMinRaise();
                        }
                        dealer.submitAction(botAction, raiseAmount);
                    }

                    printGameState(dealer, human);
                } else {
                    break;
                }
            }

            if (dealer.getGameState().equals("SHOWDOWN")) {
                System.out.println("\n=== Showdown ===");
                for (Player p : dealer.getAllPlayers()) {
                    if (!p.isFolded() && p.getHand() != null) {
                        System.out.print(p.getName() + ": ");
                        for (Card c : p.getHand().getHoleCards()) {
                            System.out.print(c + " | ");
                        }
                        if (p.getHand().getBestFive() != null) {
                            System.out.print("Best hand: ");
                            for (Card c : p.getHand().getBestFive()) {
                                System.out.print(c + " | ");
                            }
                            System.out.println("(" + p.getHand().getRank() + ")");
                        } else {
                            System.out.println();
                        }
                    } else if (p.isFolded()) {
                        System.out.println(p.getName() + ": folded");
                    }
                }
            }

            System.out.println("\n=== Chip Counts ===");
            for (Player p : dealer.getAllPlayers()) {
                System.out.println(p.getName() + ": " + p.getChips() + " chips");
            }

            dealer.getAllPlayers().stream()
                .filter(p -> p != human && p.getChips() <= 0)
                .forEach(dealer::removePlayer);

            long fundedPlayers = dealer.getAllPlayers().stream()
                .filter(p -> p.getChips() > 0)
                .count();

            if (fundedPlayers < 2) {
                System.out.println("\nOnly one player has chips remaining. Game over.");
                break;
            }

            if (human.getChips() <= 0) {
                System.out.println("\nYou're out of chips! Game over.");
                break;
            }

            System.out.print("\nPlay another hand? (y/n): ");
            String response = scanner.nextLine().trim().toLowerCase(Locale.ROOT); //CWE-178
            if (!response.equals("y") && !response.equals("yes")) {
                keepPlaying = false;
            }
        }
        loggedIn.logout(); // Ensure logout on exit
    
        System.out.println("\nThanks for playing!");
        scanner.close();
    }

    /**
     * Prompt the human player for an in-game poker action and return the selected action.
     *
     * The method prints the human's hole cards, chips, current pot and (when applicable) the amount
     * required to call, then repeatedly reads input until a valid action is entered. It accepts
     * common aliases for actions and enforces that "check" is only allowed when there is nothing to call.
     *
     * @param scanner the Scanner to read player input from
     * @param dealer  the Dealer used to obtain pot and bet information
     * @param human   the human Player whose hand and chip information are displayed
     * @return        the chosen Player.PokerAction: `FOLD`, `CHECK`, `CALL`, `RAISE`, or `ALL_IN`
     */
    private static Player.PokerAction getPlayerAction(Scanner scanner, Dealer dealer, Player human) {
        double toCall = dealer.getCurrentBetToMatch() - human.getCurrentBet();
        boolean canCheck = toCall == 0;

        System.out.println("\n--- Your Turn ---");
        System.out.println("Your hand: " + human.getHand().getHoleCards().get(0)
                + " | " + human.getHand().getHoleCards().get(1)); //CWE-130
        System.out.println("Your chips: " + human.getChips());
        if (!canCheck) {
            System.out.println("Amount to call: " + toCall);
        }
        System.out.println("Pot: " + dealer.getPot());

        while (true) {
            System.out.print("Action (fold/check/call/raise/all-in): ");
            String input = scanner.nextLine().trim().toLowerCase(Locale.ROOT); //CWE-178

            switch (input) {
                case "fold", "f" -> {
                    return Player.PokerAction.FOLD;
                }
                case "check", "x" -> {
                    if (canCheck) { //CWE-233
                        return Player.PokerAction.CHECK;
                    }
                    System.out.println("Cannot check, there is a bet to call.");
                }
                case "call", "c" -> {
                    return Player.PokerAction.CALL;
                }
                case "raise", "r" -> {
                    return Player.PokerAction.RAISE;
                }
                case "all-in", "allin", "ai" -> {
                    return Player.PokerAction.ALL_IN;
                }
                default -> System.out.println("Invalid input. Try again.");
            }
        }
    }

    /**
     * Prompt the user for a raise amount and return it clamped between the dealer's minimum and maximum allowed raise.
     *
     * Reads a line from {@code scanner}, parses it as a double, and constrains the result to the range
     * [minRaise, maxRaise] computed from {@code dealer}. If parsing fails, {@code minRaise} is returned.
     *
     * @param scanner source of user input
     * @param dealer  provides the minimum raise and current-player chip/state used to compute the maximum
     * @return the chosen raise amount constrained to the dealer's allowed range; returns `minRaise` on invalid input
     */
    private static double getRaiseAmount(Scanner scanner, Dealer dealer) {
        double minRaise = dealer.getMinRaise();
        double maxRaise = dealer.getCurrentPlayer().getChips()
                - (dealer.getCurrentBetToMatch() - dealer.getCurrentPlayer().getCurrentBet());

        System.out.print("Raise amount (min " + minRaise + ", max " + maxRaise + "): ");
        try {
            double amount = Double.parseDouble(scanner.nextLine().trim());
            amount = Math.max(minRaise, Math.min(maxRaise, amount)); //CWE-229
            return amount;
        } catch (NumberFormatException e) {
            return minRaise; //CWE-182
        }
    }

    /**
     * Chooses a bot player's poker action using simple probability-based heuristics.
     *
     * When the bot can check, it chooses CHECK with ~70% probability and RAISE otherwise.
     * When the bot must call (cannot check), it chooses CALL with ~80% probability,
     * RAISE with ~30% probability (evaluated after the CALL check), and FOLD otherwise.
     *
     * @param dealer the current game dealer providing pot and bet information
     * @param bot the bot player for whom the action is being selected
     * @return the selected {@code Player.PokerAction} for the bot
     */
    private static Player.PokerAction getBotAction(Dealer dealer, Player bot) {
        double toCall = dealer.getCurrentBetToMatch() - bot.getCurrentBet();
        boolean canCheck = toCall == 0;

        if (canCheck) {
            return Math.random() < 0.7 ? Player.PokerAction.CHECK : Player.PokerAction.RAISE;
        }

        double potOdds = toCall / (dealer.getPot() + toCall);
        if (Math.random() < 0.8) {
            return Player.PokerAction.CALL;
        } else if (Math.random() < 0.3) {
            return Player.PokerAction.RAISE;
        } else {
            return Player.PokerAction.FOLD;
        }
    }

    /**
     * Prints the dealer's current game state, community cards, pot, and, when applicable, which player is to act.
     *
     * @param dealer the Dealer whose state and table information will be displayed
     * @param human the local human Player; used to determine and highlight when it is the human's turn
     */
    private static void printGameState(Dealer dealer, Player human) {
        System.out.println("\n--- " + dealer.getGameState() + " ---");
        System.out.println("Community cards: " + dealer.getCommunityCards());
        System.out.println("Pot: " + dealer.getPot());

        Player current = dealer.getCurrentPlayer();
        if (current != null && dealer.isWaitingForAction()) {
            if (current == human) {
                System.out.println(">> It's YOUR turn to act!");
            } else {
                System.out.println(">> " + current.getName() + " is thinking...");
            }
        }
    }
   /**
     * Authenticates a user by prompting for login credentials.
     *
     * @param scanner source of user input
     * @param manager the account manager for handling authentication logic
     * @return the authenticated account or null if authentication fails
     */
    private static Account authenticateUser(Scanner scanner, AccountManager manager) {

        Account loggedIn = null;

        System.out.println("=== Poker Account System ===");

        while (loggedIn == null) {
            System.out.println("\n1. Login");
            System.out.println("2. Register");
            System.out.print("> ");

            String choice = scanner.nextLine().trim();

            switch (choice) {

                case "1": {
                    System.out.print("Username: ");
                    String u = scanner.nextLine();

                    System.out.print("Password: ");
                    String p = scanner.nextLine();

                    System.out.print("Security answer: ");
                    String a = scanner.nextLine();

                    loggedIn = manager.login(u, p,a);

                    if (loggedIn == null) {
                        System.out.println("Login failed.");
                    } else {
                        System.out.println("Login successful. Welcome, " + loggedIn.getUsername());
                    }
                    break;
                }

                case "2": {
                    System.out.print("Choose username: ");
                    String u = scanner.nextLine();

                    System.out.print("Choose password: ");
                    String p = scanner.nextLine();

                    System.out.print("Security answer: ");
                    String a = scanner.nextLine();

                    System.out.print("Starting chips: ");
                    double chips = safeDouble(scanner.nextLine());

                    boolean ok = manager.register(u, p, a, chips);
                    System.out.println(ok ? "Registration successful." : "Registration failed.");
                    break;
                }

                case "3":
                    return null;

                default:
                    System.out.println("Invalid option.");
            }
        }

        return loggedIn;
    }

/**
 * Safely parses a double from a string, returning NaN on failure.
 * @param input the string to parse
 * @return the parsed double or NaN if parsing fails
 */
    private static double safeDouble(String input) {
        try {
            return Double.parseDouble(input.trim());
        } catch (Exception e) {
            PokerLogger.logError("Failed to parse double from input " , e);
            return Double.NaN;
        }
    
    }   
}

