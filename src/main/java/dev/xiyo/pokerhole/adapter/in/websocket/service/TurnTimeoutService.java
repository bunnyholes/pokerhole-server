package dev.xiyo.pokerhole.adapter.in.websocket.service;

import dev.xiyo.pokerhole.server.room.GameRoom;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * 턴 타임아웃 관리 서비스
 * 각 플레이어의 턴 제한 시간을 관리하고, 시간 초과 시 자동 FOLD 처리
 */
@Slf4j
@Service
public class TurnTimeoutService {

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private final Map<String, ScheduledFuture<?>> activeTimeouts = new ConcurrentHashMap<>();

    /**
     * 플레이어의 턴 타임아웃을 시작합니다.
     *
     * @param playerId 플레이어 식별자 (닉네임)
     * @param room 게임 룸
     * @param timeoutSeconds 타임아웃 시간 (초)
     * @param timeoutCallback 타임아웃 발생 시 실행할 콜백
     */
    public void startTimeout(String playerId, GameRoom room, int timeoutSeconds, Runnable timeoutCallback) {
        // 기존 타임아웃이 있다면 취소
        cancelTimeout(playerId);

        log.info("턴 타임아웃 시작: playerId={}, roomId={}, timeoutSeconds={}",
                playerId, room.id(), timeoutSeconds);

        ScheduledFuture<?> timeoutTask = scheduler.schedule(() -> {
            try {
                log.warn("턴 타임아웃 발생: playerId={}, roomId={}", playerId, room.id());

                // 타임아웃 발생 시 콜백 실행
                timeoutCallback.run();

            } catch (Exception e) {
                log.error("타임아웃 처리 중 오류 발생: playerId={}, roomId={}",
                        playerId, room.id(), e);
            } finally {
                // 예외 발생 여부와 관계없이 타임아웃 제거
                activeTimeouts.remove(playerId);
            }
        }, timeoutSeconds, TimeUnit.SECONDS);

        activeTimeouts.put(playerId, timeoutTask);
    }

    /**
     * 플레이어의 턴 타임아웃을 취소합니다.
     *
     * @param playerId 플레이어 식별자 (닉네임)
     */
    public void cancelTimeout(String playerId) {
        ScheduledFuture<?> existingTimeout = activeTimeouts.remove(playerId);
        if (existingTimeout != null) {
            boolean cancelled = existingTimeout.cancel(false);
            log.debug("턴 타임아웃 취소: playerId={}, cancelled={}", playerId, cancelled);
        }
    }

    /**
     * 모든 활성 타임아웃을 취소합니다.
     */
    public void cancelAllTimeouts() {
        log.info("모든 턴 타임아웃 취소 중... (활성 타임아웃: {}개)", activeTimeouts.size());
        activeTimeouts.values().forEach(timeout -> timeout.cancel(false));
        activeTimeouts.clear();
    }

    /**
     * 현재 활성 타임아웃 개수를 반환합니다.
     *
     * @return 활성 타임아웃 개수
     */
    public int getActiveTimeoutCount() {
        return activeTimeouts.size();
    }

    /**
     * 서비스 종료 시 리소스 정리
     */
    @PreDestroy
    public void cleanup() {
        log.info("TurnTimeoutService 종료 중...");
        cancelAllTimeouts();
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
                log.warn("스케줄러 강제 종료");
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
            log.error("스케줄러 종료 중 인터럽트 발생", e);
        }
        log.info("TurnTimeoutService 종료 완료");
    }
}
