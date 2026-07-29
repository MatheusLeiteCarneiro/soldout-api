package com.mlcdev.soldout.event.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record TicketTypeDTO(UUID id, String name, String description, BigDecimal price, int availableQuantity) {
}
