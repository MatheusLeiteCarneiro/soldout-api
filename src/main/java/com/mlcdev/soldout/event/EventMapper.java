package com.mlcdev.soldout.event;

import com.mlcdev.soldout.event.dtos.EventSummaryDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EventMapper {

    EventSummaryDTO eventToEventSummaryDTO(Event event);


}
