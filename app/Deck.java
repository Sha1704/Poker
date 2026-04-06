package app;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom; 

/**
 * Deck class represents a standard deck of cards.
 * @author Maria Plascencia
 */
public class Deck {
    //Declaring variables
    private final Stack<Card> cards;

    /**
     * Constructor for a new deck of 52 cards, 
     * which is shuffled upon creation.
     */
    public Deck() {
        this.cards = new Stack<>();
        initializeDeck();
        shuffle();
    }

    /** 
     * Initialize the deck with 52 standard playing cards.
    */
    private void initializeDeck() {
        for (Card.Suit suit : Card.Suit.values()) {
            for (Card.Rank rank : Card.Rank.values()) {
                cards.push(new Card(rank, suit));
            }
        }
    }

    /**
     * Shuffle the deck using Collections.shuffle with a thread-safe random generator.
     */
    public void shuffle() {
        List<Card> cardList = new ArrayList<>(cards);
        Collections.shuffle(cardList, ThreadLocalRandom.current());
        cards.clear();
        cards.addAll(cardList);
    }

    /**
     * Draw a card from the top of the deck. 
     * If the deck is empty, it reinitializes and shuffles before drawing.
     * @return - the drawn card
     */
    public Card drawCard() {
        if (cards.isEmpty()) {
            initializeDeck();
            shuffle();
        }
        return cards.pop();
    }

    /**
     * Get the number of cards remaining in the deck.
     * @return - number of cards left in the deck
     */
    public int cardsRemaining() {
        return cards.size();
    }
}

