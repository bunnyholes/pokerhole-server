package com.pokerhole.server.support;

import com.pokerhole.server.session.ParticipantConnection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 테스트에서 세션 간 메시지 교환을 검증하기 위한 인메모리 구현체.
 */
public class InMemoryConnection implements ParticipantConnection {
    private final List<String> messages = new ArrayList<>();
    private boolean open = true;

    @Override
    public void send(String message) {
        if (open) {
            messages.add(message);
        }
    }

    @Override
    public boolean isOpen() {
        return open;
    }

    @Override
    public void close() {
        open = false;
    }

    public List<String> messages() {
        return Collections.unmodifiableList(messages);
    }

    public void clear() {
        messages.clear();
    }
}
