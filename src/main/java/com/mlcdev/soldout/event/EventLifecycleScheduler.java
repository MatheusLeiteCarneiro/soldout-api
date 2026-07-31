package com.mlcdev.soldout.event;

import com.mlcdev.soldout.event.exception.EventNotFoundException;
import com.mlcdev.soldout.event.exception.InvalidEventException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Slf4j
@Component
class EventLifecycleScheduler {

    private final EventRepository eventRepository;
    private final EventService eventService;

    @Scheduled(fixedDelay = 5, timeUnit = TimeUnit.MINUTES)
    public void finishEndedEvents(){
        eventRepository.findAllIdsPendingFinish().forEach(eventId -> {
            try {
                eventService.finish(eventId);
            }catch (OptimisticLockingFailureException | InvalidEventException | EventNotFoundException e){
                log.debug("skipping event {}: {}",eventId, e.getMessage());
            }
        });
    }


}
