package dev.xiyo.pokerhole.core.domain.match;

import dev.xiyo.pokerhole.core.domain.player.AIPlayer;
import dev.xiyo.pokerhole.core.domain.player.HoldemPlayer;
import dev.xiyo.pokerhole.core.common.exception.GameException;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 매칭 큐
 */
@Getter
@RequiredArgsConstructor
public class MatchingQueue {
    private final MatchingType type;
    private final String code; // 코드 매칭시에만 사용
    private final Queue<WaitingPlayer> waitingPlayers = new ConcurrentLinkedQueue<>();
    private final Instant createdAt = Instant.now();
    
    private static final int CAPACITY = 4;
    private static final Duration TIMEOUT = Duration.ofSeconds(30);
    
    public synchronized void enqueue(HoldemPlayer player) {
        if (waitingPlayers.size() >= CAPACITY) {
            throw new GameException("매칭 큐가 가득 찼습니다");
        }
        waitingPlayers.offer(new WaitingPlayer(player, Instant.now()));
    }
    
    public synchronized Optional<MatchResult> tryMatch() {
        if (waitingPlayers.size() < 2) {
            return Optional.empty();
        }
        
        // 타임아웃 체크 또는 정원 충족시 매칭
        if (shouldStartMatch()) {
            return Optional.of(createMatch());
        }
        
        return Optional.empty();
    }
    
    private boolean shouldStartMatch() {
        return waitingPlayers.size() >= CAPACITY || 
               Duration.between(createdAt, Instant.now()).compareTo(TIMEOUT) > 0;
    }
    
    private MatchResult createMatch() {
        var players = new ArrayList<HoldemPlayer>();
        
        // 실제 플레이어 추출
        while (!waitingPlayers.isEmpty() && players.size() < CAPACITY) {
            players.add(waitingPlayers.poll().player());
        }
        
        // AI로 빈자리 채우기
        while (players.size() < CAPACITY) {
            players.add(AIPlayer.createRandom());
        }
        
        return new MatchResult(MatchId.generate(), players, Instant.now());
    }
}

/**
 * 대기 중인 플레이어
 */
record WaitingPlayer(@NonNull HoldemPlayer player, @NonNull Instant enqueuedAt) {}

/**
 * 매칭 타입
 */
enum MatchingType {
    AUTO,    // 자동 매칭
    CODE     // 코드 기반 매칭
}

/**
 * 매칭 결과
 */
record MatchResult(@NonNull MatchId matchId, @NonNull List<HoldemPlayer> players, @NonNull Instant matchedAt) {}
