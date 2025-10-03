package dev.xiyo.pokerhole.adapter.in.websocket.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Custom Micrometer metrics for PokerHole game events.
 *
 * Provides metrics for:
 * - Active games count
 * - Games started/ended
 * - Active players count
 * - Player actions (FOLD, CHECK, CALL, RAISE, ALL_IN)
 * - Hand duration
 */
@Component
public class GameMetrics {

    private final MeterRegistry meterRegistry;

    // Gauges (current values)
    private final AtomicInteger activeGamesCount;
    private final AtomicInteger activePlayersCount;

    // Counters (cumulative)
    private final Counter gamesStartedCounter;
    private final Counter gamesEndedCounter;

    // Timer (duration tracking)
    private final Timer handDurationTimer;

    public GameMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        // Initialize atomic counters for gauges
        this.activeGamesCount = new AtomicInteger(0);
        this.activePlayersCount = new AtomicInteger(0);

        // Register gauges
        Gauge.builder("pokerhole.games.active", activeGamesCount, AtomicInteger::get)
                .description("Number of currently active games")
                .register(meterRegistry);

        Gauge.builder("pokerhole.players.active", activePlayersCount, AtomicInteger::get)
                .description("Number of currently active players")
                .register(meterRegistry);

        // Register counters
        this.gamesStartedCounter = Counter.builder("pokerhole.games.started.total")
                .description("Total number of games started")
                .register(meterRegistry);

        this.gamesEndedCounter = Counter.builder("pokerhole.games.ended.total")
                .description("Total number of games ended")
                .register(meterRegistry);

        // Register timer
        this.handDurationTimer = Timer.builder("pokerhole.hands.duration.seconds")
                .description("Duration of poker hands")
                .register(meterRegistry);
    }

    /**
     * Increment active games count
     */
    public void incrementActiveGames() {
        activeGamesCount.incrementAndGet();
    }

    /**
     * Decrement active games count
     */
    public void decrementActiveGames() {
        activeGamesCount.decrementAndGet();
    }

    /**
     * Set active games count (for batch updates)
     */
    public void setActiveGames(int count) {
        activeGamesCount.set(count);
    }

    /**
     * Increment active players count
     */
    public void incrementActivePlayers() {
        activePlayersCount.incrementAndGet();
    }

    /**
     * Decrement active players count
     */
    public void decrementActivePlayers() {
        activePlayersCount.decrementAndGet();
    }

    /**
     * Set active players count (for batch updates)
     */
    public void setActivePlayers(int count) {
        activePlayersCount.set(count);
    }

    /**
     * Increment games started counter and active games
     */
    public void incrementGamesStarted() {
        gamesStartedCounter.increment();
        incrementActiveGames();
    }

    /**
     * Increment games ended counter and decrement active games
     */
    public void incrementGamesEnded() {
        gamesEndedCounter.increment();
        decrementActiveGames();
    }

    /**
     * Record player action with type tag
     *
     * @param actionType FOLD, CHECK, CALL, RAISE, ALL_IN
     */
    public void incrementPlayerAction(String actionType) {
        Counter.builder("pokerhole.actions.total")
                .description("Total number of player actions")
                .tag("action_type", actionType)
                .register(meterRegistry)
                .increment();
    }

    /**
     * Record hand duration
     *
     * @param duration Duration of the hand
     */
    public void recordHandDuration(Duration duration) {
        handDurationTimer.record(duration);
    }

    /**
     * Record hand duration in milliseconds
     *
     * @param durationMs Duration in milliseconds
     */
    public void recordHandDurationMs(long durationMs) {
        recordHandDuration(Duration.ofMillis(durationMs));
    }

    /**
     * Get current active games count
     */
    public int getActiveGamesCount() {
        return activeGamesCount.get();
    }

    /**
     * Get current active players count
     */
    public int getActivePlayersCount() {
        return activePlayersCount.get();
    }

    /**
     * Get total games started
     */
    public double getTotalGamesStarted() {
        return gamesStartedCounter.count();
    }

    /**
     * Get total games ended
     */
    public double getTotalGamesEnded() {
        return gamesEndedCounter.count();
    }
}
