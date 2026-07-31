package com.mlcdev.soldout.event.exception;

import com.mlcdev.soldout.shared.exception.ResourceNotFoundException;

import java.util.UUID;

public class EventNotFoundException extends ResourceNotFoundException {

    public EventNotFoundException(UUID eventId) {
        super("event with id: " + eventId + " not found", eventId);
    }
}
