package com.mlcdev.soldout.event;

import com.mlcdev.soldout.shared.IdGenerator;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.time.Instant;
import java.util.UUID;


@Entity
@Table(name = "events")
// equals uses getters so lazy proxies initialize — do not use field access
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Event {

    @Id
    @EqualsAndHashCode.Include
    private UUID id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 2500)
    private String description;

    @Column(name = "starts_at", nullable = false)
    private Instant startsAt;

    @Column(name = "ends_at", nullable = false)
    private Instant endsAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EventStatus status;

    public Event(@NonNull String name,
                 @NonNull String description,
                 @NonNull Instant startsAt,
                 @NonNull Instant endsAt) {

        validatePeriod(startsAt, endsAt);

        this.id = IdGenerator.newId();
        this.name = name.trim();
        this.description = description.trim();
        this.startsAt = startsAt;
        this.endsAt = endsAt;
        this.status = EventStatus.DRAFT;
    }

    public void updateDetails(@NonNull String name, @NonNull String description) {
        ensureEditable();
        this.name = name.trim();
        this.description = description.trim();
    }

    public void reschedule(@NonNull Instant startsAt, @NonNull Instant endsAt) {
        ensureEditable();
        validatePeriod(startsAt, endsAt);

        this.startsAt = startsAt;
        this.endsAt = endsAt;
    }

    public void publish() {
        if (status != EventStatus.DRAFT) {
            throw new InvalidEventException(
                    "only draft events can be published, current status is " + status);
        }
        this.status = EventStatus.PUBLISHED;
    }

    public void cancel() {
        if (status == EventStatus.FINISHED) {
            throw new InvalidEventException("a finished event cannot be cancelled");
        }
        if (status == EventStatus.CANCELLED) {
            throw new InvalidEventException("the event is already cancelled");
        }
        this.status = EventStatus.CANCELLED;
    }

    public void finish() {
        if (status != EventStatus.PUBLISHED) {
            throw new InvalidEventException(
                    "only published events can be finished, current status is " + status);
        }
        this.status = EventStatus.FINISHED;
    }

    private void validatePeriod(Instant startsAt, Instant endsAt) {
        if (!endsAt.isAfter(startsAt)) {
            throw new InvalidEventException("endsAt must be after startsAt");
        }
    }

    private void ensureEditable() {
        if (status == EventStatus.FINISHED || status == EventStatus.CANCELLED) {
            throw new InvalidEventException("cannot modify an event with status " + status);
        }
    }
}