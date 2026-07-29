package com.mlcdev.soldout.event;

import com.mlcdev.soldout.event.dtos.EventDetailDTO;
import com.mlcdev.soldout.event.dtos.EventSummaryDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/events")
public class EventController {

    private final EventService service;

    @GetMapping
    public ResponseEntity<Page<EventSummaryDTO>> findAllAvailable(Pageable pageable){
        return ResponseEntity.ok(service.findAllAvailable(pageable));
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<EventDetailDTO> findById(@PathVariable("eventId") UUID id){
        return ResponseEntity.ok(service.findById(id));
    }

}
