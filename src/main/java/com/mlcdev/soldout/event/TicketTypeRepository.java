package com.mlcdev.soldout.event;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;


@Repository
interface TicketTypeRepository extends JpaRepository<TicketType, UUID> {

    @Query("SELECT tt FROM TicketType tt JOIN FETCH tt.event WHERE tt.id = :uuid")
    Optional<TicketType> findByIdWithEvent(@Param("uuid") UUID uuid);
}
