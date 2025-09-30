package dev.xiyo.pokerhole.adapter.out.network.session.model;

public interface ParticipantConnection {
    void send(String message);

    boolean isOpen();

    void close();
}
