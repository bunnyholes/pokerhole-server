package dev.xiyo.pokerhole.adapter.in.websocket.message;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 클라이언트 → 서버 메시지
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ClientMessage {
    private ClientMessageType type;
    private Long timestamp;
    private Map<String, Object> payload;
}
