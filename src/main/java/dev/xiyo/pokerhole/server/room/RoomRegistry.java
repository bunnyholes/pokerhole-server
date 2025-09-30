package dev.xiyo.pokerhole.server.room;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Component
public class RoomRegistry {
    private static final String ROOM_ID_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom random = new SecureRandom();

    private final Map<String, GameRoom> rooms = new ConcurrentHashMap<>();

    public GameRoom createRoom(String name) {
        String id = nextRoomId();
        GameRoom room = new GameRoom(id, name);
        rooms.put(id, room);
        log.info("Created room {} ({})", id, name);
        return room;
    }

    public Optional<GameRoom> findRoom(String id) {
        return Optional.ofNullable(rooms.get(id));
    }

    public List<GameRoomSummary> listRooms() {
        return rooms.values().stream()
                .map(GameRoom::summary)
                .sorted(Comparator.comparing(GameRoomSummary::createdAt))
                .collect(Collectors.toList());
    }

    public void removeRoom(String id) {
        GameRoom removed = rooms.remove(id);
        if (removed != null) {
            log.info("Removed empty room {} ({})", id, removed.name());
        }
    }

    private String nextRoomId() {
        while (true) {
            String candidate = random.ints(6, 0, ROOM_ID_ALPHABET.length())
                    .mapToObj(ROOM_ID_ALPHABET::charAt)
                    .map(String::valueOf)
                    .collect(Collectors.joining())
                    .toUpperCase(Locale.ROOT);
            if (!rooms.containsKey(candidate)) {
                return candidate;
            }
        }
    }
}
