package com.mlcdev.soldout.event;

import com.mlcdev.soldout.event.dto.ReserveTicketTypeDTO;
import com.mlcdev.soldout.event.dto.TicketTypeDTO;
import com.mlcdev.soldout.event.dto.TicketTypeInsertDTO;
import com.mlcdev.soldout.event.dto.UpdateTicketTypeDTO;
import com.mlcdev.soldout.event.exception.EventNotFoundException;
import com.mlcdev.soldout.event.exception.InvalidEventException;
import com.mlcdev.soldout.event.exception.TicketTypeNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class TicketTypeService {

    private final TicketTypeRepository ticketTypeRepository;
    private final EventRepository eventRepository;
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
    public TicketTypeDTO insert(UUID eventId, TicketTypeInsertDTO insertDTO){
        Event event = eventRepository.findByIdWithTicketTypes(eventId).orElseThrow(() -> new EventNotFoundException(eventId));
        TicketType ticketType = event.addTicketType(insertDTO.name(), insertDTO.description(), insertDTO.price(), insertDTO.totalQuantity());
        eventRepository.save(event);
        return ticketTypeMapper.ticketTypeToTicketTypeDTO(ticketType);
    }

    @Transactional
    public TicketTypeDTO reserve(UUID ticketTypeId, ReserveTicketTypeDTO reserveDTO){
        TicketType ticketType = ticketTypeRepository.findByIdWithEventForUpdate(ticketTypeId)
                .orElseThrow(() -> new TicketTypeNotFoundException(ticketTypeId));

        Event event = ticketType.getEvent();
        if(!event.isPublished()){
            throw new InvalidEventException("tickets can only be reserved for published events, current status is: " + event.getStatus());
        }
        if(!event.getEndsAt().isAfter(Instant.now())){
            throw new InvalidEventException("tickets cannot be reserved for an event that has already ended");
        }

        ticketType.reserve(reserveDTO.quantity());
        return ticketTypeMapper.ticketTypeToTicketTypeDTO(ticketTypeRepository.save(ticketType));
    }

    @Transactional
    public TicketTypeDTO update(UUID ticketTypeId, UpdateTicketTypeDTO dto) {
        TicketType ticketType = ticketTypeRepository.findByIdWithEvent(ticketTypeId)
                .orElseThrow(() -> new TicketTypeNotFoundException(ticketTypeId));

        ticketType.getEvent().ensureModifiable();

        String newName = (dto.name() == null) ? ticketType.getName() : dto.name();
        String newDescription = (dto.description() == null) ? ticketType.getDescription() : dto.description();
        BigDecimal newPrice = (dto.price() == null) ? ticketType.getPrice() : dto.price();
        ticketType.updateDetails(newName, newDescription, newPrice);

        return ticketTypeMapper.ticketTypeToTicketTypeDTO(ticketType);
    }
}
