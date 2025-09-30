package dev.xiyo.pokerhole.core.domain.round;

import dev.xiyo.pokerhole.core.domain.betting.*;
import dev.xiyo.pokerhole.core.domain.card.Card;
import dev.xiyo.pokerhole.core.domain.card.Deck;
import dev.xiyo.pokerhole.core.domain.game.GameId;
import dev.xiyo.pokerhole.core.domain.player.HoldemPlayer;
import dev.xiyo.pokerhole.core.domain.player.PlayerId;
import dev.xiyo.pokerhole.core.common.exception.GameException;
import lombok.*;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 라운드 (한 게임)
 */
@Value
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Round {
    @NonNull RoundId id;
    @NonNull GameId gameId;
    int roundNumber;
    
    @Singular
    List<HoldemPlayer> players;
    
    @NonNull Deck deck;
    
    @NonNull RoundGameState state;
    
    @Singular("action")
    List<PlayerAction> actionHistory;
    
    @NonNull Instant startedAt;
    Instant endedAt;
    
    public static Round create(List<HoldemPlayer> players, GameId gameId, int roundNumber) {
        if (players.size() < 2 || players.size() > 4) {
            throw new GameException("플레이어는 2-4명이어야 합니다");
        }
        
        var deck = Deck.newDeck();
        deck.shuffle();
        
        // 포지션 할당
        var positions = assignPositions(players);
        
        // 블라인드 강제 베팅
        var smallBlind = findPlayer(players, positions, Position.SMALL_BLIND);
        var bigBlind = findPlayer(players, positions, Position.BIG_BLIND);
        
        var initialState = RoundGameState.builder()
            .gameId(gameId)
            .currentRound(BettingRound.PRE_FLOP)
            .pot(Position.SMALL_BLIND.getForcedBet() + Position.BIG_BLIND.getForcedBet())
            .currentBet(Position.BIG_BLIND.getForcedBet())
            .playerBet(smallBlind.getId(), Position.SMALL_BLIND.getForcedBet())
            .playerBet(bigBlind.getId(), Position.BIG_BLIND.getForcedBet())
            .positions(positions)
            .currentTurn(nextPlayer(bigBlind.getId(), players))
            .build();
        
        // 각 플레이어에게 2장씩 카드 배분
        players.forEach(player -> {
            player.receiveCard(deck.drawCard());
            player.receiveCard(deck.drawCard());
        });
        
        return Round.builder()
            .id(RoundId.generate())
            .gameId(gameId)
            .roundNumber(roundNumber)
            .players(players)
            .deck(deck)
            .state(initialState)
            .startedAt(Instant.now())
            .build();
    }
    
    public Round applyAction(PlayerAction action) {
        validateAction(action);
        
        var player = findPlayer(action.getPlayerId());
        var updatedState = state;
        
        switch (action.getType()) {
            case FOLD -> updatedState = handleFold(player, state);
            case CHECK -> updatedState = handleCheck(player, state);
            case CALL -> updatedState = handleCall(player, action.getAmount(), state);
            case BET -> updatedState = handleBet(player, action.getAmount(), state);
            case RAISE -> updatedState = handleRaise(player, action.getAmount(), state);
            case ALL_IN -> updatedState = handleAllIn(player, action.getAmount(), state);
        }
        
        updatedState = updatedState.nextTurn(determineNextPlayer());
        
        return this.toBuilder()
            .state(updatedState)
            .action(action)
            .build();
    }
    
    private void validateAction(PlayerAction action) {
        if (!state.getCurrentTurn().equals(action.getPlayerId())) {
            throw new GameException("현재 차례가 아닙니다");
        }
    }
    
    private RoundGameState handleCheck(HoldemPlayer player, RoundGameState state) {
        if (!state.canCheck(player.getId())) {
            throw new GameException("체크할 수 없습니다");
        }
        return state; // 체크는 상태 변화 없음
    }
    
    private RoundGameState handleFold(HoldemPlayer player, RoundGameState state) {
        return state.toBuilder()
            .foldedPlayer(player.getId())
            .build();
    }
    
    private RoundGameState handleCall(HoldemPlayer player, long amount, RoundGameState state) {
        player.deductChips(amount);
        return state
            .addToPot(amount)
            .updatePlayerBet(player.getId(), state.getPlayerBet(player.getId()) + amount);
    }
    
    private RoundGameState handleBet(HoldemPlayer player, long amount, RoundGameState state) {
        if (state.getCurrentBet() > 0) {
            throw new GameException("이미 베팅이 있습니다. RAISE를 사용하세요");
        }
        player.deductChips(amount);
        return state.toBuilder()
            .pot(state.getPot() + amount)
            .currentBet(amount)
            .build()
            .updatePlayerBet(player.getId(), amount);
    }
    
    private RoundGameState handleRaise(HoldemPlayer player, long amount, RoundGameState state) {
        if (amount <= state.getCurrentBet()) {
            throw new GameException("레이즈 금액이 현재 베팅보다 커야 합니다");
        }
        
        long totalBet = amount;
        long toAdd = totalBet - state.getPlayerBet(player.getId());
        
        player.deductChips(toAdd);
        return state.toBuilder()
            .pot(state.getPot() + toAdd)
            .currentBet(totalBet)
            .build()
            .updatePlayerBet(player.getId(), totalBet);
    }
    
    private RoundGameState handleAllIn(HoldemPlayer player, long amount, RoundGameState state) {
        player.deductChips(amount);
        return state.toBuilder()
            .pot(state.getPot() + amount)
            .currentBet(Math.max(state.getCurrentBet(), amount))
            .allInPlayer(player.getId())
            .build()
            .updatePlayerBet(player.getId(), amount);
    }
    
    public boolean isRoundComplete() {
        var activePlayers = players.stream()
            .filter(p -> !state.getFoldedPlayers().contains(p.getId()))
            .toList();
        
        // 1명만 남으면 즉시 종료
        if (activePlayers.size() == 1) {
            return true;
        }
        
        // 모든 플레이어가 동일 베팅액이고 액션을 했으면 라운드 완료
        return activePlayers.stream()
            .allMatch(p -> {
                long playerBet = state.getPlayerBet(p.getId());
                return playerBet == state.getCurrentBet() || 
                       state.getAllInPlayers().contains(p.getId());
            });
    }
    
    public Round progressToNextBettingRound() {
        var nextRound = switch (state.getCurrentRound()) {
            case PRE_FLOP -> BettingRound.FLOP;
            case FLOP -> BettingRound.TURN;
            case TURN -> BettingRound.RIVER;
            case RIVER -> BettingRound.SHOWDOWN;
            case SHOWDOWN -> throw new GameException("이미 쇼다운입니다");
        };
        
        var updatedState = state.nextRound(nextRound);
        
        // 커뮤니티 카드 공개
        if (nextRound == BettingRound.FLOP) {
            var cards = new ArrayList<>(updatedState.getCommunityCards());
            cards.add(deck.drawCard());
            cards.add(deck.drawCard());
            cards.add(deck.drawCard());
            updatedState = updatedState.toBuilder()
                .communityCards(cards)
                .build();
        } else if (nextRound == BettingRound.TURN || nextRound == BettingRound.RIVER) {
            var cards = new ArrayList<>(updatedState.getCommunityCards());
            cards.add(deck.drawCard());
            updatedState = updatedState.toBuilder()
                .communityCards(cards)
                .build();
        }
        
        return this.toBuilder()
            .state(updatedState)
            .build();
    }
    
    public RoundResult evaluateWinner() {
        var activePlayers = players.stream()
            .filter(p -> !state.getFoldedPlayers().contains(p.getId()))
            .toList();
        
        if (activePlayers.size() == 1) {
            return RoundResult.builder()
                .roundId(id)
                .winner(activePlayers.get(0))
                .winAmount(state.getPot())
                .build();
        }
        
        // 쇼다운: 핸드 평가
        activePlayers.forEach(HoldemPlayer::openHand);
        
        var winner = activePlayers.stream()
            .max(Comparator.comparing(HoldemPlayer::getHand))
            .orElseThrow();
        
        return RoundResult.builder()
            .roundId(id)
            .winner(winner)
            .winAmount(state.getPot())
            .playerHands(activePlayers.stream()
                .collect(Collectors.toMap(HoldemPlayer::getId, HoldemPlayer::getHand)))
            .build();
    }
    
    private static Map<PlayerId, Position> assignPositions(List<HoldemPlayer> players) {
        var positions = new HashMap<PlayerId, Position>();
        positions.put(players.get(0).getId(), Position.SMALL_BLIND);
        positions.put(players.get(1).getId(), Position.BIG_BLIND);
        
        for (int i = 2; i < players.size(); i++) {
            positions.put(players.get(i).getId(), Position.EARLY);
        }
        
        return positions;
    }
    
    private static HoldemPlayer findPlayer(List<HoldemPlayer> players, Map<PlayerId, Position> positions, Position position) {
        return players.stream()
            .filter(p -> position.equals(positions.get(p.getId())))
            .findFirst()
            .orElseThrow(() -> new GameException("해당 포지션의 플레이어를 찾을 수 없습니다"));
    }
    
    private static PlayerId nextPlayer(PlayerId current, List<HoldemPlayer> players) {
        int currentIndex = -1;
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).getId().equals(current)) {
                currentIndex = i;
                break;
            }
        }
        
        if (currentIndex == -1) {
            throw new GameException("현재 플레이어를 찾을 수 없습니다");
        }
        
        int nextIndex = (currentIndex + 1) % players.size();
        return players.get(nextIndex).getId();
    }
    
    private HoldemPlayer findPlayer(PlayerId playerId) {
        return players.stream()
            .filter(p -> p.getId().equals(playerId))
            .findFirst()
            .orElseThrow(() -> new GameException("플레이어를 찾을 수 없습니다"));
    }
    
    private PlayerId determineNextPlayer() {
        // 간단한 순차 로직 - 폴드한 플레이어는 건너뜀
        var currentIndex = -1;
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).getId().equals(state.getCurrentTurn())) {
                currentIndex = i;
                break;
            }
        }
        
        for (int i = 1; i <= players.size(); i++) {
            int nextIndex = (currentIndex + i) % players.size();
            var nextPlayer = players.get(nextIndex);
            if (!state.getFoldedPlayers().contains(nextPlayer.getId())) {
                return nextPlayer.getId();
            }
        }
        
        throw new GameException("다음 플레이어를 찾을 수 없습니다");
    }
}
