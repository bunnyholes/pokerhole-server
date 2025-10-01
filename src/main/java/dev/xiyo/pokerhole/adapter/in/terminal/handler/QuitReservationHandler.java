package dev.xiyo.pokerhole.adapter.in.terminal.handler;

import dev.xiyo.pokerhole.adapter.out.network.session.model.SessionState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 나가기 예약 핸들러
 * 게임 중 q키로 나가기 예약 (게임 종료 후 실행)
 */
@Slf4j
@Component
public class QuitReservationHandler {

    private final Map<String, QuitReservation> reservations = new ConcurrentHashMap<>();

    /**
     * 나가기 예약
     */
    public void reserve(SessionState session) {
        String sessionId = session.id();
        reservations.put(sessionId, new QuitReservation(sessionId, System.currentTimeMillis()));

        log.info("나가기 예약: sessionId={}", sessionId);
        session.send("\n❗ 나가기가 예약되었습니다.");
        session.send("게임 종료 후 자동으로 나갑니다.");
        session.send("취소하려면 'c' 키를 누르세요.\n");
    }

    /**
     * 나가기 예약 취소
     */
    public void cancel(SessionState session) {
        String sessionId = session.id();
        QuitReservation removed = reservations.remove(sessionId);

        if (removed != null) {
            log.info("나가기 예약 취소: sessionId={}", sessionId);
            session.send("\n✅ 나가기 예약이 취소되었습니다.\n");
        } else {
            session.send("\n⚠️  예약된 나가기가 없습니다.\n");
        }
    }

    /**
     * 예약 확인
     */
    public boolean isReserved(String sessionId) {
        return reservations.containsKey(sessionId);
    }

    /**
     * 예약 실행 (게임 종료 후)
     */
    public void execute(String sessionId) {
        if (reservations.remove(sessionId) != null) {
            log.info("나가기 예약 실행: sessionId={}", sessionId);
        }
    }

    /**
     * 예약 초기화 (세션 종료 시)
     */
    public void clear(String sessionId) {
        reservations.remove(sessionId);
    }

    /**
     * 나가기 예약 정보
     */
    private record QuitReservation(
            String sessionId,
            long reservedAt
    ) {
    }
}
