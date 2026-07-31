package com.mlcdev.soldout.event.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import org.hibernate.validator.constraints.Length;

import java.math.BigDecimal;

public record UpdateTicketTypeDTO(
        @Pattern(regexp = "(?s).*\\S.*", message = "{ticket-type.name.blank}")
        @Length(max = 100, message = "{ticket-type.name.length}")
        String name,

        @Pattern(regexp = "(?s).*\\S.*", message = "{ticket-type.description.blank}")
        @Length(max = 500, message = "{ticket-type.description.length}")
        String description,

        @PositiveOrZero(message = "{ticket-type.price.positive-or-zero}")
        BigDecimal price
) {
}
