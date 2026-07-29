package com.mlcdev.soldout.event;

import com.mlcdev.soldout.event.dtos.EventDetailDTO;
import com.mlcdev.soldout.event.dtos.EventSummaryDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = TicketTypeMapper.class)
public interface EventMapper {

    EventSummaryDTO eventToEventSummaryDTO(Event event);

    EventDetailDTO eventToEventDetailDTO(Event event);
}
