package com.mlcdev.soldout.event.dtos;

import com.mlcdev.soldout.event.EventStatus;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record EventDetailDTO(UUID id, String name, String description, Instant startsAt, Instant endsAt, EventStatus status, Set<TicketTypeDTO> ticketTypes) {
}
