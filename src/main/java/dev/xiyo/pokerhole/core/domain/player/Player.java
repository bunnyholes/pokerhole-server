package dev.xiyo.pokerhole.core.domain.player;

import dev.xiyo.pokerhole.core.domain.card.*;

import java.util.*;

public class Player {
    private static final Set<String> nickNames = new HashSet<>(); // 닉네임 중복 확인을 위한 데이터 저장소

    private final String nickName; // 이름
    private final Hand hand = new Hand();
    private int point = 10_000; // 총 획득 포인트

    private final PlayerRecord playerRecord =  new PlayerRecord(); // 전적

    public static Player newPlayer(String nickName) {
        return new Player(nickName);
    }

    protected Player(String nickName) {
        // 규칙4, 닉네임은 고유해야한다.
        if (nickNames.contains(nickName))
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");

        // 규칙4, 닉네임의 길이는 20자를 넘기지 못한다.
        if (nickName.length() > 20)
            throw new IllegalArgumentException("닉네임은 20자 이하여야 합니다.");

        this.nickName = nickName;
        nickNames.add(nickName); // 중복 데이터에 등록
    }

    public String getNickName() {
        return nickName;
    }

    public Hand getHand() {
        return hand;
    }

    public int getPoints() {
        return point;
    }

    public int getPoint() {
        return point;
    }





    public int getWins() {
        return this.playerRecord.getWins();
    }

    public int getLosses() {
        return this.playerRecord.getLosses();
    }

    public void win() {
        this.playerRecord.incrementWins();
    }

    public void lose() {
        this.playerRecord.incrementLosses();
    }

    public void prizePoint(int prize) {
        this.point += prize;
    }

    private boolean isHandOpen = false;
    public void openHand() {
        hand.open();
        isHandOpen = true;
    }

    public void receiveCard(Card card) {
        hand.add(card);
    }

    public void dropHand() {
        hand.clear();
    }

    // 1. 승리 횟수 기준 내림차순, 패배 횟수 오름차순, 닉네임 오름차순 정렬 Comparator
    public static final Comparator<Player> WIN_COUNT_ORDER = new WinCountComparator();

    public void draw() {
        this.playerRecord.incrementDraws();
    }

    public int getDraws() {
        return this.playerRecord.getDraws();
    }

    private static class WinCountComparator implements Comparator<Player> {
        @Override
        public int compare(Player p1, Player p2) {
            int winsCompare = Integer.compare(p2.getWins(), p1.getWins()); // 승리 내림차순
            int loseCompare = Integer.compare(p1.getLosses(), p2.getLosses()); // 패배 오름차순
            int nickNameCompare = String.CASE_INSENSITIVE_ORDER.compare(p1.nickName, p2.nickName); // 닉네임 오름차순

            if (winsCompare != 0) return winsCompare;
            if (loseCompare != 0) return loseCompare;
            return nickNameCompare;
        }
    }

    // 2. 핸드 기준 내림차순 정렬 Comparator
    public static final Comparator<Player> HAND_ORDER = new HandValueComparator();

    private static class HandValueComparator implements Comparator<Player> {
        @Override
        public int compare(Player p1, Player p2) {
            return p1.hand.compareTo(p2.hand); // 핸드 밸류 내림차순
        }
    }

    public String toString() {
        return nickName;
    }

    public void releaseNickname() {
        nickNames.remove(this.nickName);
    }

    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Player player = (Player) obj;
        return nickName.equals(player.nickName);
    }

    public int hashCode() {
        return Objects.hash(nickName);
    }
}
