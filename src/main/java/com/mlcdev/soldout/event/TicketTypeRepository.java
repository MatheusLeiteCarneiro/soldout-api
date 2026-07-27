package com.mlcdev.soldout.event;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;


@Repository
interface TicketTypeRepository extends JpaRepository<TicketType, UUID> {
}
