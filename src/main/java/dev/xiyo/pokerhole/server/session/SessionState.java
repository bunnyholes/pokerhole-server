package dev.xiyo.pokerhole.server.session;

import dev.xiyo.pokerhole.player.Player;
import dev.xiyo.pokerhole.server.room.GameRoom;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class SessionState {
    @Getter
    private final String id = UUID.randomUUID().toString();
    private final ParticipantConnection connection;

    private GameRoom room;
    private Player player;
    @Getter
    private boolean host;

    public String id() {
        return id;
    }

    public void send(String message) {
        if (connection.isOpen()) {
            connection.send(message);
        }
    }

    public void closeConnection() {
        connection.close();
    }

    public Optional<GameRoom> currentRoom() {
        return Optional.ofNullable(room);
    }

    public Optional<Player> player() {
        return Optional.ofNullable(player);
    }

    public void attachToRoom(GameRoom room, Player player, boolean host) {
        this.room = room;
        this.player = player;
        this.host = host;
    }

    public void promoteToHost() {
        this.host = true;
    }

    public void demoteFromHost() {
        this.host = false;
    }

    public void detachFromRoom() {
        this.room = null;
        this.player = null;
        this.host = false;
    }
}
