package app;

public class Player {

    public enum PokerAction { FOLD, CHECK, CALL, RAISE, ALL_IN }
    private final int id;
    private final String name;
    private double chips;
    private Hand hand;
    private double currentBet;
    private boolean folded;
    private boolean allIn;
    private int seatPosition;

    public Player(int id, String name, double startingChips) {
        this.id = id;
        this.name = name;
        this.chips = startingChips;
        this.currentBet = 0;
        this.folded = false;
        this.allIn = false;
    }

    // --- Betting & state ---
    public boolean placeBet(double amount) {
        if (amount <= 0 || amount > chips) return false;
        chips -= amount;
        currentBet += amount;
        return true;
    }
    public void addWinnings(double amount) { chips += amount; }
    public void fold() { folded = true; }
    public void allIn() { allIn = true; }
    public void resetForNewRound() {
        hand = null;
        currentBet = 0;
        folded = false;
        allIn = false;
    }
    public boolean isFolded() { return folded; }
    public boolean isAllIn() { return allIn; }
    public double getCurrentBet() { return currentBet; }

    // --- Getters/setters ---
    public int getId() { return id; }
    public String getName() { return name; }
    public double getChips() { return chips; }
    public Hand getHand() { return hand; }
    public void setHand(Hand hand) { this.hand = hand; }
    public int getSeatPosition() { return seatPosition; }
    public void setSeatPosition(int seat) { this.seatPosition = seat; }
    @Override public String toString() { return name + " (" + chips + " chips)"; }
}