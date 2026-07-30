package com.mlcdev.soldout.event;

import com.mlcdev.soldout.event.dto.TicketTypeDTO;
import com.mlcdev.soldout.event.exception.InvalidEventException;
import com.mlcdev.soldout.event.exception.TicketTypeNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class TicketTypeService {

    private final TicketTypeRepository ticketTypeRepository;
    private final TicketTypeMapper ticketTypeMapper;

    @Transactional(readOnly = true)
    public List<TicketTypeDTO> findAllFromEvent(UUID eventId){
       List<TicketType> ticketTypes = ticketTypeRepository.findAllByEventId(eventId);
        return ticketTypes.stream().map(ticketTypeMapper::ticketTypeToTicketTypeDTO).toList();
    }

    @Transactional(readOnly = true)
    public TicketTypeDTO findById(UUID ticketTypeId) {
        TicketType ticketType = ticketTypeRepository.findById(ticketTypeId).orElseThrow(() -> new TicketTypeNotFoundException(ticketTypeId));
        return ticketTypeMapper.ticketTypeToTicketTypeDTO(ticketType);
    }

    @Transactional
    public void reserve(UUID ticketTypeId, int quantity){
        TicketType ticketType = ticketTypeRepository.findByIdWithEventForUpdate(ticketTypeId)
                .orElseThrow(() -> new TicketTypeNotFoundException(ticketTypeId));

        Event event = ticketType.getEvent();
        if(!event.isPublished()){
            throw new InvalidEventException("tickets can only be reserved for published events, current status is: " + event.getStatus());
        }

        ticketType.reserve(quantity);
    }


}
