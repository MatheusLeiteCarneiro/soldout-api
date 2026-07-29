package com.mlcdev.soldout.event.exception;

import com.mlcdev.soldout.shared.exception.ResourceNotFoundException;

import java.util.UUID;

public class TicketTypeNotFoundException extends ResourceNotFoundException {
    public TicketTypeNotFoundException(UUID ticketTypeId)
    {
        super("ticket type with id: " + ticketTypeId + " not found", ticketTypeId);
    }
}
