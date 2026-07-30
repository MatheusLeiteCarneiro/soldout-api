package com.mlcdev.soldout.event.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.Length;

import java.time.Instant;

public record EventUpdateDTO(@Pattern(regexp = "(?s).*\\S.*", message = "{event.name.blank}")
                             @Length(max = 150, message = "{event.name.length}")
                             String name,

                             @Pattern(regexp = "(?s).*\\S.*", message = "{event.description.blank}")
                             @Length(max = 2500, message = "{event.description.length}")
                             String description,

                             @FutureOrPresent(message = "{event.startsAt.future}")
                             Instant startsAt,

                             @FutureOrPresent(message = "{event.endsAt.future}")
                             Instant endsAt) {
}
