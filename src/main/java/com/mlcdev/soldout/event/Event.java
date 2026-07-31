package com.mlcdev.soldout.event;

import com.mlcdev.soldout.event.exception.InvalidEventException;
import com.mlcdev.soldout.event.exception.InvalidEventPeriodException;
import com.mlcdev.soldout.event.exception.InvalidTicketTypeException;
import com.mlcdev.soldout.event.exception.TicketTypeNotFoundException;
import com.mlcdev.soldout.shared.persistence.BaseEntity;
import com.mlcdev.soldout.shared.utils.IdGenerator;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;


@Entity
@Table(name = "events")
// equals uses getters so lazy proxies initialize — do not use field access
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Event extends BaseEntity {

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

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    @Getter(AccessLevel.NONE)
    private Set<TicketType> ticketTypes = new HashSet<>();

    @Version
    private Long version;

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
        ensureModifiable();
        this.name = name.trim();
        this.description = description.trim();
    }

    public void reschedule(@NonNull Instant startsAt, @NonNull Instant endsAt) {
        ensureModifiable();
        validatePeriod(startsAt, endsAt);

        this.startsAt = startsAt;
        this.endsAt = endsAt;
    }

    public void publish() {
        if (status != EventStatus.DRAFT) {
            throw new InvalidEventException(
                    "only draft events can be published, current status is " + status);
        }
        if (ticketTypes.isEmpty()) {
            throw new InvalidEventException(
                    "events without ticket types can't be published"
            );
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

    public void restore() {

        if (status != EventStatus.CANCELLED) {
            throw new InvalidEventException(
                    "only cancelled events can be restored, current status is " + status);
        }

        this.status = EventStatus.DRAFT;
    }

    public TicketType addTicketType(@NonNull String ticketName, @NonNull String ticketDescription, @NonNull BigDecimal ticketPrice, int ticketTotalQuantity) {
        ensureModifiable();
        TicketType ticketType = new TicketType(ticketName, ticketDescription, ticketPrice, ticketTotalQuantity, this);
        ticketTypes.add(ticketType);
        return ticketType;
    }

    public void removeTicketType(@NonNull UUID ticketTypeId) {
        ensureModifiable();
        TicketType found = ticketTypes.stream()
                .filter(t -> Objects.equals(t.getId(), ticketTypeId))
                .findFirst()
                .orElseThrow(() -> new TicketTypeNotFoundException(ticketTypeId));
        if (found.getAvailableQuantity() != found.getTotalQuantity()) {
            throw new InvalidTicketTypeException("cannot remove a ticket type with reserved tickets");
        }
        ticketTypes.remove(found);

    }

    public void ensureOpenForReservation() {
        if (status != EventStatus.PUBLISHED) {
            throw new InvalidEventException(
                    "tickets can only be reserved for published events, current status is " + status);
        }
        if (!endsAt.isAfter(Instant.now())) {
            throw new InvalidEventException("tickets cannot be reserved for an event that has already ended");
        }
    }

    public void ensureModifiable() {
        if (status == EventStatus.FINISHED || status == EventStatus.CANCELLED) {
            throw new InvalidEventException("cannot modify an event with status " + status);
        }
    }

    public Set<TicketType> getTicketTypes() {
        return Collections.unmodifiableSet(ticketTypes);
    }

    private void validatePeriod(Instant startsAt, Instant endsAt) {
        if (!endsAt.isAfter(startsAt)) {
            throw new InvalidEventPeriodException("event endsAt must be after startsAt");
        }
    }

}