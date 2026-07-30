package com.mlcdev.soldout.event.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.hibernate.validator.constraints.Length;

import java.math.BigDecimal;


public record TicketTypeInsertDTO(
        @NotBlank(message = "{ticket-type.name.required}")
        @Length(max = 100, message = "{ticket-type.name.length}")
        String name,

        @NotBlank(message = "{ticket-type.description.required}")
        @Length(max = 500, message = "{ticket-type.description.length}")
        String description,

        @NotNull(message = "{ticket-type.price.required}")
        @PositiveOrZero(message = "{ticket-type.price.positive-or-zero}")
        BigDecimal price,

        @NotNull(message = "{ticket-type.total-quantity.required}")
        @Positive(message = "{ticket-type.total-quantity.positive}")
        Integer totalQuantity) {

}
