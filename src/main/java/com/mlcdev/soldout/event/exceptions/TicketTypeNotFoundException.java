package com.mlcdev.soldout.event.exceptions;

import com.mlcdev.soldout.shared.exceptions.ResourceNotFoundException;

import java.util.UUID;

public class TicketTypeNotFoundException extends ResourceNotFoundException {
    public TicketTypeNotFoundException(UUID ticketTypeId)
    {
        super("ticket type with id: " + ticketTypeId + " not found", ticketTypeId);
    }
}
