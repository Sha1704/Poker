package app;

import java.util.*;

public class Dealer {
    private final Deck deck;
    private final List<Player> players;
    private List<Card> communityCards;
    private double pot;
    private int dealerButtonPos;
    private double currentBetToMatch;
    private String gameState; // PRE_FLOP, FLOP, TURN, RIVER, SHOWDOWN, FINISHED
    private int currentPlayerIndex;   // index in players list of who is to act
    private boolean waitingForAction; // true when expecting player action
    private Player currentPlayer;     // the player who must act now
    private double minRaise;          // minimum raise amount (for UI)

    public Dealer(Deck deck) {
        this.deck = Objects.requireNonNull(deck);
        this.players = new ArrayList<>();
        this.communityCards = new ArrayList<>();
        this.pot = 0;
        this.dealerButtonPos = 0;
        this.currentBetToMatch = 0;
        this.gameState = "WAITING";
        this.waitingForAction = false;
    }

    public void addPlayer(Player p) { players.add(p); }
    public void removePlayer(Player p) { players.remove(p); }
    public List<Player> getAllPlayers() { return new ArrayList<>(players); }
    public List<Card> getCommunityCards() { return new ArrayList<>(communityCards); }
    public String getGameState() { return gameState; }
    public boolean isWaitingForAction() { return waitingForAction; }
    public Player getCurrentPlayer() { return currentPlayer; }
    public double getCurrentBetToMatch() { return currentBetToMatch; }
    public double getPot() { return pot; }
    public double getMinRaise() { return minRaise; }

    // Call this to start a new hand
    public void startHand() {
        resetForNewHand();
        if (players.size() < 2) {
            gameState = "FINISHED";
            return;
        }
        // Rotate dealer button
        dealerButtonPos = (dealerButtonPos + 1) % players.size();

        // Post blinds (example 5/10)
        Player sb = players.get((dealerButtonPos + 1) % players.size());
        Player bb = players.get((dealerButtonPos + 2) % players.size());
        postBlind(sb, 5);
        postBlind(bb, 10);
        currentBetToMatch = 10;
        minRaise = 10; // minimum raise = big blind

        // Deal hole cards
        for (Player p : players) {
            Hand h = new Hand();
            h.addCard(deck.drawCard());
            h.addCard(deck.drawCard());
            p.setHand(h);
            p.resetForNewRound();
        }

        gameState = "PRE_FLOP";
        startBettingRound();
    }

    private void resetForNewHand() {
        communityCards.clear();
        pot = 0;
        currentBetToMatch = 0;
        deck.shuffle();
        waitingForAction = false;
        currentPlayer = null;
    }

    private void postBlind(Player p, double amount) {
        if (p.getChips() >= amount) {
            p.placeBet(amount);
            pot += amount;
        } else {
            p.placeBet(p.getChips());
            pot += p.getChips();
            p.allIn();
        }
    }

    private void startBettingRound() {
        // Determine first player to act based on game state
        int startIdx;
        if (gameState.equals("PRE_FLOP")) {
            startIdx = (dealerButtonPos + 3) % players.size(); // after blinds
        } else {
            startIdx = (dealerButtonPos + 1) % players.size(); // first after button
        }
        currentPlayerIndex = startIdx;
        advanceToNextActivePlayer();
        waitingForAction = true;
    }

    // Called by main when player has made a decision
    public void submitAction(Player.PokerAction action, double raiseAmount) {
        if (!waitingForAction || currentPlayer == null) return;
        waitingForAction = false;

        // Store the index of the player who acted (in case of raise)
        int actingIndex = currentPlayerIndex;

        boolean wasRaise = processAction(currentPlayer, action, raiseAmount);

        if (wasRaise) {
            // After a raise, the next player to act is the one after the raiser
            currentPlayerIndex = (actingIndex + 1) % players.size();
            advanceToNextActivePlayer();
        } else {
            // Normal move to next player
            currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
            advanceToNextActivePlayer();
        }

        // Check if betting round is complete
        if (isBettingRoundComplete()) {
            // Move to next street
            if (!advanceToNextStreet()) {
                gameState = "FINISHED";
                return;
            }
            // Start a new betting round
            startBettingRound();
        } else {
            waitingForAction = true; // next player's turn
        }
    }

    private boolean processAction(Player p, Player.PokerAction action, double raiseAmount) {
        double toCall = currentBetToMatch - p.getCurrentBet();
        switch (action) {
            case FOLD:
                p.fold();
                System.out.println(p.getName() + " folds");
                return false;
            case CHECK:
                if (toCall == 0) {
                    System.out.println(p.getName() + " checks");
                    return false;
                } else {
                    // illegal check – treat as call
                    return processAction(p, Player.PokerAction.CALL, 0);
                }
            case CALL:
                if (toCall <= 0) return false;
                if (p.placeBet(toCall)) {
                    pot += toCall;
                    System.out.println(p.getName() + " calls " + toCall);
                    return false;
                } else {
                    double allIn = p.getChips();
                    p.placeBet(allIn);
                    pot += allIn;
                    p.allIn();
                    System.out.println(p.getName() + " calls all-in with " + allIn);
                    return false;
                }
            case RAISE:
                double total = currentBetToMatch + raiseAmount;
                double needed = total - p.getCurrentBet();
                if (needed > 0 && p.placeBet(needed)) {
                    pot += needed;
                    currentBetToMatch = total;
                    minRaise = raiseAmount;
                    System.out.println(p.getName() + " raises to " + total);
                    return true; // indicates a raise occurred
                } else {
                    return processAction(p, Player.PokerAction.ALL_IN, 0);
                }
            case ALL_IN:
                double allInChips = p.getChips();
                if (allInChips > 0) {
                    p.placeBet(allInChips);
                    pot += allInChips;
                    p.allIn();
                    System.out.println(p.getName() + " goes all-in with " + allInChips);
                    if (p.getCurrentBet() > currentBetToMatch) {
                        currentBetToMatch = p.getCurrentBet();
                        return true; // all-in that increases the bet counts as a raise
                    }
                }
                return false;
            default:
                return false;
        }
    }

    private void advanceToNextActivePlayer() {
        int start = currentPlayerIndex;
        while (true) {
            Player p = players.get(currentPlayerIndex);
            if (!p.isFolded() && !p.isAllIn()) {
                currentPlayer = p;
                return;
            }
            currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
            if (currentPlayerIndex == start) break; // no active players
        }
        currentPlayer = null;
    }

    private boolean isBettingRoundComplete() {
        // All players either folded, all-in, or have currentBet == currentBetToMatch
        for (Player p : players) {
            if (!p.isFolded() && !p.isAllIn() && p.getCurrentBet() < currentBetToMatch) {
                return false;
            }
        }
        return true;
    }

    private boolean advanceToNextStreet() {
        switch (gameState) {
            case "PRE_FLOP":
                dealFlop();
                gameState = "FLOP";
                return true;
            case "FLOP":
                dealTurn();
                gameState = "TURN";
                return true;
            case "TURN":
                dealRiver();
                gameState = "RIVER";
                return true;
            case "RIVER":
                showdown();
                gameState = "SHOWDOWN";
                return false; // game ends after showdown
            default:
                return false;
        }
    }

    private void dealFlop() {
        deck.drawCard(); // burn
        communityCards.add(deck.drawCard());
        communityCards.add(deck.drawCard());
        communityCards.add(deck.drawCard());
        System.out.println("Flop: " + communityCards);
    }

    private void dealTurn() {
        deck.drawCard(); // burn
        communityCards.add(deck.drawCard());
        System.out.println("Turn: " + communityCards.get(3));
    }

    private void dealRiver() {
        deck.drawCard(); // burn
        communityCards.add(deck.drawCard());
        System.out.println("River: " + communityCards.get(4));
    }

    private void showdown() {
        List<Player> active = new ArrayList<>();
        for (Player p : players) if (!p.isFolded()) active.add(p);
        if (active.isEmpty()) return;

        // Evaluate hands
        for (Player p : active) p.getHand().evaluate(communityCards);

        // Build side pots
        List<Pot> pots = createSidePots(active);
        for (Pot potObj : pots) {
            List<Player> eligible = potObj.eligible;
            Player winner = eligible.get(0);
            for (int i = 1; i < eligible.size(); i++) {
                if (eligible.get(i).getHand().compareTo(winner.getHand(), communityCards) > 0)
                    winner = eligible.get(i);
            }
            // Ties
            List<Player> ties = new ArrayList<>();
            for (Player p : eligible) {
                if (p.getHand().compareTo(winner.getHand(), communityCards) == 0)
                    ties.add(p);
            }
            double share = potObj.amount / ties.size();
            for (Player p : ties) {
                p.addWinnings(share);
                System.out.println(p.getName() + " wins " + share + " from pot");
            }
        }
    }

    private List<Pot> createSidePots(List<Player> active) {
        List<Player> sorted = new ArrayList<>(active);
        sorted.sort(Comparator.comparingDouble(Player::getCurrentBet));
        List<Pot> pots = new ArrayList<>();
        double lastBet = 0;
        for (int i = 0; i < sorted.size(); i++) {
            Player p = sorted.get(i);
            double bet = p.getCurrentBet();
            if (bet > lastBet) {
                double contrib = bet - lastBet;
                double size = contrib * (sorted.size() - i);
                List<Player> eligible = new ArrayList<>(sorted.subList(i, sorted.size()));
                pots.add(new Pot(size, eligible));
                lastBet = bet;
            }
        }
        return pots;
    }

    private static class Pot {
        double amount;
        List<Player> eligible;
        Pot(double amount, List<Player> eligible) { this.amount = amount; this.eligible = eligible; }
    }
}