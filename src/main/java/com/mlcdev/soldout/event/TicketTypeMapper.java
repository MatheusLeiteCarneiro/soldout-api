package com.mlcdev.soldout.event;

import com.mlcdev.soldout.event.dto.TicketTypeDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TicketTypeMapper {


    TicketTypeDTO ticketTypeToTicketTypeDTO(TicketType ticketType);

}
