package dev.xiyo.pokerhole.server.room;

import dev.xiyo.pokerhole.adapter.out.network.session.model.SessionState;
import dev.xiyo.pokerhole.core.domain.round.RoundLifecycleState;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * 라운드 생명주기 관리자
 * - 라운드 완료 후 5초 자동 재시작
 * - 나가기 예약 관리
 * - AI 플레이어 교체
 */
@Slf4j
public class RoundLifecycleManager {
    private static final int AUTO_START_DELAY_SECONDS = 5;
    
    @Getter
    private RoundLifecycleState state = RoundLifecycleState.IN_PROGRESS;
    
    private final GameRoom gameRoom;
    private final ScheduledExecutorService scheduler;
    private final Set<String> pendingExits = new HashSet<>();
    private ScheduledFuture<?> autoStartTask;
    private Instant roundCompletedAt;
    
    public RoundLifecycleManager(GameRoom gameRoom, ScheduledExecutorService scheduler) {
        this.gameRoom = gameRoom;
        this.scheduler = scheduler;
    }
    
    /**
     * 라운드 완료 처리 및 5초 타이머 시작
     */
    public synchronized void onRoundComplete() {
        if (state == RoundLifecycleState.IN_PROGRESS) {
            state = RoundLifecycleState.COMPLETED;
            roundCompletedAt = Instant.now();
            
            log.info("Round completed for room {}, starting 5-second timer", gameRoom.id());
            
            // 5초 후 자동 재시작 스케줄링
            autoStartTask = scheduler.schedule(
                this::onAutoStartTimerExpired,
                AUTO_START_DELAY_SECONDS,
                TimeUnit.SECONDS
            );
            
            // 모든 플레이어에게 알림
            gameRoom.broadcast("⏱️ " + AUTO_START_DELAY_SECONDS + "초 후 다음 라운드가 자동으로 시작됩니다.");
            gameRoom.broadcast("💡 나가려면 LEAVE 명령을 입력하세요.");
        }
    }
    
    /**
     * 나가기 예약
     */
    public synchronized void requestLeave(SessionState session) {
        String sessionId = session.id();
        
        if (state == RoundLifecycleState.IN_PROGRESS) {
            // 라운드 진행 중에는 예약만 가능
            if (!pendingExits.contains(sessionId)) {
                pendingExits.add(sessionId);
                session.send("✅ 나가기 예약됨. 라운드 종료 후 자동으로 퇴장됩니다.");
                gameRoom.broadcast("📢 " + session.player().map(p -> p.getNickName()).orElse("플레이어") + " 님이 나가기를 예약했습니다.");
                log.info("Player {} reserved to leave room {}", sessionId, gameRoom.id());
            } else {
                session.send("⚠️ 이미 나가기가 예약되어 있습니다.");
            }
        } else if (state == RoundLifecycleState.WAITING_NEXT || state == RoundLifecycleState.COMPLETED) {
            // 라운드 완료 후에는 즉시 처리
            pendingExits.add(sessionId);
            processLeaveRequest(session);
        } else {
            session.send("⚠️ 현재 나갈 수 없는 상태입니다.");
        }
    }
    
    /**
     * 나가기 예약 취소
     */
    public synchronized void cancelLeaveRequest(SessionState session) {
        if (pendingExits.remove(session.id())) {
            session.send("✅ 나가기 예약이 취소되었습니다.");
            log.info("Player {} cancelled leave request for room {}", session.id(), gameRoom.id());
        }
    }
    
    /**
     * 5초 타이머 만료 시 처리
     */
    private synchronized void onAutoStartTimerExpired() {
        state = RoundLifecycleState.WAITING_NEXT;
        
        log.info("Auto-start timer expired for room {}, processing exits and starting next round", gameRoom.id());
        
        // 나가기 예약된 플레이어 처리
        processPendingExits();
        
        // 남은 플레이어로 다음 라운드 시작
        if (gameRoom.canStartRound()) {
            try {
                state = RoundLifecycleState.IN_PROGRESS;
                gameRoom.autoStartNextRound();
            } catch (Exception e) {
                log.error("Failed to auto-start next round for room {}", gameRoom.id(), e);
                state = RoundLifecycleState.TERMINATED;
                gameRoom.broadcast("⚠️ 다음 라운드 시작 중 오류가 발생했습니다.");
            }
        } else {
            log.info("Not enough players remaining in room {}, terminating", gameRoom.id());
            state = RoundLifecycleState.TERMINATED;
            gameRoom.broadcast("⚠️ 인원이 부족하여 게임을 계속할 수 없습니다.");
        }
    }
    
    /**
     * 강제 종료 처리 (연결 끊김)
     */
    public synchronized void handleDisconnect(SessionState session) {
        String sessionId = session.id();
        
        log.warn("Player {} disconnected from room {} during state {}", sessionId, gameRoom.id(), state);
        
        if (state == RoundLifecycleState.IN_PROGRESS) {
            // AI로 대체하고 라운드 완료 후 제거
            pendingExits.add(sessionId);
            gameRoom.broadcast("⚠️ " + session.player().map(p -> p.getNickName()).orElse("플레이어") + " 님의 연결이 끊어졌습니다. AI가 대신합니다.");
            // TODO: AI 플레이어로 교체 로직
        } else {
            // 즉시 제거
            pendingExits.add(sessionId);
            processPendingExits();
        }
    }
    
    /**
     * 예약된 나가기 처리
     */
    private void processPendingExits() {
        if (pendingExits.isEmpty()) {
            return;
        }
        
        Set<String> toRemove = new HashSet<>(pendingExits);
        pendingExits.clear();
        
        for (String sessionId : toRemove) {
            // GameRoom에서 실제 제거 처리
            gameRoom.removeBySessionId(sessionId);
        }
    }
    
    /**
     * 특정 세션이 나가기 예약 상태인지 확인
     */
    public synchronized boolean isLeaveReserved(String sessionId) {
        return pendingExits.contains(sessionId);
    }
    
    /**
     * 라운드 시작
     */
    public synchronized void startRound() {
        state = RoundLifecycleState.IN_PROGRESS;
        pendingExits.clear();
        
        if (autoStartTask != null && !autoStartTask.isDone()) {
            autoStartTask.cancel(false);
            autoStartTask = null;
        }
    }
    
    /**
     * 정리 작업
     */
    public synchronized void cleanup() {
        if (autoStartTask != null && !autoStartTask.isDone()) {
            autoStartTask.cancel(true);
            autoStartTask = null;
        }
        pendingExits.clear();
        state = RoundLifecycleState.TERMINATED;
    }
    
    private void processLeaveRequest(SessionState session) {
        // 실제 나가기 처리는 GameRoom에 위임
        gameRoom.removeBySessionId(session.id());
    }
}
