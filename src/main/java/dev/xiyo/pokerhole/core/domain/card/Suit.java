package dev.xiyo.pokerhole.core.domain.card;

public enum Suit {
    CLUBS("♣️"),
    DIAMONDS("♦️"),
    HEARTS("♥️"),
    SPADES("♠️");

    private final String symbol;

    Suit(String symbol) {
        this.symbol = symbol;
    }

    @Override
    public String toString() {
        return symbol;
    }
}

