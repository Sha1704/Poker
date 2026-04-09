package app;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== Texas Hold'em Poker ===\n");

        System.out.print("Enter your name: ");
        String playerName = scanner.nextLine().trim();
        if (playerName.isEmpty()) {
            playerName = "Player";
        }

        System.out.print("Enter number of players (2-6): ");
        int numPlayers = 2; //CWE-182
        try {
            numPlayers = Integer.parseInt(scanner.nextLine().trim());
            numPlayers = Math.max(2, Math.min(6, numPlayers)); //CWE-229
        } catch (NumberFormatException e) {
            numPlayers = 2;
        }

        Deck deck = new Deck();
        Dealer dealer = new Dealer(deck);

        Player human = new Player(0, playerName, 1000.0);
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

            if (human.getChips() <= 0) {
                System.out.println("\nYou're out of chips! Game over.");
                break;
            }

            System.out.print("\nPlay another hand? (y/n): ");
            String response = scanner.nextLine().trim().toLowerCase(); //CWE-178
            if (!response.equals("y") && !response.equals("yes")) {
                keepPlaying = false;
            }
        }

        System.out.println("\nThanks for playing!");
        scanner.close();
    }

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
            String input = scanner.nextLine().trim().toLowerCase(); //CWE-178

            switch (input) {
                case "fold":
                case "f":
                    return Player.PokerAction.FOLD;
                case "check":
                case "x":
                    if (canCheck) { //CWE-233
                        return Player.PokerAction.CHECK;
                    }
                    System.out.println("Cannot check, there is a bet to call.");
                    break;
                case "call":
                case "c":
                    return Player.PokerAction.CALL;
                case "raise":
                case "r":
                    return Player.PokerAction.RAISE;
                case "all-in":
                case "allin":
                case "ai":
                    return Player.PokerAction.ALL_IN;
                default:
                    System.out.println("Invalid input. Try again.");
            }
        }
    }

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
}
