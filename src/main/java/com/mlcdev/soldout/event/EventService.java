package com.mlcdev.soldout.event;

import com.mlcdev.soldout.event.dtos.EventDetailDTO;
import com.mlcdev.soldout.event.dtos.EventInsertDTO;
import com.mlcdev.soldout.event.dtos.EventSummaryDTO;
import com.mlcdev.soldout.event.exceptions.EventNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}
