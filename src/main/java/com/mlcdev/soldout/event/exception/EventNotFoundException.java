package com.mlcdev.soldout.event.exception;

import com.mlcdev.soldout.shared.exceptions.ResourceNotFoundException;

import java.util.UUID;

public class EventNotFoundException extends ResourceNotFoundException {

    public EventNotFoundException(UUID eventId) {
        super("event type with id: " + eventId + " not found", eventId);
    }
}
