package com.mlcdev.soldout.event;

import com.mlcdev.soldout.event.dto.EventDetailDTO;
import com.mlcdev.soldout.event.dto.EventInsertDTO;
import com.mlcdev.soldout.event.dto.EventSummaryDTO;
import com.mlcdev.soldout.event.dto.EventUpdateDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
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
    public ResponseEntity<EventDetailDTO> findById(@PathVariable("eventId") UUID eventId){
        return ResponseEntity.ok(service.findById(eventId));
    }

    @PostMapping
    public ResponseEntity<EventDetailDTO> insert(@RequestBody @Valid EventInsertDTO insertDTO){
        EventDetailDTO saved = service.insert(insertDTO);
        URI uri = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(saved.id())
                .toUri();

        return ResponseEntity.created(uri).body(saved);
    }

    @PostMapping("/{eventId}/publish")
    public ResponseEntity<EventDetailDTO> publish(@PathVariable("eventId") UUID eventId){
        return ResponseEntity.ok(service.publish(eventId));
    }

    @PostMapping("/{eventId}/cancel")
    public ResponseEntity<EventDetailDTO> cancel(@PathVariable("eventId") UUID eventId){
        return ResponseEntity.ok(service.cancel(eventId));
    }

    @PostMapping("/{eventId}/restore")
    public ResponseEntity<EventDetailDTO> restore(@PathVariable("eventId") UUID eventId){
        return ResponseEntity.ok(service.restore(eventId));
    }

    @PatchMapping("/{eventId}")
    public ResponseEntity<EventDetailDTO> update(@PathVariable("eventId") UUID eventId, @RequestBody @Valid EventUpdateDTO dto){
        return ResponseEntity.ok(service.update(eventId, dto));
    }
}
