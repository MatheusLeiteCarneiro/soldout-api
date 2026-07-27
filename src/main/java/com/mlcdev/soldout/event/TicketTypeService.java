package com.mlcdev.soldout.event;

import com.mlcdev.soldout.event.exceptions.InvalidEventException;
import com.mlcdev.soldout.event.exceptions.TicketTypeNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class TicketTypeService {

    private final TicketTypeRepository ticketTypeRepository;

    @Transactional
    public void reserve(UUID ticketTypeId, int quantity){
        TicketType ticketType = ticketTypeRepository.findByIdWithEventForUpdate(ticketTypeId)
                .orElseThrow(() -> new TicketTypeNotFoundException(ticketTypeId));

        Event event = ticketType.getEvent();
        if(!event.isPublished()){
            throw new InvalidEventException("cannot reserve tickets for events with status: " + event.getStatus() + " reservations only for published events");
        }

        ticketType.reserve(quantity);
    }
}
