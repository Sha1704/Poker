package app;
import java.util.Arrays;
import java.util.Objects;

/**
 * Represents a playing card with a rank and suit.
 * The class includes enums for standard ranks and suits, and provides methods
 * to access card attributes and compare arrays of cards safely.
 */

public class Card {

    //The four suits in a standard deck of cards.
    public enum Suit {
        HEARTS, DIAMONDS, CLUBS, SPADES
    }

    //The ranks in a standard deck of cards, with associated values for Blackjack.
    public enum Rank {
        TWO(2), THREE(3), FOUR(4), FIVE(5), SIX(6), SEVEN(7), EIGHT(8), NINE(9), TEN(10),
        JACK(10), QUEEN(10), KING(10), ACE(11);

        private final int value;
        
        /**
         * Constructs a rank with its numeric value.
         * @param value the numeric value of the rank
         */
        Rank(int value) { 
            this.value = value; 
        }

        public int getValue() { 
            return value; 
        }
    }

    private final Rank rank;
    private final Suit suit;

    /**
     * Constructs a new Card with the given rank and suit.
     * MET50-J: Single clear constructor avoids ambiguous overload
     * @param rank the rank of the card
     * @param suit the suit of the card 
     * @throws IllegalArgumentException if either rank or suit is null
     */    
    public Card(Rank rank, Suit suit) {
        if (rank == null || suit == null) {
            throw new IllegalArgumentException("Rank and Suit cannot be null");
        }
        this.rank = rank;
        this.suit = suit;
    }

    //getters
    public Suit getSuit() { 
        return suit; 
    }

    public Rank getRank() { 
        return rank; 
    }

    public int getValue() { 
        return rank.getValue(); 
    }

    @Override
    public String toString() {
        return rank + " of " + suit;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Card)) return false;
        Card other = (Card) obj;
        return rank == other.rank && suit == other.suit;
    }

    @Override
    public int hashCode() {
        return Objects.hash(rank, suit);
    }

    /**
     * Compares two arrays of cards for equality 
     * EXP02-J: Compare arrays correctly
     * @param a the first array
     * @param b the second array
     * @return true if the arrays are equal, false otherwise
     */
    public static boolean compareCards(Card[] a, Card[] b) {
        return Arrays.equals(a, b);
    }

    /**
     * OBJ14-J: Do not use an object that has been freed.
     * A static inner class simulates a resource that must be checked before use.
     */
    static class Resource {
        private boolean open = true;

        //uses the resource if it is still open, otherwise throws an exception
        public void use() {
            if (!open) {
                throw new IllegalStateException("Resource has been freed");
            }
            System.out.println("Resource in use");
        }

        //frees the resource and marks it as no longer usable
        public void free() {
            open = false;
            System.out.println("Resource freed");
        }
    }
}