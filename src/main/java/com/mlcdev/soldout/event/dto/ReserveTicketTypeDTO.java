package com.mlcdev.soldout.event.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record ReserveTicketTypeDTO(

        @NotNull(message = "{ticket-type.id.required}")
        UUID id,

        @NotNull(message = "{ticket-type.quantity.required}")
        @Positive(message = "{ticket-type.quantity.positive}")
        Integer quantity

) {
}
