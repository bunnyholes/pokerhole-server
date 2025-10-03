package dev.xiyo.pokerhole.adapter.in.websocket.session;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * WebSocket 세션 레지스트리
 * 세션 ID와 PlayerSession 매핑 관리
 */
@Slf4j
@Component
public class WebSocketSessionRegistry {

    private final Map<String, PlayerSession> sessionsBySessionId = new ConcurrentHashMap<>();
    private final Map<String, PlayerSession> sessionsByUuid = new ConcurrentHashMap<>();

    /**
     * 플레이어 세션 등록
     */
    public void register(PlayerSession playerSession) {
        String sessionId = playerSession.getSessionId();
        String uuid = playerSession.getUuid();

        sessionsBySessionId.put(sessionId, playerSession);
        sessionsByUuid.put(uuid, playerSession);

        log.info("플레이어 세션 등록: sessionId={}, uuid={}, nickname={}",
                sessionId, uuid, playerSession.getNickname());
    }

    /**
     * 세션 ID로 플레이어 세션 조회
     */
    public Optional<PlayerSession> findBySessionId(String sessionId) {
        return Optional.ofNullable(sessionsBySessionId.get(sessionId));
    }

    /**
     * UUID로 플레이어 세션 조회
     */
    public Optional<PlayerSession> findByUuid(String uuid) {
        return Optional.ofNullable(sessionsByUuid.get(uuid));
    }

    /**
     * 세션 제거 (세션 ID 기준)
     * 동시성 문제 방지를 위해 동기화 처리
     */
    public synchronized void unregisterBySessionId(String sessionId) {
        if (sessionId == null) {
            log.warn("세션 ID가 null이어서 제거할 수 없습니다");
            return;
        }

        PlayerSession session = sessionsBySessionId.remove(sessionId);
        if (session != null) {
            sessionsByUuid.remove(session.getUuid());
            log.info("플레이어 세션 제거: sessionId={}, uuid={}, nickname={}",
                    sessionId, session.getUuid(), session.getNickname());
        }
    }

    /**
     * 세션 제거 (UUID 기준)
     * 동시성 문제 방지를 위해 동기화 처리
     */
    public synchronized void unregisterByUuid(String uuid) {
        if (uuid == null) {
            log.warn("UUID가 null이어서 제거할 수 없습니다");
            return;
        }

        PlayerSession session = sessionsByUuid.remove(uuid);
        if (session != null) {
            sessionsBySessionId.remove(session.getSessionId());
            log.info("플레이어 세션 제거: uuid={}, sessionId={}, nickname={}",
                    uuid, session.getSessionId(), session.getNickname());
        }
    }

    /**
     * 전체 세션 수
     */
    public int size() {
        return sessionsBySessionId.size();
    }

    /**
     * UUID가 이미 등록되어 있는지 확인
     */
    public boolean containsUuid(String uuid) {
        return sessionsByUuid.containsKey(uuid);
    }

    /**
     * 세션 ID가 이미 등록되어 있는지 확인
     */
    public boolean containsSessionId(String sessionId) {
        return sessionsBySessionId.containsKey(sessionId);
    }

    /**
     * 특정 방에 속한 모든 플레이어 세션 조회
     */
    public List<PlayerSession> findAllByRoomId(String roomId) {
        return sessionsBySessionId.values().stream()
                .filter(session -> session.isInRoom() && roomId.equals(session.getCurrentRoomId()))
                .collect(Collectors.toList());
    }
}
