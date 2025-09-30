package dev.xiyo.pokerhole.core.domain.match;

import dev.xiyo.pokerhole.core.domain.game.GameId;
import dev.xiyo.pokerhole.core.domain.player.AIPlayer;
import dev.xiyo.pokerhole.core.domain.player.HoldemPlayer;
import dev.xiyo.pokerhole.core.domain.player.PlayerId;
import dev.xiyo.pokerhole.core.domain.round.Round;
import dev.xiyo.pokerhole.core.domain.round.RoundId;
import dev.xiyo.pokerhole.core.domain.round.RoundResult;
import lombok.*;

import java.time.Instant;
import java.util.*;

/**
 * 매치 세션 - 여러 라운드로 구성된 게임 세션
 */
@Getter
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MatchSession {
    @NonNull
    private final MatchSessionId id;
    
    @NonNull
    private final List<HoldemPlayer> participants;
    
    @Builder.Default
    private final Set<PlayerId> pendingExits = new HashSet<>();
    
    @Builder.Default
    private final Map<PlayerId, PlayerId> aiReplacements = new HashMap<>();
    
    private final GameId currentGameId;
    private RoundId currentRoundId;
    private Instant leaveDeadline;
    private boolean terminated;
    
    public void markRoundCompleted(RoundResult result, Instant deadline) {
        this.leaveDeadline = deadline;
    }
    
    public void registerPendingExit(PlayerId id) {
        pendingExits.add(id);
    }
    
    public void replaceWithAI(PlayerId original) {
        if (aiReplacements.containsKey(original)) {
            return;
        }
        var ai = AIPlayer.createRandom();
        aiReplacements.put(original, ai.getId());
        
        // 참가자 목록에서 해당 플레이어를 AI로 교체
        for (int i = 0; i < participants.size(); i++) {
            if (participants.get(i).getId().equals(original)) {
                participants.set(i, ai);
                break;
            }
        }
    }
    
    public void applyPendingExitsAndReplacements() {
        var remaining = new ArrayList<HoldemPlayer>();
        for (HoldemPlayer p : participants) {
            if (pendingExits.contains(p.getId())) {
                continue;
            }
            remaining.add(p);
        }
        participants.clear();
        participants.addAll(remaining);
        pendingExits.clear();
    }
    
    public int activePlayerCount() {
        return (int) participants.stream()
            .filter(p -> !pendingExits.contains(p.getId()))
            .count();
    }
    
    public void startNextRound(Round round) {
        this.currentRoundId = round.getId();
        this.leaveDeadline = null;
    }
    
    public void terminate() {
        this.terminated = true;
    }
}
