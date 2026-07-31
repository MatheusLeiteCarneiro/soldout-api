package com.mlcdev.soldout.event;

import com.mlcdev.soldout.event.dto.ReserveTicketTypeDTO;
import com.mlcdev.soldout.event.dto.TicketTypeDTO;
import com.mlcdev.soldout.event.dto.TicketTypeInsertDTO;
import com.mlcdev.soldout.event.dto.UpdateTicketTypeDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

import java.net.URI;
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

    @GetMapping("/ticket-types/{ticketTypeId}")
    public ResponseEntity<TicketTypeDTO> findById(@PathVariable("ticketTypeId") UUID ticketTypeId){
        return ResponseEntity.ok(ticketTypeService.findById(ticketTypeId));
    }

    @PostMapping("/events/{eventId}/ticket-types")
    public ResponseEntity<TicketTypeDTO> insert(@PathVariable("eventId") UUID eventId, @RequestBody @Valid TicketTypeInsertDTO insertDTO){
        TicketTypeDTO created = ticketTypeService.insert(eventId, insertDTO);
        URI uri = MvcUriComponentsBuilder.fromMethodCall(MvcUriComponentsBuilder.on(TicketTypeController.class).findById(created.id())).build().toUri();
        return ResponseEntity.created(uri).body(created);
    }

    @PostMapping("/ticket-types/{ticketTypeId}/reserve")
    public ResponseEntity<TicketTypeDTO> reserve(@PathVariable("ticketTypeId")UUID ticketTypeId,@RequestBody @Valid ReserveTicketTypeDTO reserveDTO){
        return ResponseEntity.ok(ticketTypeService.reserve(ticketTypeId, reserveDTO));
    }

    @PatchMapping("/ticket-types/{ticketTypeId}")
    public ResponseEntity<TicketTypeDTO> update(@PathVariable("ticketTypeId")UUID ticketTypeId,
                                                @RequestBody @Valid UpdateTicketTypeDTO updateDTO){
        return ResponseEntity.ok(ticketTypeService.update(ticketTypeId, updateDTO));
    }

}
