package com.pokerhole.server.session;

public interface ParticipantConnection {
    void send(String message);

    boolean isOpen();

    void close();
}
