package com.pokerhole.server.room;

import java.time.Instant;

public record GameRoomSummary(String id, String name, int participants, int capacity, Instant createdAt) {
}
