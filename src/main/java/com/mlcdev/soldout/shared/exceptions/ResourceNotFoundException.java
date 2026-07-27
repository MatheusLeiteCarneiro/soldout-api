package com.mlcdev.soldout.shared.exceptions;

import lombok.Getter;

import java.util.UUID;

@Getter
public abstract class ResourceNotFoundException extends RuntimeException {

    private final UUID resourceId;

    protected ResourceNotFoundException(String message, UUID resourceId) {
        super(message);
        this.resourceId = resourceId;
    }
}
