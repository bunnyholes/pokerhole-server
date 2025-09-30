package dev.xiyo.pokerhole.server.guest;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "guest_visits")
public class GuestVisit {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "guest_number", nullable = false, unique = true, updatable = false)
    private Long guestNumber;

    @Column(name = "session_id", nullable = false, unique = true, updatable = false)
    private String sessionId;

    @Column(name = "connected_at", nullable = false, updatable = false)
    private Instant connectedAt;
}
