package dev.xiyo.pokerhole.player;

class PlayerRecord {
    private int wins;
    private int losses;
    private int draw;

    public PlayerRecord() {
        this.wins = 0;
        this.losses = 0;
        this.draw = 0;
    }

    public int getWins() {
        return wins;
    }

    public int getLosses() {
        return losses;
    }

    public int getDraws() {
        return draw;
    }

    /**
     * 값의 증가는 같은 패키지에 있는 클래스에서만 가능하도록 제한합니다.
     */
    void incrementWins() {
        this.wins++;
    }

    /**
     * 값의 증가는 같은 패키지에 있는 클래스에서만 가능하도록 제한합니다.
     */
    void incrementLosses() {
        this.losses++;
    }

    public void incrementDraws() {
        this.draw++;
    }

    @Override
    public String toString() {
        return "Wins: " + wins + ", Losses: " + losses + ", Draws: " + draw;
    }

}