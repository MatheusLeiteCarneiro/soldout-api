package com.mlcdev.soldout.event.dto;

import com.mlcdev.soldout.event.validation.EventPeriod;
import com.mlcdev.soldout.event.validation.ValidEventPeriod;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

import java.time.Instant;


@ValidEventPeriod
public record EventInsertDTO(@NotBlank(message = "{event.name.required}")
                             @Length(max = 150, message = "{event.name.length}")
                             String name,

                             @NotBlank(message = "{event.description.required}")
                             @Length(max = 2500, message = "{event.description.length}")
                             String description,

                             @NotNull(message = "{event.startsAt.required}")
                             @FutureOrPresent(message = "{event.startsAt.future}")
                             Instant startsAt,

                             @NotNull(message = "{event.endsAt.required}")
                             @FutureOrPresent(message = "{event.endsAt.future}")
                             Instant endsAt) implements EventPeriod {
}
