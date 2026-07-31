package com.mlcdev.soldout.event;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
interface EventRepository extends JpaRepository<Event, UUID> {

    @Query("SELECT e FROM Event e LEFT JOIN FETCH e.ticketTypes WHERE e.id = :uuid")
    Optional<Event> findByIdWithTicketTypes(@Param("uuid") UUID uuid);

    Page<Event> findAllByStatusAndEndsAtAfter(EventStatus status, Instant instant, Pageable pageable);

    default Page<Event> findAllAvailable(Pageable pageable) {
        return findAllByStatusAndEndsAtAfter(EventStatus.PUBLISHED, Instant.now(), pageable);
    }

    @Query("SELECT e.id FROM Event e WHERE e.status = :status AND e.endsAt <= :instant")
    List<UUID> findAllIdsPendingFinish(@Param("status") EventStatus status, @Param("instant") Instant instant);

    default List<UUID> findAllIdsPendingFinish(){
        return findAllIdsPendingFinish(EventStatus.PUBLISHED, Instant.now());
    }
}
