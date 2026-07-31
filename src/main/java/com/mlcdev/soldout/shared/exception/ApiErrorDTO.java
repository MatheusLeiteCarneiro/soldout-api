package com.mlcdev.soldout.shared.exception;


import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

public record ApiErrorDTO
        (Instant timestamp,
         Integer status,
         String error,
         String path,
         @JsonInclude(JsonInclude.Include.NON_EMPTY) List<FieldErrorDTO> errors
        ) {

    public ApiErrorDTO(Integer status, String error, String path) {
        this(Instant.now(), status, error, path, List.of());
    }
    public ApiErrorDTO(Integer status, String error, String path, List<FieldErrorDTO> fieldErrors) {
        this(Instant.now(), status, error, path, fieldErrors);
    }

}
