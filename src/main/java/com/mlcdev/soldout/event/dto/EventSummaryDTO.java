package com.mlcdev.soldout.event.dto;

import java.time.Instant;
import java.util.UUID;

public record EventSummaryDTO(UUID id, String name, String description, Instant startsAt) {
}
