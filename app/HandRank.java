package app;

import java.util.*;

public enum HandRank {
    HIGH_CARD, ONE_PAIR, TWO_PAIR, THREE_OF_A_KIND, STRAIGHT,
    FLUSH, FULL_HOUSE, FOUR_OF_A_KIND, STRAIGHT_FLUSH, ROYAL_FLUSH;

    public static HandRank evaluate(List<Card> fiveCards) {
        List<Card> sorted = new ArrayList<>(fiveCards);
        sorted.sort((c1, c2) -> Integer.compare(getRankValue(c2.getRank()), getRankValue(c1.getRank())));

        boolean isFlush = isFlush(sorted);
        boolean isStraight = isStraight(sorted);

        if (isFlush && isStraight) {
            if (getRankValue(sorted.get(0).getRank()) == 14 && getRankValue(sorted.get(4).getRank()) == 10)
                return ROYAL_FLUSH;
            return STRAIGHT_FLUSH;
        }
        if (isFourOfAKind(sorted)) return FOUR_OF_A_KIND;
        if (isFullHouse(sorted)) return FULL_HOUSE;
        if (isFlush) return FLUSH;
        if (isStraight) return STRAIGHT;
        if (isThreeOfAKind(sorted)) return THREE_OF_A_KIND;
        int pairCount = countPairs(sorted);
        if (pairCount == 2) return TWO_PAIR;
        if (pairCount == 1) return ONE_PAIR;
        return HIGH_CARD;
    }

    private static int getRankValue(Card.Rank rank) {
        switch (rank) {
            case TWO: return 2; case THREE: return 3; case FOUR: return 4;
            case FIVE: return 5; case SIX: return 6; case SEVEN: return 7;
            case EIGHT: return 8; case NINE: return 9; case TEN: return 10;
            case JACK: return 11; case QUEEN: return 12; case KING: return 13;
            case ACE: return 14;
            default: throw new IllegalArgumentException();
        }
    }

    private static boolean isFlush(List<Card> cards) {
        Card.Suit first = cards.get(0).getSuit();
        return cards.stream().allMatch(c -> c.getSuit() == first);
    }

    private static boolean isStraight(List<Card> cards) {
        Set<Integer> values = new TreeSet<>(Collections.reverseOrder());
        for (Card c : cards) values.add(getRankValue(c.getRank()));
        List<Integer> list = new ArrayList<>(values);
        if (list.size() < 5) return false;

        // normal straight
        boolean straight = true;
        for (int i = 0; i < list.size() - 1; i++) {
            if (list.get(i) - list.get(i + 1) != 1) { straight = false; break; }
        }
        if (straight) return true;

        // Ace-low straight
        return list.contains(14) && list.contains(2) && list.contains(3) && list.contains(4) && list.contains(5);
    }

    private static boolean isFourOfAKind(List<Card> cards) {
        return getRankFrequency(cards).containsValue(4);
    }

    private static boolean isFullHouse(List<Card> cards) {
        Map<Integer, Integer> freq = getRankFrequency(cards);
        return freq.containsValue(3) && freq.containsValue(2);
    }

    private static boolean isThreeOfAKind(List<Card> cards) {
        Map<Integer, Integer> freq = getRankFrequency(cards);
        return freq.containsValue(3) && !freq.containsValue(2);
    }

    private static int countPairs(List<Card> cards) {
        return (int) getRankFrequency(cards).values().stream().filter(v -> v == 2).count();
    }

    private static Map<Integer, Integer> getRankFrequency(List<Card> cards) {
        Map<Integer, Integer> freq = new HashMap<>();
        for (Card c : cards) {
            int val = getRankValue(c.getRank());
            freq.put(val, freq.getOrDefault(val, 0) + 1);
        }
        return freq;
    }
}