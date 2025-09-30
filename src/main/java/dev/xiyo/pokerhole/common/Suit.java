package dev.xiyo.pokerhole.common;

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

