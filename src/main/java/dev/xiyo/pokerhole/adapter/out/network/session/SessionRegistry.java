package dev.xiyo.pokerhole.adapter.out.network.session;

import dev.xiyo.pokerhole.adapter.out.network.session.model.SessionState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 세션 레지스트리
 * 세션 ID와 SessionState 매핑 관리
 */
@Slf4j
@Component
public class SessionRegistry {

    private final ConcurrentHashMap<String, SessionState> sessions = new ConcurrentHashMap<>();

    /**
     * 세션 등록
     */
    public void register(String sessionId, SessionState state) {
        sessions.put(sessionId, state);
        log.info("세션 등록: sessionId={}", sessionId);
    }

    /**
     * 세션 조회
     */
    public Optional<SessionState> findById(String sessionId) {
        return Optional.ofNullable(sessions.get(sessionId));
    }

    /**
     * 세션 제거
     */
    public void unregister(String sessionId) {
        SessionState removed = sessions.remove(sessionId);
        if (removed != null) {
            log.info("세션 제거: sessionId={}", sessionId);
        }
    }

    /**
     * 전체 세션 수
     */
    public int size() {
        return sessions.size();
    }

    /**
     * 모든 세션 ID 조회
     */
    public java.util.Set<String> getAllSessionIds() {
        return sessions.keySet();
    }
}
