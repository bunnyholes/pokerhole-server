package dev.xiyo.pokerhole.adapter.in.websocket.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 메시지 직렬화/역직렬화 담당
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MessageCodec {

    private final ObjectMapper objectMapper;

    /**
     * ClientMessage를 JSON 문자열로 변환
     */
    public String encode(ClientMessage message) throws JsonProcessingException {
        return objectMapper.writeValueAsString(message);
    }

    /**
     * ServerMessage를 JSON 문자열로 변환
     */
    public String encode(ServerMessage message) throws JsonProcessingException {
        return objectMapper.writeValueAsString(message);
    }

    /**
     * JSON 문자열을 ClientMessage로 변환
     */
    public ClientMessage decodeClientMessage(String json) throws JsonProcessingException {
        return objectMapper.readValue(json, ClientMessage.class);
    }

    /**
     * JSON 문자열을 ServerMessage로 변환
     */
    public ServerMessage decodeServerMessage(String json) throws JsonProcessingException {
        return objectMapper.readValue(json, ServerMessage.class);
    }
}
