package com.mlcdev.soldout.event;

import com.mlcdev.soldout.event.dto.EventDetailDTO;
import com.mlcdev.soldout.event.dto.EventInsertDTO;
import com.mlcdev.soldout.event.dto.EventSummaryDTO;
import org.mapstruct.Mapper;


@Mapper(componentModel = "spring", uses = TicketTypeMapper.class)
public interface EventMapper {

    EventSummaryDTO eventToEventSummaryDTO(Event event);

    EventDetailDTO eventToEventDetailDTO(Event event);

    Event eventInsertDTOtoEvent(EventInsertDTO eventInsertDTO);

}
