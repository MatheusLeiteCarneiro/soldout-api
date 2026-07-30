package com.mlcdev.soldout.event;

import com.mlcdev.soldout.event.dto.TicketTypeDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
public class TicketTypeController {

    private final TicketTypeService ticketTypeService;

    @GetMapping("/events/{eventId}/ticket-types")
    public ResponseEntity<List<TicketTypeDTO>> findAllFromEvent(@PathVariable("eventId") UUID eventId){
        return ResponseEntity.ok(ticketTypeService.findAllFromEvent(eventId));
    }
}
