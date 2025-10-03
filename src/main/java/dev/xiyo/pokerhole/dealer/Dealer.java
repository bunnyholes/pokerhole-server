package dev.xiyo.pokerhole.dealer;


import dev.xiyo.pokerhole.core.domain.card.Card;
import dev.xiyo.pokerhole.core.domain.card.Deck;
import dev.xiyo.pokerhole.core.domain.game.HandEvaluator;
import dev.xiyo.pokerhole.core.domain.game.HandEvaluatorImpl;
import dev.xiyo.pokerhole.core.domain.game.PotDistributor;
import dev.xiyo.pokerhole.core.domain.game.WinnerResolver;
import dev.xiyo.pokerhole.core.domain.game.vo.BettingRound;
import dev.xiyo.pokerhole.core.domain.game.vo.HandResult;
import dev.xiyo.pokerhole.core.domain.game.vo.PlayerAction;
import dev.xiyo.pokerhole.core.domain.game.vo.SidePot;
import dev.xiyo.pokerhole.core.domain.player.Player;
import dev.xiyo.pokerhole.core.domain.player.vo.PlayerId;
import dev.xiyo.pokerhole.core.domain.player.vo.PlayerStatus;

import java.util.*;

public class Dealer {
    public static final int MAX_PLAYER = 4;
    public static final int MIN_PLAYER = 2;
    public static final int MAX_CARD = 5;
    public static final int PRIZE_POINT = 100;

    // 기존 필드 (5장 카드 게임용)
    private Deck deck;
    private final List<Player> players;
    private final List<Player> winsHistory;
    private final List<Map<String, String>> matchHistory;

    private boolean isNewDeck;
    private boolean isShuffle = false;
    private boolean isHandOpened = false;

    // ===== Texas Hold'em 필드 =====
    private BettingRound currentRound;
    private List<Card> communityCards;
    private int pot;
    private int currentBet;
    private Map<Player, Integer> currentRoundBets;  // 이번 라운드 각 플레이어 베팅액
    private Map<Player, Integer> totalGameBets;     // 게임 전체 각 플레이어 총 베팅액 (사이드 팟 계산용)
    private int currentPlayerPosition;              // 현재 턴 플레이어 인덱스
    private int dealerButtonPosition;               // 딜러 버튼 위치
    private int currentHandNumber;                  // 현재 핸드 번호 (딜러 버튼 로테이션 추적용)
    private int lastRaisePosition;                  // 마지막 레이즈한 플레이어 위치
    private final HandEvaluator handEvaluator = new HandEvaluatorImpl(); // 핸드 평가기
    private final WinnerResolver winnerResolver = new WinnerResolver(); // 승자 결정 서비스
    private final PotDistributor potDistributor = new PotDistributor(); // 팟 분배 서비스

    // 블라인드 설정
    private int smallBlind = 50;  // 스몰 블라인드 (기본값 50)
    private int bigBlind = 100;   // 빅 블라인드 (기본값 100)

    public static Dealer newDealer() {
        return new Dealer();
    }

    public Dealer() {
        this.players = new ArrayList<>();
        this.winsHistory = new ArrayList<>();
        this.matchHistory = new ArrayList<>();

        // Texas Hold'em 초기화
        this.communityCards = new ArrayList<>();
        this.currentRoundBets = new HashMap<>();
        this.totalGameBets = new HashMap<>();
        this.pot = 0;
        this.currentBet = 0;
        this.currentPlayerPosition = 0;
        this.dealerButtonPosition = 0;
        this.currentHandNumber = 0;
        this.lastRaisePosition = -1;
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

    // ===== Texas Hold'em 메서드 =====

    /**
     * Texas Hold'em 게임 시작
     * - 덱 생성 및 섞기
     * - 블라인드 베팅 (스몰 블라인드, 빅 블라인드)
     * - 홀카드 2장 배분
     * - 게임 상태 초기화
     */
    public void startTexasHoldem() {
        if (this.players.size() < Dealer.MIN_PLAYER) {
            throw new IllegalStateException("플레이어가 " + Dealer.MIN_PLAYER + "명 이상이어야 합니다.");
        }

        // 덱 생성 및 섞기
        this.deck = Deck.newDeck();
        this.deck.shuffle();

        // 상태 초기화
        this.communityCards.clear();
        this.currentRoundBets.clear();
        this.totalGameBets.clear();
        this.pot = 0;
        this.currentBet = 0;
        this.currentRound = BettingRound.PRE_FLOP;
        this.lastRaisePosition = -1;

        // 모든 플레이어 상태를 ACTIVE로 설정, 베팅 초기화
        for (Player player : this.players) {
            player.setStatus(PlayerStatus.ACTIVE);
            player.resetBet();
            player.dropHand(); // 기존 카드 제거
        }

        // === 블라인드 베팅 (홀카드 배분 전) ===
        // 블라인드가 0이 아닐 때만 베팅 진행
        int bigBlindPosition = (dealerButtonPosition + 2) % players.size();

        if (smallBlind > 0 || bigBlind > 0) {
            // 스몰 블라인드: 딜러 버튼 다음 플레이어
            int smallBlindPosition = (dealerButtonPosition + 1) % players.size();
            Player smallBlindPlayer = players.get(smallBlindPosition);
            int actualSmallBlind = smallBlindPlayer.bet(smallBlind);
            pot += actualSmallBlind;
            currentRoundBets.put(smallBlindPlayer, actualSmallBlind);
            totalGameBets.put(smallBlindPlayer, actualSmallBlind);

            // 빅 블라인드: 스몰 블라인드 다음 플레이어
            Player bigBlindPlayer = players.get(bigBlindPosition);
            int actualBigBlind = bigBlindPlayer.bet(bigBlind);
            pot += actualBigBlind;
            currentRoundBets.put(bigBlindPlayer, actualBigBlind);
            totalGameBets.put(bigBlindPlayer, actualBigBlind);

            // 현재 베팅은 빅 블라인드 금액
            this.currentBet = actualBigBlind;
        }

        // 홀카드 2장씩 배분 (돌아가면서 한 장씩, 2번 반복)
        for (int i = 0; i < 2; i++) {
            for (Player player : this.players) {
                Card card = deck.drawCard();
                player.receiveCard(card);
            }
        }

        // 첫 액션은 빅 블라인드 다음 플레이어부터 시작
        this.currentPlayerPosition = (bigBlindPosition + 1) % players.size();
    }

    /**
     * 플레이어 액션 처리 (Texas Hold'em)
     * @param player 액션을 수행하는 플레이어
     * @param action 액션 타입
     * @param amount 베팅/레이즈 금액 (필요한 경우)
     */
    public void processPlayerAction(Player player, PlayerAction action, int amount) {
        // 1. 플레이어가 현재 턴인지 확인
        if (!players.get(currentPlayerPosition).equals(player)) {
            throw new IllegalStateException("현재 턴이 아닙니다.");
        }

        // 2. 플레이어 상태 확인
        if (player.getStatus() != PlayerStatus.ACTIVE) {
            throw new IllegalStateException("액션을 수행할 수 없는 상태입니다: " + player.getStatus());
        }

        // 3. 액션 타입별 처리
        switch (action) {
            case FOLD:
                player.fold();
                break;

            case CHECK:
                // CHECK는 현재 베팅이 0일 때만 가능
                int playerCurrentBet = currentRoundBets.getOrDefault(player, 0);
                if (currentBet > playerCurrentBet) {
                    throw new IllegalStateException("베팅이 있을 때는 CHECK할 수 없습니다.");
                }
                // CHECK도 액션으로 기록 (베팅 라운드 완료 판단을 위해)
                currentRoundBets.put(player, playerCurrentBet);
                break;

            case CALL:
                int playerBet = currentRoundBets.getOrDefault(player, 0);
                int callAmount = currentBet - playerBet;

                if (callAmount <= 0) {
                    throw new IllegalStateException("CALL할 베팅이 없습니다.");
                }

                int actualCall = player.bet(callAmount);
                pot += actualCall;
                currentRoundBets.put(player, playerBet + actualCall);
                totalGameBets.put(player, totalGameBets.getOrDefault(player, 0) + actualCall);
                break;

            case BET:
                // BET은 현재 베팅이 0일 때만 가능
                if (currentBet > 0) {
                    throw new IllegalStateException("이미 베팅이 있을 때는 BET할 수 없습니다. RAISE를 사용하세요.");
                }

                if (amount <= 0) {
                    throw new IllegalArgumentException("베팅 금액은 0보다 커야 합니다.");
                }

                int actualBet = player.bet(amount);
                pot += actualBet;
                currentBet = actualBet;
                currentRoundBets.put(player, actualBet);
                totalGameBets.put(player, totalGameBets.getOrDefault(player, 0) + actualBet);
                lastRaisePosition = currentPlayerPosition;
                break;

            case RAISE:
                int currentPlayerBet = currentRoundBets.getOrDefault(player, 0);

                if (amount <= currentBet) {
                    throw new IllegalArgumentException("RAISE는 현재 베팅(" + currentBet + ")보다 높아야 합니다.");
                }

                int raiseAmount = amount - currentPlayerBet;
                int actualRaise = player.bet(raiseAmount);
                pot += actualRaise;

                int newTotal = currentPlayerBet + actualRaise;
                currentBet = Math.max(currentBet, newTotal);
                currentRoundBets.put(player, newTotal);
                totalGameBets.put(player, totalGameBets.getOrDefault(player, 0) + actualRaise);
                lastRaisePosition = currentPlayerPosition;
                break;

            case ALL_IN:
                int allInAmount = player.getChips();
                int actualAllIn = player.bet(allInAmount);
                pot += actualAllIn;

                int allInTotal = currentRoundBets.getOrDefault(player, 0) + actualAllIn;
                currentRoundBets.put(player, allInTotal);
                totalGameBets.put(player, totalGameBets.getOrDefault(player, 0) + actualAllIn);

                // ALL_IN 금액이 현재 베팅보다 높으면 레이즈 효과
                if (allInTotal > currentBet) {
                    currentBet = allInTotal;
                    lastRaisePosition = currentPlayerPosition;
                }
                // player.bet()에서 자동으로 status가 ALL_IN으로 변경됨
                break;

            default:
                throw new IllegalArgumentException("알 수 없는 액션: " + action);
        }

        // 4. 다음 플레이어로 턴 이동
        moveToNextPlayer();

        // 5. 베팅 라운드 종료 확인
        if (isBettingRoundComplete()) {
            progressToNextRound();
        }
    }

    /**
     * 다음 플레이어로 턴 이동
     */
    private void moveToNextPlayer() {
        int startPosition = currentPlayerPosition;
        do {
            currentPlayerPosition = (currentPlayerPosition + 1) % players.size();
            Player nextPlayer = players.get(currentPlayerPosition);

            // ACTIVE 플레이어를 찾을 때까지 계속
            if (nextPlayer.getStatus() == PlayerStatus.ACTIVE) {
                return;
            }

            // 한 바퀴 돌았는데도 ACTIVE 플레이어가 없으면 종료
            if (currentPlayerPosition == startPosition) {
                return;
            }
        } while (true);
    }

    /**
     * 베팅 라운드가 완료되었는지 확인
     */
    private boolean isBettingRoundComplete() {
        // 한 명만 남은 경우 (모두 폴드)
        long activeOrAllInPlayers = players.stream()
                .filter(p -> p.getStatus() == PlayerStatus.ACTIVE || p.getStatus() == PlayerStatus.ALL_IN)
                .count();

        if (activeOrAllInPlayers <= 1) {
            return true;
        }

        // 모든 ACTIVE 플레이어가 동일한 금액을 베팅했는지 확인
        long activePlayers = players.stream()
                .filter(p -> p.getStatus() == PlayerStatus.ACTIVE)
                .count();

        if (activePlayers == 0) {
            return true; // 모두 ALL_IN
        }

        // 모든 ACTIVE 플레이어가 이번 라운드에서 액션했는지 확인
        long playersWhoActed = players.stream()
                .filter(p -> p.getStatus() == PlayerStatus.ACTIVE)
                .filter(p -> currentRoundBets.containsKey(p))
                .count();

        // 모든 ACTIVE 플레이어가 액션하지 않았으면 미완료
        if (playersWhoActed < activePlayers) {
            return false;
        }

        // 모든 ACTIVE 플레이어가 currentBet과 동일하게 베팅했는지 확인
        long playersWhoNeedToAct = players.stream()
                .filter(p -> p.getStatus() == PlayerStatus.ACTIVE)
                .filter(p -> {
                    int playerBet = currentRoundBets.getOrDefault(p, 0);
                    return playerBet < currentBet;
                })
                .count();

        return playersWhoNeedToAct == 0;
    }

    /**
     * 다음 베팅 라운드로 진행
     */
    private void progressToNextRound() {
        // 현재 라운드 베팅 초기화
        currentRoundBets.clear();
        currentBet = 0;

        // 모든 플레이어의 currentBet 리셋
        for (Player player : players) {
            player.resetBet();
        }

        switch (currentRound) {
            case PRE_FLOP:
                // FLOP: 커뮤니티 카드 3장 공개
                communityCards.add(deck.drawCard());
                communityCards.add(deck.drawCard());
                communityCards.add(deck.drawCard());
                currentRound = BettingRound.FLOP;
                break;

            case FLOP:
                // TURN: 커뮤니티 카드 1장 추가
                communityCards.add(deck.drawCard());
                currentRound = BettingRound.TURN;
                break;

            case TURN:
                // RIVER: 커뮤니티 카드 1장 추가
                communityCards.add(deck.drawCard());
                currentRound = BettingRound.RIVER;
                break;

            case RIVER:
                // SHOWDOWN: 패 공개 및 승자 결정
                currentRound = BettingRound.SHOWDOWN;
                determineWinner();
                return; // 게임 종료

            case SHOWDOWN:
                // 이미 종료된 상태
                return;
        }

        // 첫 번째 ACTIVE 플레이어부터 다시 시작 (딜러 버튼 다음부터)
        currentPlayerPosition = findNextActivePlayer(dealerButtonPosition);
    }

    /**
     * 특정 위치 다음의 ACTIVE 플레이어 찾기
     */
    private int findNextActivePlayer(int fromPosition) {
        for (int i = 1; i <= players.size(); i++) {
            int pos = (fromPosition + i) % players.size();
            Player player = players.get(pos);
            if (player.getStatus() == PlayerStatus.ACTIVE) {
                return pos;
            }
        }
        return fromPosition; // ACTIVE 플레이어가 없으면 원래 위치 반환
    }

    /**
     * 승자 결정 (SHOWDOWN)
     * 각 플레이어의 최종 핸드를 평가하고 승자를 결정합니다.
     */
    private void determineWinner() {
        // 남은 플레이어들 (ACTIVE 또는 ALL_IN)
        List<Player> activePlayers = players.stream()
                .filter(p -> p.getStatus() == PlayerStatus.ACTIVE || p.getStatus() == PlayerStatus.ALL_IN)
                .toList();

        if (activePlayers.isEmpty()) {
            return; // 모두 폴드한 경우 (이미 처리됨)
        }

        // 한 명만 남은 경우
        if (activePlayers.size() == 1) {
            Player winner = activePlayers.get(0);
            winner.addChips(pot);
            pot = 0;
            return;
        }

        // 각 플레이어의 최종 핸드 평가
        Map<Player, HandResult> results = new HashMap<>();
        for (Player player : activePlayers) {
            List<Card> playerCards = new ArrayList<>();
            // Hand에서 카드 추출 (Hand는 Iterable<Card>)
            for (Card card : player.getHand()) {
                playerCards.add(card);
            }

            // 홀카드 + 커뮤니티 카드로 최고의 5장 조합 평가
            HandResult result = handEvaluator.evaluate(playerCards, communityCards);
            results.put(player, result);
        }

        // 승자 결정 (가장 높은 HandResult)
        distributePot(results);
    }

    /**
     * 팟 분배 - 사이드 팟 처리 포함
     *
     * <p>사이드 팟 알고리즘:</p>
     * <ol>
     *   <li>모든 플레이어를 베팅액 기준으로 정렬 (오름차순)</li>
     *   <li>가장 적게 베팅한 플레이어부터 팟을 생성</li>
     *   <li>각 팟마다 적격 플레이어 중 최고 패를 가진 플레이어에게 분배</li>
     *   <li>동점자가 있으면 팟을 균등 분배 (나머지는 딜러 버튼 다음 순서부터)</li>
     * </ol>
     *
     * @param results 각 플레이어의 핸드 평가 결과
     */
    private void distributePot(Map<Player, HandResult> results) {
        // 사이드 팟 생성
        List<SidePot> sidePots = createSidePots(results);

        // 각 팟마다 승자 결정 및 분배
        for (SidePot sidePot : sidePots) {
            distributeSinglePot(sidePot.amount(), sidePot.eligiblePlayers(), results);
        }

        // 모든 팟 분배 완료
        pot = 0;
    }

    /**
     * 단일 팟 분배 (동점 처리 포함)
     *
     * @param potAmount 분배할 팟 금액
     * @param eligiblePlayers 팟을 받을 수 있는 플레이어 목록
     * @param allResults 모든 플레이어의 핸드 결과
     */
    private void distributeSinglePot(int potAmount, List<Player> eligiblePlayers, Map<Player, HandResult> allResults) {
        if (eligiblePlayers.isEmpty() || potAmount == 0) {
            return;
        }

        // 1. 이 팟에 참여 가능한 플레이어들의 핸드만 추출
        Map<Player, HandResult> eligibleResults = new HashMap<>();
        for (Player player : eligiblePlayers) {
            if (allResults.containsKey(player)) {
                eligibleResults.put(player, allResults.get(player));
            }
        }

        // 2. 최고 핸드 찾기
        HandResult bestHand = eligibleResults.values().stream()
                .max(HandResult::compareTo)
                .orElseThrow(() -> new IllegalStateException("No hands to evaluate"));

        // 3. 동점자들 찾기 (compareTo가 0을 반환하는 플레이어들)
        List<Player> winners = eligibleResults.entrySet().stream()
                .filter(entry -> entry.getValue().compareTo(bestHand) == 0)
                .map(Map.Entry::getKey)
                .toList();

        // 4. 승자가 1명이면 전체 팟 지급
        if (winners.size() == 1) {
            winners.get(0).addChips(potAmount);
            return;
        }

        // 5. 동점자가 여러 명이면 팟 분할
        // 딜러 버튼 기준으로 승자 순서 정렬
        List<Player> sortedWinners = sortPlayersByDealerButton(winners);

        // 팟 균등 분배
        int baseAmount = potAmount / sortedWinners.size();
        int remainder = potAmount % sortedWinners.size();

        for (int i = 0; i < sortedWinners.size(); i++) {
            Player winner = sortedWinners.get(i);
            int amount = baseAmount;

            // 나머지 칩은 딜러 버튼 다음 순서부터 1칩씩 추가
            if (i < remainder) {
                amount += 1;
            }

            winner.addChips(amount);
        }
    }

    /**
     * 플레이어 목록을 딜러 버튼 기준으로 정렬
     * 딜러 버튼 다음 위치부터 순서대로 정렬됩니다.
     *
     * @param playersToSort 정렬할 플레이어 목록
     * @return 딜러 버튼 기준 순서로 정렬된 플레이어 목록
     */
    private List<Player> sortPlayersByDealerButton(List<Player> playersToSort) {
        return playersToSort.stream()
                .sorted((p1, p2) -> {
                    int pos1 = players.indexOf(p1);
                    int pos2 = players.indexOf(p2);

                    // 딜러 버튼 기준 상대 위치 계산
                    int relativePos1 = (pos1 - dealerButtonPosition + players.size()) % players.size();
                    int relativePos2 = (pos2 - dealerButtonPosition + players.size()) % players.size();

                    return Integer.compare(relativePos1, relativePos2);
                })
                .toList();
    }

    /**
     * 사이드 팟 생성
     *
     * <p>알고리즘 예시:</p>
     * <pre>
     * Player A: 1,000 ALL_IN (총 베팅액)
     * Player B: 2,000 베팅
     * Player C: 2,000 베팅
     *
     * Step 1: A의 1,000을 기준으로 Main Pot 생성
     *   - Amount: 1,000 * 3 = 3,000
     *   - Eligible: A, B, C
     *
     * Step 2: 남은 금액 (B: 1,000, C: 1,000)으로 Side Pot 생성
     *   - Amount: 1,000 * 2 = 2,000
     *   - Eligible: B, C (A는 이미 ALL_IN이므로 제외)
     * </pre>
     */
    private List<SidePot> createSidePots(Map<Player, HandResult> results) {
        List<SidePot> sidePots = new ArrayList<>();

        // 1. 남은 플레이어들과 그들의 게임 전체 베팅액 수집
        List<PlayerBetInfo> playerBets = new ArrayList<>();
        for (Player player : results.keySet()) {
            int totalBet = totalGameBets.getOrDefault(player, 0);
            playerBets.add(new PlayerBetInfo(player, totalBet));
        }

        // 2. 베팅액 기준 오름차순 정렬 (가장 적게 베팅한 플레이어부터)
        playerBets.sort(Comparator.comparingInt(PlayerBetInfo::betAmount));

        // 3. 사이드 팟 생성
        int previousBet = 0;
        List<Player> remainingPlayers = new ArrayList<>();
        for (Player player : results.keySet()) {
            remainingPlayers.add(player);
        }

        for (int i = 0; i < playerBets.size(); i++) {
            PlayerBetInfo current = playerBets.get(i);
            int currentBet = current.betAmount();

            // 이번 단계에서 각 플레이어가 기여한 금액
            int contribution = currentBet - previousBet;

            if (contribution > 0 && !remainingPlayers.isEmpty()) {
                // 팟 금액 = 기여금 * 남은 플레이어 수
                int potAmount = contribution * remainingPlayers.size();

                // 이 팟에 참여할 수 있는 플레이어 목록 (현재 남은 모든 플레이어)
                SidePot sidePot = new SidePot(potAmount, new ArrayList<>(remainingPlayers));
                sidePots.add(sidePot);
            }

            // 현재 플레이어는 더 이상 다음 팟에 참여할 수 없음
            remainingPlayers.remove(current.player());
            previousBet = currentBet;
        }

        return sidePots;
    }


    /**
     * 플레이어와 베팅액 정보를 담는 내부 클래스
     */
    private record PlayerBetInfo(Player player, int betAmount) {}

    // ===== Getter 메서드 (상태 조회용) =====

    public BettingRound getCurrentRound() {
        return currentRound;
    }

    public List<Card> getCommunityCards() {
        return new ArrayList<>(communityCards); // 방어적 복사
    }

    public int getPot() {
        return pot;
    }

    public int getCurrentBet() {
        return currentBet;
    }

    public Player getCurrentPlayer() {
        if (currentPlayerPosition >= 0 && currentPlayerPosition < players.size()) {
            return players.get(currentPlayerPosition);
        }
        return null;
    }

    /**
     * 스몰 블라인드 금액 조회
     */
    public int getSmallBlind() {
        return smallBlind;
    }

    /**
     * 빅 블라인드 금액 조회
     */
    public int getBigBlind() {
        return bigBlind;
    }

    /**
     * 스몰 블라인드 금액 설정
     */
    public void setSmallBlind(int smallBlind) {
        if (smallBlind < 0) {
            throw new IllegalArgumentException("스몰 블라인드는 0 이상이어야 합니다.");
        }
        this.smallBlind = smallBlind;
    }

    /**
     * 빅 블라인드 금액 설정
     */
    public void setBigBlind(int bigBlind) {
        if (bigBlind < 0) {
            throw new IllegalArgumentException("빅 블라인드는 0 이상이어야 합니다.");
        }
        this.bigBlind = bigBlind;
    }

    /**
     * 딜러 버튼 위치 조회
     */
    public int getDealerButtonPosition() {
        return dealerButtonPosition;
    }

    /**
     * 현재 핸드 번호 조회
     */
    public int getCurrentHandNumber() {
        return currentHandNumber;
    }

    // ===== 딜러 버튼 로테이션 관련 메서드 =====

    /**
     * 딜러 버튼을 시계방향으로 회전시킵니다.
     * 핸드 번호도 함께 증가시킵니다.
     *
     * <p>호출 시점: 핸드가 끝날 때 (SHOWDOWN 또는 모두 폴드 시)</p>
     */
    public void rotateDealerButton() {
        if (players.isEmpty()) {
            // 플레이어가 없으면 회전하지 않음
            return;
        }

        if (players.size() == 1) {
            // 1명일 때는 위치는 그대로, 핸드 번호만 증가
            currentHandNumber++;
            return;
        }

        // 이전 위치 저장 (이벤트 발행용)
        int previousPosition = dealerButtonPosition;

        // 딜러 버튼을 시계방향으로 회전 (다음 플레이어로 이동)
        dealerButtonPosition = (dealerButtonPosition + 1) % players.size();

        // 핸드 번호 증가
        currentHandNumber++;

        // TODO: DealerButtonRotated 이벤트 발행 (Event Sourcing 구현 시)
        // emitEvent(new DealerButtonRotated(gameId, previousPosition, dealerButtonPosition, currentHandNumber));
    }

    /**
     * 현재 핸드를 종료하고 딜러 버튼을 회전시킵니다.
     *
     * <p>수행 작업:</p>
     * <ol>
     *   <li>딜러 버튼 회전</li>
     *   <li>핸드별 상태 초기화 (다음 핸드 준비)</li>
     * </ol>
     *
     * <p>호출 시점: SHOWDOWN 완료 후 또는 모든 플레이어가 폴드하여 승자가 결정된 후</p>
     */
    public void endHand() {
        // 1. 딜러 버튼 회전
        rotateDealerButton();

        // 2. 핸드별 상태 초기화
        // - 커뮤니티 카드 초기화는 다음 startTexasHoldem()에서 처리됨
        // - 플레이어별 카드는 startTexasHoldem()에서 dropHand()로 처리됨
        // 여기서는 명시적으로 초기화할 상태가 없음 (rotation만 수행)
    }

    /**
     * 새로운 핸드를 시작합니다.
     * 기존 핸드를 종료하고 딜러 버튼을 회전시킨 후, 새로운 Texas Hold'em 게임을 시작합니다.
     *
     * <p>사용 예시:</p>
     * <pre>
     * dealer.startTexasHoldem();  // 첫 핸드
     * // ... 게임 진행 ...
     * dealer.startNewHand();      // 다음 핸드 (딜러 버튼 자동 회전)
     * </pre>
     *
     * <p>주의: 이 메서드는 이전 핸드가 완전히 종료된 후에만 호출해야 합니다.</p>
     */
    public void startNewHand() {
        // 1. 이전 핸드 종료 및 딜러 버튼 회전
        endHand();

        // 2. 새로운 Texas Hold'em 핸드 시작
        // startTexasHoldem()이 모든 상태를 초기화하므로 별도 준비 불필요
        startTexasHoldem();
    }

    /**
     * 플레이어가 게임을 떠날 때 딜러 버튼 위치를 조정합니다.
     *
     * <p>전략: 딜러 버튼이 물리적 위치에 고정되도록 유지합니다.</p>
     * <ul>
     *   <li>떠나는 플레이어가 딜러 버튼 위치보다 앞에 있으면: 버튼 위치를 1 감소 (인덱스 조정)</li>
     *   <li>떠나는 플레이어가 딜러 버튼 위치이면: 버튼 위치 유지 (다음 플레이어가 자연스럽게 딜러가 됨)</li>
     *   <li>떠나는 플레이어가 딜러 버튼 위치보다 뒤에 있으면: 버튼 위치 변경 없음</li>
     * </ul>
     *
     * @param player 떠나는 플레이어
     */
    public void adjustDealerButtonOnPlayerLeave(Player player) {
        int playerPosition = players.indexOf(player);

        if (playerPosition == -1) {
            // 플레이어가 리스트에 없음 (이미 제거됨)
            return;
        }

        // 플레이어가 딜러 버튼보다 앞에 있으면 버튼 위치를 1 감소
        if (playerPosition < dealerButtonPosition) {
            dealerButtonPosition--;
        }
        // 플레이어가 딜러 버튼 위치이면 버튼 위치는 그대로 (다음 플레이어가 딜러)
        // 플레이어가 딜러 버튼보다 뒤에 있으면 버튼 위치 변경 없음

        // 경계 조건 처리: 플레이어 제거 후 버튼 위치가 범위를 벗어나면 조정
        if (dealerButtonPosition >= players.size() && !players.isEmpty()) {
            dealerButtonPosition = players.size() - 1;
        }
    }

}
