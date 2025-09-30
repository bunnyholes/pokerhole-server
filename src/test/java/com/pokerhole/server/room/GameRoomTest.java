package com.pokerhole.server.room;

import com.pokerhole.server.session.SessionState;
import com.pokerhole.server.support.InMemoryConnection;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameRoomTest {

    @Test
    void 방장이퇴장하면다른참가자가승계된다() {
        GameRoom room = new GameRoom("ROOM01", "test");
        SessionState hostState = new SessionState(new InMemoryConnection());
        SessionState guestState = new SessionState(new InMemoryConnection());

        room.join(hostState, "host", true);
        room.join(guestState, "guest", false);

        boolean empty = room.remove(hostState);
        assertFalse(empty, "게스트가 남아 있으므로 방은 유지되어야 합니다.");
        assertFalse(hostState.isHost(), "퇴장한 사용자는 더 이상 방장이 아니다.");
        assertTrue(guestState.isHost(), "남아 있는 사용자가 새 방장이 되어야 한다.");

        room.remove(guestState); // 테스트 종료 후 자원 정리
    }

    @Test
    void 최소인원미만이면라운드를시작할수없다() {
        GameRoom room = new GameRoom("ROOM02", "test");
        SessionState hostState = new SessionState(new InMemoryConnection());
        room.join(hostState, "host", true);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> room.startRound(hostState));
        assertTrue(ex.getMessage().contains("최소"), "에러 메시지에 최소 인원 안내가 포함되어야 한다.");

        room.remove(hostState);
    }
}
