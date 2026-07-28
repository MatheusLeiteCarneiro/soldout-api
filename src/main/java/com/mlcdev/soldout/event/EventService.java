package com.mlcdev.soldout.event;

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

}
