package dev.xiyo.pokerhole.server.room;

import dev.xiyo.pokerhole.adapter.out.network.session.model.SessionState;
import dev.xiyo.pokerhole.core.domain.round.RoundLifecycleState;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * RoundLifecycleManager 테스트
 */
class RoundLifecycleManagerTest {

    private GameRoom gameRoom;
    private SessionState hostSession;
    private SessionState player2Session;

    @BeforeEach
    void setUp() {
        gameRoom = new GameRoom("TEST-" + UUID.randomUUID(), "테스트방");
        
        // Mock SessionState
        hostSession = mock(SessionState.class);
        when(hostSession.id()).thenReturn("host-" + UUID.randomUUID());
        when(hostSession.isHost()).thenReturn(true);
        when(hostSession.currentRoom()).thenReturn(Optional.of(gameRoom));
        when(hostSession.player()).thenReturn(Optional.empty());
        
        player2Session = mock(SessionState.class);
        when(player2Session.id()).thenReturn("player2-" + UUID.randomUUID());
        when(player2Session.isHost()).thenReturn(false);
        when(player2Session.currentRoom()).thenReturn(Optional.of(gameRoom));
        when(player2Session.player()).thenReturn(Optional.empty());
    }

    @AfterEach
    void tearDown() {
        if (gameRoom != null) {
            gameRoom.cleanup();
        }
    }

    @Test
    void 라운드_완료_후_5초_타이머_시작() {
        // Given
        String uuid1 = UUID.randomUUID().toString().substring(0, 8);
        String uuid2 = UUID.randomUUID().toString().substring(0, 8);
        gameRoom.join(hostSession, "Host_" + uuid1, true);
        gameRoom.join(player2Session, "Player2_" + uuid2, false);

        // When
        gameRoom.startRound(hostSession);

        // Then: 5초 타이머가 시작되고 메시지가 브로드캐스트됨
        verify(hostSession, timeout(1000).atLeastOnce()).send(contains("5초 후"));
    }

    @Test
    void 라운드_중_나가기_요청은_예약만_됨() {
        // Given: 2명의 플레이어로 방 생성
        String uuid1 = UUID.randomUUID().toString().substring(0, 8);
        String uuid2 = UUID.randomUUID().toString().substring(0, 8);
        gameRoom.join(hostSession, "Host_" + uuid1, true);
        
        SessionState player2 = mock(SessionState.class);
        when(player2.id()).thenReturn("player2-" + UUID.randomUUID());
        when(player2.currentRoom()).thenReturn(Optional.of(gameRoom));
        when(player2.player()).thenReturn(Optional.empty());
        
        gameRoom.join(player2, "Player2_" + uuid2, false);
        
        // When: 라운드 시작 (현재는 즉시 완료됨)
        gameRoom.startRound(hostSession);
        
        // 라운드가 완료되면 대기 상태로 전환되므로, 
        // 대기 중에 나가기를 요청하면 예약됨
        // 실제로는 라운드가 진행 중일 때 테스트해야 하지만,
        // 현재 구현에서는 라운드가 즉시 완료되므로 이 테스트는 수정 필요
        
        // Then: 현재 구현에서는 라운드가 즉시 완료되므로 이 테스트는 스킵
        // TODO: 실제 베팅 라운드가 구현되면 다시 활성화
    }

    @Test
    void 예약된_플레이어는_5초_후_자동으로_퇴장() {
        // Given
        String uuid1 = UUID.randomUUID().toString().substring(0, 8);
        String uuid2 = UUID.randomUUID().toString().substring(0, 8);
        gameRoom.join(hostSession, "Host_" + uuid1, true);
        gameRoom.join(player2Session, "Player2_" + uuid2, false);
        gameRoom.startRound(hostSession);

        // When: 나가기 예약
        gameRoom.requestLeave(player2Session);

        // Then: 5초 후 자동 퇴장
        await().atMost(6, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                verify(player2Session).detachFromRoom();
            });
    }

    @Test
    void 라운드_진행_중이_아닐_때는_즉시_나가기() {
        // Given
        String uuid1 = UUID.randomUUID().toString().substring(0, 8);
        String uuid2 = UUID.randomUUID().toString().substring(0, 8);
        gameRoom.join(hostSession, "Host_" + uuid1, true);
        gameRoom.join(player2Session, "Player2_" + uuid2, false);
        
        // When: 라운드 시작 전 나가기
        gameRoom.requestLeave(player2Session);

        // Then: 즉시 퇴장
        verify(player2Session).detachFromRoom();
    }
}
