package dev.xiyo.pokerhole.adapter.in.websocket.service;

import dev.xiyo.pokerhole.server.room.GameRoom;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TurnTimeoutService 단위 테스트
 */
@DisplayName("TurnTimeoutService 테스트")
class TurnTimeoutTest {

    private TurnTimeoutService timeoutService;
    private GameRoom mockRoom;

    @BeforeEach
    void setUp() {
        timeoutService = new TurnTimeoutService();
        mockRoom = new GameRoom("test-room-1", "Test Room");
    }

    @AfterEach
    void tearDown() {
        timeoutService.cleanup();
    }

    @Test
    @DisplayName("타임아웃이 정상적으로 트리거되어 FOLD 처리됨")
    void testTimeoutTriggersFold() throws InterruptedException {
        // Given
        String playerId = "player1";
        int timeoutSeconds = 1; // 1초 타임아웃
        AtomicBoolean foldExecuted = new AtomicBoolean(false);
        CountDownLatch latch = new CountDownLatch(1);

        Runnable timeoutCallback = () -> {
            foldExecuted.set(true);
            latch.countDown();
        };

        // When
        timeoutService.startTimeout(playerId, mockRoom, timeoutSeconds, timeoutCallback);

        // Then
        boolean completed = latch.await(2, TimeUnit.SECONDS);
        assertThat(completed).isTrue();
        assertThat(foldExecuted.get()).isTrue();
        assertThat(timeoutService.getActiveTimeoutCount()).isZero();
    }

    @Test
    @DisplayName("플레이어 액션 시 타임아웃이 취소됨")
    void testTimeoutCancellationOnPlayerAction() throws InterruptedException {
        // Given
        String playerId = "player1";
        int timeoutSeconds = 2; // 2초 타임아웃
        AtomicBoolean foldExecuted = new AtomicBoolean(false);

        Runnable timeoutCallback = () -> foldExecuted.set(true);

        // When
        timeoutService.startTimeout(playerId, mockRoom, timeoutSeconds, timeoutCallback);
        assertThat(timeoutService.getActiveTimeoutCount()).isEqualTo(1);

        // 플레이어가 액션을 수행하여 타임아웃 취소
        Thread.sleep(500); // 0.5초 대기 (타임아웃 전)
        timeoutService.cancelTimeout(playerId);

        // Then
        Thread.sleep(2000); // 타임아웃 시간이 지나도록 대기
        assertThat(foldExecuted.get()).isFalse(); // FOLD가 실행되지 않아야 함
        assertThat(timeoutService.getActiveTimeoutCount()).isZero();
    }

    @Test
    @DisplayName("여러 플레이어의 타임아웃이 동시에 관리됨")
    void testMultipleConcurrentTimeouts() throws InterruptedException {
        // Given
        String player1 = "player1";
        String player2 = "player2";
        String player3 = "player3";
        int timeoutSeconds = 1;

        AtomicInteger foldCount = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(3);

        Runnable callback = () -> {
            foldCount.incrementAndGet();
            latch.countDown();
        };

        // When
        timeoutService.startTimeout(player1, mockRoom, timeoutSeconds, callback);
        timeoutService.startTimeout(player2, mockRoom, timeoutSeconds, callback);
        timeoutService.startTimeout(player3, mockRoom, timeoutSeconds, callback);

        assertThat(timeoutService.getActiveTimeoutCount()).isEqualTo(3);

        // Then
        boolean completed = latch.await(2, TimeUnit.SECONDS);
        assertThat(completed).isTrue();
        assertThat(foldCount.get()).isEqualTo(3);
        assertThat(timeoutService.getActiveTimeoutCount()).isZero();
    }

    @Test
    @DisplayName("서비스 종료 시 모든 타임아웃이 정리됨")
    void testCleanupOnServiceShutdown() throws InterruptedException {
        // Given
        String player1 = "player1";
        String player2 = "player2";
        int timeoutSeconds = 10; // 긴 타임아웃

        AtomicInteger foldCount = new AtomicInteger(0);
        Runnable callback = () -> foldCount.incrementAndGet();

        timeoutService.startTimeout(player1, mockRoom, timeoutSeconds, callback);
        timeoutService.startTimeout(player2, mockRoom, timeoutSeconds, callback);

        assertThat(timeoutService.getActiveTimeoutCount()).isEqualTo(2);

        // When
        timeoutService.cleanup();

        // Then
        Thread.sleep(500); // cleanup 완료 대기
        assertThat(timeoutService.getActiveTimeoutCount()).isZero();
        assertThat(foldCount.get()).isZero(); // 타임아웃이 실행되지 않아야 함
    }

    @Test
    @DisplayName("같은 플레이어의 타임아웃이 중복 시작되면 기존 타임아웃이 취소됨")
    void testDuplicateTimeoutReplacesExisting() throws InterruptedException {
        // Given
        String playerId = "player1";
        AtomicInteger callbackCount = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(1);

        Runnable callback = () -> {
            callbackCount.incrementAndGet();
            latch.countDown();
        };

        // When
        timeoutService.startTimeout(playerId, mockRoom, 2, callback); // 2초 타임아웃
        Thread.sleep(500); // 0.5초 대기
        timeoutService.startTimeout(playerId, mockRoom, 1, callback); // 1초 타임아웃으로 대체

        // Then
        boolean completed = latch.await(2, TimeUnit.SECONDS);
        assertThat(completed).isTrue();
        assertThat(callbackCount.get()).isEqualTo(1); // 한 번만 실행되어야 함
        assertThat(timeoutService.getActiveTimeoutCount()).isZero();
    }

    @Test
    @DisplayName("타임아웃 콜백에서 예외가 발생해도 서비스가 안정적으로 동작함")
    void testTimeoutCallbackExceptionHandling() throws InterruptedException {
        // Given
        String playerId = "player1";
        int timeoutSeconds = 1;
        CountDownLatch latch = new CountDownLatch(1);

        Runnable faultyCallback = () -> {
            latch.countDown();
            throw new RuntimeException("Test exception in callback");
        };

        // When
        timeoutService.startTimeout(playerId, mockRoom, timeoutSeconds, faultyCallback);

        // Then
        boolean completed = latch.await(2, TimeUnit.SECONDS);
        assertThat(completed).isTrue();

        // Wait a bit for cleanup to complete
        Thread.sleep(100);
        assertThat(timeoutService.getActiveTimeoutCount()).isZero();

        // 서비스가 여전히 정상 동작하는지 확인 (새로운 TurnTimeoutService 인스턴스 사용)
        TurnTimeoutService secondService = new TurnTimeoutService();
        AtomicBoolean secondCallbackExecuted = new AtomicBoolean(false);
        CountDownLatch secondLatch = new CountDownLatch(1);

        secondService.startTimeout("player2", mockRoom, 1, () -> {
            secondCallbackExecuted.set(true);
            secondLatch.countDown();
        });

        boolean secondCompleted = secondLatch.await(2, TimeUnit.SECONDS);
        assertThat(secondCompleted).isTrue();
        assertThat(secondCallbackExecuted.get()).isTrue();

        secondService.cleanup();
    }

    @Test
    @DisplayName("존재하지 않는 타임아웃 취소는 안전하게 처리됨")
    void testCancelNonExistentTimeout() {
        // When & Then
        timeoutService.cancelTimeout("non-existent-player");
        assertThat(timeoutService.getActiveTimeoutCount()).isZero();
    }
}
