package app;

import java.util.*;

public class PokerHands {

    private List<Card> cards;
    public int countRank = 0;
    List<Integer> tiebreaker = new ArrayList<>();

    public PokerHands(List<Card> cards) {
        this.cards = cards;
    }

    //all 10 hands
    
    public boolean royalFlush() {
        return straightFlush() && highCard() == 14; 
    }

     public boolean straightFlush() {
        return flush() && straight();
    }

    public boolean fourOfAKind() {
        return getCounts().containsValue(4);
    }

    public boolean fullHouse() {
        Map<Integer, Integer> counts = getCounts();
        return counts.containsValue(3) && counts.containsValue(2);
    }

    public boolean flush() {
        Card.Suit suit = cards.get(0).getSuit();
        for (Card card : cards) {
            if (card.getSuit() != suit) {
                return false;
            }
        }
        return true;
    }

    public boolean straight() {
        List<Integer> values = new ArrayList<>();
        for (Card card : cards) {
            values.add(card.getValue());
        }

        Collections.sort(values);

        for (int i = 0; i < values.size() - 1; i++) {
            if (values.get(i) + 1 != values.get(i + 1)) {
                return false;
            }
        }
        return true;
    }

    public boolean threeOfAKind() {
        return getCounts().containsValue(3);
    }

    public boolean twoPair() {
        int pairCount = 0;
        for (int count : getCounts().values()) {
            if (count == 2) pairCount++;
        }
        return pairCount == 2;
    }

    public boolean pair() {
        int count = 0;
        for (int c : getCounts().values()) {
            if (c == 2) count++;
        }
        return count == 1;
    }

    public int highCard() {
        int max = 0;
        for (Card card : cards) {
            max = Math.max(max, card.getValue());
        }
        return max;
    }

    public void determineTiebreaker() {
        Map<Integer, Integer> counts = getCounts();

        List<Integer> sortedValues = new ArrayList<>(counts.keySet());

        sortedValues.sort((a, b) -> {
            int countCompare = counts.get(b).compareTo(counts.get(a));
            return countCompare != 0 ? countCompare : b.compareTo(a);
        });

        tiebreaker = new ArrayList<>();

        for (int val : sortedValues) {
            int count = counts.get(val);
            for (int i = 0; i < count; i++) {
                tiebreaker.add(val);
            }
        }
    }

    private Map<Integer, Integer> getCounts() {
        Map<Integer, Integer> valueCounts = new HashMap<>();
        for (Card card : cards) {
            valueCounts.put(card.getValue(),
                valueCounts.getOrDefault(card.getValue(), 0) + 1);
        }
        return valueCounts;
    }

    public void evaluateHand() {
        if (royalFlush()) {
            countRank = 10;
            System.out.println("Royal Flush");
        } else if (straightFlush()) {
            countRank = 9;
            System.out.println("Straight Flush");
        } else if (fourOfAKind()) {
            countRank = 8;
            System.out.println("Four of a Kind");
        } else if (fullHouse()) {
            countRank = 7;
            System.out.println("Full House");
        } else if (flush()) {
            countRank = 6;
            System.out.println("Flush");
        } else if (straight()) {
            countRank = 5;
            System.out.println("Straight");
        } else if (threeOfAKind()) {
            countRank = 4;
            System.out.println("Three of a Kind");
        } else if (twoPair()) {
            countRank = 3;
            System.out.println("Two Pair");
        } else if (pair()) {
            countRank = 2;
            System.out.println("Pair");
        } else {
            countRank = 1;
            System.out.println("High Card: " + highCard());
        }
        determineTiebreaker();
    }
}