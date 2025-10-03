package dev.xiyo.pokerhole.adapter.out.persistence.jpa.repository;

import dev.xiyo.pokerhole.adapter.out.persistence.jpa.entity.HandEventEntity;
import dev.xiyo.pokerhole.adapter.out.persistence.jpa.entity.HandHistoryEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for HandEventJpaRepository.
 *
 * Tests event replay, player queries, and JSONB queries.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class HandEventJpaRepositoryTest {

    @Autowired
    private HandHistoryJpaRepository handHistoryRepository;

    @Autowired
    private HandEventJpaRepository handEventRepository;

    @Autowired
    private TestEntityManager entityManager;

    private UUID gameId;
    private UUID player1Id;
    private UUID player2Id;
    private HandHistoryEntity handHistory;

    @BeforeEach
    void setUp() {
        // Clean up
        handEventRepository.deleteAll();
        handHistoryRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();

        // Initialize test data
        gameId = UUID.randomUUID();
        player1Id = UUID.randomUUID();
        player2Id = UUID.randomUUID();

        handHistory = HandHistoryEntity.builder()
            .handId(UUID.randomUUID())
            .gameId(gameId)
            .handNumber(1)
            .startedAt(Instant.now())
            .endedAt(Instant.now())
            .finalPot(1000L)
            .winnerIds(List.of(player1Id))
            .deckSeed(12345L)
            .dealerButtonPosition(0)
            .smallBlind(50L)
            .bigBlind(100L)
            .createdAt(Instant.now())
            .events(new ArrayList<>())
            .build();

        handHistoryRepository.save(handHistory);
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("Should save and retrieve HandEventEntity")
    void shouldSaveAndRetrieveHandEvent() {
        // Given
        HandEventEntity event = createEvent(handHistory.getHandId(), 1, "PlayerActed", player1Id, "{\"action\":\"CALL\"}");

        // When
        HandEventEntity saved = handEventRepository.save(event);
        entityManager.flush();
        entityManager.clear();

        HandEventEntity retrieved = handEventRepository.findById(saved.getEventId()).orElseThrow();

        // Then
        assertThat(retrieved.getEventId()).isEqualTo(saved.getEventId());
        assertThat(retrieved.getEventSequence()).isEqualTo(1);
        assertThat(retrieved.getEventType()).isEqualTo("PlayerActed");
        assertThat(retrieved.getPlayerId()).isEqualTo(player1Id);
        assertThat(retrieved.getEventData()).contains("CALL");
    }

    @Test
    @DisplayName("Should find events by hand ID ordered by sequence (replay)")
    void shouldFindByHandIdOrderByEventSequence() {
        // Given
        HandEventEntity event1 = createEvent(handHistory.getHandId(), 1, "RoundStarted", null, "{}");
        HandEventEntity event2 = createEvent(handHistory.getHandId(), 2, "PlayerActed", player1Id, "{\"action\":\"CALL\"}");
        HandEventEntity event3 = createEvent(handHistory.getHandId(), 3, "PlayerActed", player2Id, "{\"action\":\"RAISE\"}");
        HandEventEntity event4 = createEvent(handHistory.getHandId(), 4, "RoundProgressed", null, "{}");

        handEventRepository.saveAll(List.of(event3, event1, event4, event2)); // Save out of order
        entityManager.flush();
        entityManager.clear();

        // When
        List<HandEventEntity> events = handEventRepository.findByHandIdOrderByEventSequence(handHistory.getHandId());

        // Then
        assertThat(events).hasSize(4);
        assertThat(events.get(0).getEventSequence()).isEqualTo(1);
        assertThat(events.get(1).getEventSequence()).isEqualTo(2);
        assertThat(events.get(2).getEventSequence()).isEqualTo(3);
        assertThat(events.get(3).getEventSequence()).isEqualTo(4);
    }

    @Test
    @DisplayName("Should find events by hand ID and event type")
    void shouldFindByHandIdAndEventType() {
        // Given
        HandEventEntity event1 = createEvent(handHistory.getHandId(), 1, "PlayerActed", player1Id, "{\"action\":\"CALL\"}");
        HandEventEntity event2 = createEvent(handHistory.getHandId(), 2, "RoundProgressed", null, "{}");
        HandEventEntity event3 = createEvent(handHistory.getHandId(), 3, "PlayerActed", player2Id, "{\"action\":\"RAISE\"}");

        handEventRepository.saveAll(List.of(event1, event2, event3));
        entityManager.flush();
        entityManager.clear();

        // When
        List<HandEventEntity> playerActions = handEventRepository.findByHandIdAndEventType(
            handHistory.getHandId(), "PlayerActed"
        );
        List<HandEventEntity> roundProgressions = handEventRepository.findByHandIdAndEventType(
            handHistory.getHandId(), "RoundProgressed"
        );

        // Then
        assertThat(playerActions).hasSize(2);
        assertThat(playerActions.get(0).getEventSequence()).isEqualTo(1);
        assertThat(playerActions.get(1).getEventSequence()).isEqualTo(3);

        assertThat(roundProgressions).hasSize(1);
        assertThat(roundProgressions.get(0).getEventSequence()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should find events by player ID")
    void shouldFindByPlayerIdOrderByOccurredAtDesc() {
        // Given
        Instant now = Instant.now();

        HandEventEntity event1 = createEvent(handHistory.getHandId(), 1, "PlayerActed", player1Id, "{\"action\":\"CALL\"}");
        event1.setOccurredAt(now.minusSeconds(30));

        HandEventEntity event2 = createEvent(handHistory.getHandId(), 2, "PlayerActed", player2Id, "{\"action\":\"RAISE\"}");
        event2.setOccurredAt(now.minusSeconds(20));

        HandEventEntity event3 = createEvent(handHistory.getHandId(), 3, "PlayerActed", player1Id, "{\"action\":\"FOLD\"}");
        event3.setOccurredAt(now.minusSeconds(10));

        handEventRepository.saveAll(List.of(event1, event2, event3));
        entityManager.flush();
        entityManager.clear();

        // When
        List<HandEventEntity> player1Actions = handEventRepository.findByPlayerIdOrderByOccurredAtDesc(player1Id);
        List<HandEventEntity> player2Actions = handEventRepository.findByPlayerIdOrderByOccurredAtDesc(player2Id);

        // Then
        assertThat(player1Actions).hasSize(2);
        assertThat(player1Actions.get(0).getEventSequence()).isEqualTo(3); // Most recent first
        assertThat(player1Actions.get(1).getEventSequence()).isEqualTo(1);

        assertThat(player2Actions).hasSize(1);
        assertThat(player2Actions.get(0).getEventSequence()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should find events by player ID and event type")
    void shouldFindByPlayerIdAndEventTypeOrderByOccurredAtDesc() {
        // Given
        HandEventEntity event1 = createEvent(handHistory.getHandId(), 1, "PlayerActed", player1Id, "{\"action\":\"CALL\"}");
        HandEventEntity event2 = createEvent(handHistory.getHandId(), 2, "TurnChanged", player1Id, "{}");
        HandEventEntity event3 = createEvent(handHistory.getHandId(), 3, "PlayerActed", player1Id, "{\"action\":\"FOLD\"}");

        handEventRepository.saveAll(List.of(event1, event2, event3));
        entityManager.flush();
        entityManager.clear();

        // When
        List<HandEventEntity> playerActions = handEventRepository.findByPlayerIdAndEventTypeOrderByOccurredAtDesc(
            player1Id, "PlayerActed"
        );
        List<HandEventEntity> turnChanges = handEventRepository.findByPlayerIdAndEventTypeOrderByOccurredAtDesc(
            player1Id, "TurnChanged"
        );

        // Then
        assertThat(playerActions).hasSize(2);
        assertThat(turnChanges).hasSize(1);
    }

    @Test
    @DisplayName("Should find events by time range")
    void shouldFindByOccurredAtBetween() {
        // Given
        Instant now = Instant.now();
        Instant oneHourAgo = now.minusSeconds(3600);

        HandEventEntity event1 = createEvent(handHistory.getHandId(), 1, "PlayerActed", player1Id, "{}");
        event1.setOccurredAt(oneHourAgo);

        HandEventEntity event2 = createEvent(handHistory.getHandId(), 2, "PlayerActed", player2Id, "{}");
        event2.setOccurredAt(now.minusSeconds(1800));

        HandEventEntity event3 = createEvent(handHistory.getHandId(), 3, "PlayerActed", player1Id, "{}");
        event3.setOccurredAt(now);

        handEventRepository.saveAll(List.of(event1, event2, event3));
        entityManager.flush();
        entityManager.clear();

        // When
        List<HandEventEntity> lastHalfHourEvents = handEventRepository.findByOccurredAtBetweenOrderByOccurredAtDesc(
            now.minusSeconds(1800), now.plusSeconds(1)
        );

        // Then
        assertThat(lastHalfHourEvents).hasSize(2);
        assertThat(lastHalfHourEvents.get(0).getEventSequence()).isEqualTo(3);
        assertThat(lastHalfHourEvents.get(1).getEventSequence()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should find events by event type")
    void shouldFindByEventTypeOrderByOccurredAtDesc() {
        // Given
        HandEventEntity event1 = createEvent(handHistory.getHandId(), 1, "PlayerActed", player1Id, "{}");
        HandEventEntity event2 = createEvent(handHistory.getHandId(), 2, "RoundProgressed", null, "{}");
        HandEventEntity event3 = createEvent(handHistory.getHandId(), 3, "PlayerActed", player2Id, "{}");

        handEventRepository.saveAll(List.of(event1, event2, event3));
        entityManager.flush();
        entityManager.clear();

        // When
        List<HandEventEntity> playerActions = handEventRepository.findByEventTypeOrderByOccurredAtDesc("PlayerActed");

        // Then
        assertThat(playerActions).hasSize(2);
    }

    @Test
    @DisplayName("Should count events by hand ID")
    void shouldCountByHandId() {
        // Given
        HandEventEntity event1 = createEvent(handHistory.getHandId(), 1, "PlayerActed", player1Id, "{}");
        HandEventEntity event2 = createEvent(handHistory.getHandId(), 2, "RoundProgressed", null, "{}");
        HandEventEntity event3 = createEvent(handHistory.getHandId(), 3, "PlayerActed", player2Id, "{}");

        handEventRepository.saveAll(List.of(event1, event2, event3));
        entityManager.flush();
        entityManager.clear();

        // When
        long count = handEventRepository.countByHandId(handHistory.getHandId());

        // Then
        assertThat(count).isEqualTo(3);
    }

    @Test
    @DisplayName("Should count events by hand ID and event type")
    void shouldCountByHandIdAndEventType() {
        // Given
        HandEventEntity event1 = createEvent(handHistory.getHandId(), 1, "PlayerActed", player1Id, "{}");
        HandEventEntity event2 = createEvent(handHistory.getHandId(), 2, "RoundProgressed", null, "{}");
        HandEventEntity event3 = createEvent(handHistory.getHandId(), 3, "PlayerActed", player2Id, "{}");

        handEventRepository.saveAll(List.of(event1, event2, event3));
        entityManager.flush();
        entityManager.clear();

        // When
        long playerActionCount = handEventRepository.countByHandIdAndEventType(
            handHistory.getHandId(), "PlayerActed"
        );
        long roundProgressionCount = handEventRepository.countByHandIdAndEventType(
            handHistory.getHandId(), "RoundProgressed"
        );

        // Then
        assertThat(playerActionCount).isEqualTo(2);
        assertThat(roundProgressionCount).isEqualTo(1);
    }

    @Test
    @DisplayName("Should find last event by hand ID")
    void shouldFindLastEventByHandId() {
        // Given
        HandEventEntity event1 = createEvent(handHistory.getHandId(), 1, "PlayerActed", player1Id, "{}");
        HandEventEntity event2 = createEvent(handHistory.getHandId(), 2, "RoundProgressed", null, "{}");
        HandEventEntity event3 = createEvent(handHistory.getHandId(), 3, "PlayerActed", player2Id, "{}");

        handEventRepository.saveAll(List.of(event1, event2, event3));
        entityManager.flush();
        entityManager.clear();

        // When
        HandEventEntity lastEvent = handEventRepository.findLastEventByHandId(handHistory.getHandId());

        // Then
        assertThat(lastEvent).isNotNull();
        assertThat(lastEvent.getEventSequence()).isEqualTo(3);
    }

    @Test
    @DisplayName("Should find events by JSONB query")
    void shouldFindByHandIdAndEventDataContains() {
        // Given
        HandEventEntity event1 = createEvent(handHistory.getHandId(), 1, "PlayerActed", player1Id, "{\"action\":\"CALL\",\"amount\":100}");
        HandEventEntity event2 = createEvent(handHistory.getHandId(), 2, "PlayerActed", player2Id, "{\"action\":\"RAISE\",\"amount\":200}");
        HandEventEntity event3 = createEvent(handHistory.getHandId(), 3, "PlayerActed", player1Id, "{\"action\":\"FOLD\"}");

        handEventRepository.saveAll(List.of(event1, event2, event3));
        entityManager.flush();
        entityManager.clear();

        // When
        List<HandEventEntity> raiseActions = handEventRepository.findByHandIdAndEventDataContains(
            handHistory.getHandId(), "{\"action\":\"RAISE\"}"
        );

        // Then
        assertThat(raiseActions).hasSize(1);
        assertThat(raiseActions.get(0).getEventSequence()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should enforce unique constraint on hand_id and event_sequence")
    void shouldEnforceUniqueConstraint() {
        // Given
        HandEventEntity event1 = createEvent(handHistory.getHandId(), 1, "PlayerActed", player1Id, "{}");
        HandEventEntity event2 = createEvent(handHistory.getHandId(), 1, "PlayerActed", player2Id, "{}"); // Same sequence

        handEventRepository.save(event1);
        entityManager.flush();

        // When/Then
        org.junit.jupiter.api.Assertions.assertThrows(
            Exception.class,
            () -> {
                handEventRepository.save(event2);
                entityManager.flush();
            }
        );
    }

    // Helper methods

    private HandEventEntity createEvent(UUID handId, int sequence, String eventType, UUID playerId, String eventData) {
        HandHistoryEntity hand = handHistoryRepository.findById(handId).orElseThrow();
        return HandEventEntity.builder()
            .eventId(UUID.randomUUID())
            .handHistory(hand)
            .eventSequence(sequence)
            .eventType(eventType)
            .playerId(playerId)
            .eventData(eventData)
            .occurredAt(Instant.now())
            .build();
    }
}
