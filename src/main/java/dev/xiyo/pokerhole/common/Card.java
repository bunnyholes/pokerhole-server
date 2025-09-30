package dev.xiyo.pokerhole.common;

import lombok.Getter;

import java.util.Objects;

/**
 * 카드 클래스는 카드 한 장을 나타내며, 무늬(Suit)와 숫자(Rank)를 가집니다.
 */
@Getter
public final class Card implements Comparable<Card> {
    private final Suit suit; // 카드의 무늬
    private final Rank rank; // 카드의 숫자

    public Card(Suit suit, Rank rank) {
        if (suit == null || rank == null) {
            throw new IllegalArgumentException("Suit과 Rank는 null이 될 수 없습니다");
        }
        this.suit = suit;
        this.rank = rank;
    }

    @Override
    public int compareTo(Card o) {
        int compare = Integer.compare(this.rank.ordinal(), o.rank.ordinal());
        if (compare == 0) {
            return Integer.compare(this.suit.ordinal(), o.suit.ordinal());
        }
        return compare;
    }

    @Override
    public String toString() {
        return this.suit + String.format("%2s", this.rank);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Card card = (Card) o;
        return suit == card.suit && rank == card.rank;
    }

    @Override
    public int hashCode() {
        return Objects.hash(suit, rank);
    }

}
