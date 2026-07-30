package com.mlcdev.soldout.event;

import com.mlcdev.soldout.event.dto.EventDetailDTO;
import com.mlcdev.soldout.event.dto.EventInsertDTO;
import com.mlcdev.soldout.event.dto.EventSummaryDTO;
import com.mlcdev.soldout.event.dto.EventUpdateDTO;
import com.mlcdev.soldout.event.exception.EventNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;


@RequiredArgsConstructor
@Service
public class EventService {

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;

    @Transactional(readOnly = true)
    public Page<EventSummaryDTO> findAllAvailable(Pageable pageable){
        return eventRepository.findAllAvailable(pageable).map(eventMapper::eventToEventSummaryDTO);
    }

    @Transactional(readOnly = true)
    public EventDetailDTO findById(UUID eventId){
        Event event = eventRepository.findByIdWithTicketTypes(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));
        return eventMapper.eventToEventDetailDTO(event);
    }

    @Transactional
    public EventDetailDTO insert(EventInsertDTO insertDTO){
        Event event = eventMapper.eventInsertDTOtoEvent(insertDTO);
        Event saved = eventRepository.save(event);
        return eventMapper.eventToEventDetailDTO(saved);
    }


    @Transactional
    public EventDetailDTO publish(UUID eventId){
        Event event = eventRepository
                .findByIdWithTicketTypes(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));
        event.publish();
        return eventMapper.eventToEventDetailDTO(event);
    }

    @Transactional
    public EventDetailDTO cancel(UUID eventId) {
        Event event = eventRepository
                .findByIdWithTicketTypes(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));
        event.cancel();
        return eventMapper.eventToEventDetailDTO(event);
    }

    @Transactional
    public EventDetailDTO restore(UUID eventId) {
        Event event = eventRepository
                .findByIdWithTicketTypes(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));
        event.restore();
        return eventMapper.eventToEventDetailDTO(event);
    }

    @Transactional
    public EventDetailDTO update(UUID eventId, EventUpdateDTO updateDTO){
        Event event = eventRepository.findByIdWithTicketTypes(eventId).orElseThrow(() -> new EventNotFoundException(eventId));
        applyDTOPatchToEntity(event, updateDTO);
        return eventMapper.eventToEventDetailDTO(event);
    }

    private void applyDTOPatchToEntity(Event event, EventUpdateDTO dto){
        String newName= (dto.name() == null) ? event.getName() : dto.name();
        String newDescription= (dto.description() == null) ? event.getDescription() : dto.description();
        Instant newStart = (dto.startsAt() == null) ? event.getStartsAt() : dto.startsAt();
        Instant newEnd = (dto.endsAt() == null) ? event.getEndsAt() : dto.endsAt();
        event.updateDetails(newName, newDescription);
        event.reschedule(newStart, newEnd);
    }

}
