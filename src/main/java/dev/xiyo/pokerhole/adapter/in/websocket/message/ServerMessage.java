package dev.xiyo.pokerhole.adapter.in.websocket.message;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 서버 → 클라이언트 메시지
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ServerMessage {
    private ServerMessageType type;
    private Long timestamp;
    private Map<String, Object> payload;

    /**
     * 현재 타임스탬프로 메시지 생성
     */
    public static ServerMessage of(ServerMessageType type, Map<String, Object> payload) {
        return ServerMessage.builder()
                .type(type)
                .timestamp(System.currentTimeMillis())
                .payload(payload)
                .build();
    }

    /**
     * 페이로드 없는 메시지 생성
     */
    public static ServerMessage of(ServerMessageType type) {
        return of(type, null);
    }
}
