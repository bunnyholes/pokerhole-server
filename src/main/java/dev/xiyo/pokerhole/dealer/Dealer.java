package dev.xiyo.pokerhole.dealer;


import dev.xiyo.pokerhole.common.Card;
import dev.xiyo.pokerhole.player.Player;

import java.util.*;

public class Dealer {
    public static final int MAX_PLAYER = 4;
    public static final int MIN_PLAYER = 2;
    public static final int MAX_CARD = 5;
    public static final int PRIZE_POINT = 100;

    // 각 플레이어에게 카드를 나눠주는 메서드 이름
    private Deck deck;
    private final List<Player> players;
    private final List<Player> winsHistory;
    private final List<Map<String, String>> matchHistory;

    private boolean isNewDeck;
    private boolean isShuffle = false;
    private boolean isHandOpened = false;

    public static Dealer newDealer() {
        return new Dealer();
    }

    public Dealer() {
        this.players = new ArrayList<>();
        this.winsHistory = new ArrayList<>();
        this.matchHistory = new ArrayList<>();
    }

    /**
     * 새로운 게임을 시작시 덱을 교체합니다.
     */
    public void newGame() {
        deck = Deck.newDeck();
        isNewDeck = true;
        isShuffle = false;
    }

    public Player enrollPlayer(Player player) {
        if (this.players.size() >= Dealer.MAX_PLAYER) {
            String message = "⚠️ 플레이어는 " + Dealer.MAX_PLAYER + "명까지만 가능합니다.";
            throw new IllegalStateException(message);
        }

        // 중복 플레이어 검증 추가
        if (this.players.contains(player)) {
            throw new IllegalArgumentException("이미 등록된 플레이어입니다: " + player.getNickName());
        }

        this.players.add(player);
        return player;
    }

    public void removePlayer(Player player) {
        this.players.remove(player);
    }

    public void dealCard() {
        if (this.players.size() < Dealer.MIN_PLAYER) {
            String message = "⚠️ 플레이어가 " + Dealer.MIN_PLAYER + "명 이상이어야 합니다.";
            throw new IllegalStateException(message);
        }

        if (!isNewDeck) {
            String message = "⚠️ 덱이 준비되지 않았습니다. newGame() 메서드를 호출하세요.";
            throw new IllegalStateException(message);
        }

        if (!isShuffle) {
            String message = "⚠️ 덱이 섞이지 않았습니다. shuffle() 메서드를 호출하세요.";
            throw new IllegalStateException(message);
        }

        // 돌아가면서 한장씩 총 5개의 카드를 나눠준다.
        for (int i = 0; i < Dealer.MAX_CARD; i++) {
            for (Player player : this.players) {
                Card drawnCard = deck.drawCard();
                player.receiveCard(drawnCard);
            }
        }

        isNewDeck = false;
    }

    public void handOpen() {
        // 00. 모든 플레이어의 패를 오픈한다.
        this.players.forEach(Player::openHand);
        this.isHandOpened = true;

        // 01. 각 플레이어의 패를 확인하고 순위를 결정한다.
        this.players.sort(Player.HAND_ORDER); // 패 점수가 높은 순으로 정렬한다.

        // 02. 승자를 결정한다.
        Iterator<Player> iterator = this.players.iterator();
        Player highestPlayer = iterator.next(); // 반드시 존재하기 떄문에 null 체크는 하지 않는다.
        Player nextPlayer = iterator.next(); // 두번째 플레이어도 무조건 존재한다.

        // 표준 포커 룰: 하이카드도 순위 비교하여 승자 결정
        Player winningPlayer;
        if (highestPlayer.getHand().compareTo(nextPlayer.getHand()) == 0) {
            winningPlayer = null; // 진짜 동점일 때만 무승부
        } else {
            winningPlayer = highestPlayer; // 하이카드도 승자 결정
        }

        // 03. 기록 남기기, 승자가 없다면 null을 기록한다.
        this.winsHistory.add(winningPlayer);

        // 04. 승자에게 상금을 주고, 패자에게는 패배 횟수를 기록한다.
        Map<String, String> matchRecord = new HashMap<>();
        for (Player player : this.players) {
            if (player.equals(winningPlayer)) {
                player.prizePoint(Dealer.PRIZE_POINT);
                player.win();
            } else if (winningPlayer == null) {
                player.draw();
            } else {
                player.lose();
            }
            matchRecord.put(player.toString(), player.getHand().toString());
        }
        this.matchHistory.add(matchRecord);
    }

    public Map<String, String> getLatestMatch() {
        return this.matchHistory.getLast();
    }

    /**
     * 마지막 스테이지의 승자
     */
    public Optional<Player> getLastMatchWinner() {
        if (this.winsHistory.isEmpty()) {
            return Optional.empty();
        }
        
        Player lastWinner = this.winsHistory.get(this.winsHistory.size() - 1);

        if (lastWinner == null) {
            return Optional.empty();
        }

        return Optional.of(lastWinner);
    }

    /**
     * 게임의 최종 승자
     */
    public Optional<Player> getTotalStageWinner() {
        if (this.players.isEmpty()) {
            return Optional.empty();
        }
        
        Player winner = Collections.min(this.players, Player.WIN_COUNT_ORDER);
        return Optional.of(winner);
    }

    public List<Player> getPlayers() {
        this.players.sort(Player.WIN_COUNT_ORDER);
        return this.players;
    }

    /**
     * 각 플레이어의 카드를 수거한다.
     */
    public void retrieveCard() {
        if (!isHandOpened) {
            throw new IllegalStateException("패를 공개한 후에만 카드를 수거할 수 있습니다.");
        }
        
        for (Player player : this.players)
            player.dropHand();
            
        isHandOpened = false; // 카드 수거 후 상태 초기화
    }

    public void shuffle() {
        isShuffle = true;
        deck.shuffle();
    }

}
