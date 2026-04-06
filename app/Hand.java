package app;

import java.util.*;
import java.util.stream.Collectors;

public class Hand {
    private final List<Card> holeCards;
    private HandRank rank;
    private List<Card> bestFive;

    public Hand() { holeCards = new ArrayList<>(); }
    public void addCard(Card card) { if (card != null && holeCards.size() < 2) holeCards.add(card); }
    public List<Card> getHoleCards() { return new ArrayList<>(holeCards); }

    public void evaluate(List<Card> communityCards) {
        if (communityCards == null || communityCards.size() != 5)
            throw new IllegalArgumentException("Need 5 community cards");
        List<Card> all = new ArrayList<>(holeCards);
        all.addAll(communityCards);
        bestFive = findBest(all);
        rank = HandRank.evaluate(bestFive);
    }

    private List<Card> findBest(List<Card> seven) {
        List<List<Card>> combs = new ArrayList<>();
        combine(seven, 0, new ArrayList<>(), combs);
        List<Card> best = null;
        HandRank bestRank = null;
        for (List<Card> five : combs) {
            HandRank r = HandRank.evaluate(five);
            if (bestRank == null || r.compareTo(bestRank) > 0) {
                bestRank = r;
                best = five;
            }
        }
        return best;
    }

    private void combine(List<Card> cards, int start, List<Card> temp, List<List<Card>> out) {
        if (temp.size() == 5) { out.add(new ArrayList<>(temp)); return; }
        for (int i = start; i < cards.size(); i++) {
            temp.add(cards.get(i));
            combine(cards, i + 1, temp, out);
            temp.remove(temp.size() - 1);
        }
    }

    public HandRank getRank() { return rank; }
    public List<Card> getBestFive() { return bestFive; }

    public int compareTo(Hand other, List<Card> community) {
        if (this.rank == null) this.evaluate(community);
        if (other.rank == null) other.evaluate(community);
        int cmp = this.rank.compareTo(other.rank);
        if (cmp != 0) return cmp;
        List<Integer> k1 = kickers(this.bestFive);
        List<Integer> k2 = kickers(other.bestFive);
        for (int i = 0; i < Math.min(k1.size(), k2.size()); i++)
            if (!k1.get(i).equals(k2.get(i))) return k1.get(i) - k2.get(i);
        return 0;
    }

    private List<Integer> kickers(List<Card> cards) {
        return cards.stream()
                .map(c -> rankValue(c.getRank()))
                .sorted(Collections.reverseOrder())
                .collect(Collectors.toList());
    }

    private int rankValue(Card.Rank r) {
        switch (r) {
            case TWO: return 2; case THREE: return 3; case FOUR: return 4;
            case FIVE: return 5; case SIX: return 6; case SEVEN: return 7;
            case EIGHT: return 8; case NINE: return 9; case TEN: return 10;
            case JACK: return 11; case QUEEN: return 12; case KING: return 13;
            case ACE: return 14;
            default: throw new IllegalArgumentException();
        }
    }
}