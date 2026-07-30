package com.mlcdev.soldout.event.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;


public record ReserveTicketTypeDTO(
        @NotNull(message = "{ticket-type.quantity.required}")
        @Positive(message = "{ticket-type.quantity.positive}")
        Integer quantity

) {
}
