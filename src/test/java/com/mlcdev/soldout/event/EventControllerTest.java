package com.mlcdev.soldout.event;

import com.mlcdev.soldout.event.dto.EventDetailDTO;
import com.mlcdev.soldout.event.dto.EventInsertDTO;
import com.mlcdev.soldout.event.dto.EventSummaryDTO;
import com.mlcdev.soldout.event.dto.EventUpdateDTO;
import com.mlcdev.soldout.event.dto.TicketTypeDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(EventController.class)
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EventService eventService;

    private UUID eventId;
    private String eventName, eventDescription;
    private Instant eventStart, eventEnd;
    private EventStatus eventStatus;
    private EventDetailDTO eventDetailDTO;
    private Page<EventSummaryDTO> eventSummaryDTOPage;
    private Set<TicketTypeDTO> eventTicketTypes;

    @BeforeEach
    void setUp() {
        eventId = UUID.randomUUID();
        eventName = "New event";
        eventDescription = "New event description";
        eventStart = Instant.now().plus(Duration.ofDays(1));
        eventEnd = eventStart.plus(Duration.ofDays(1));
        eventStatus = EventStatus.PUBLISHED;
        eventTicketTypes = Set.of(new TicketTypeDTO(UUID.randomUUID(), "Regular ticket", "Description of ticket", new BigDecimal("100.00"), 10));
        eventDetailDTO = new EventDetailDTO(eventId, eventName, eventDescription, eventStart,eventEnd, eventStatus, eventTicketTypes);
        eventSummaryDTOPage = new PageImpl<>(List.of(new EventSummaryDTO(eventId, eventName, eventDescription, eventStart)), PageRequest.of(0,1),1);
    }

    @Test
    void getByIdEndpointShouldReturnResponseWithEventDetailDTOOnBody() throws Exception{
        when(eventService.findById(eventId)).thenReturn(eventDetailDTO);

        ResultActions resultActions =mockMvc.perform(get("/v1/events/{eventId}", eventId))
                .andExpect(status().isOk()).andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
        assertJsonEventDetailDtoDataIsCorrect(resultActions);
    }

    @Test
    void getAllEndpointShouldReturnResponseWithPageContainingEventsSummary() throws Exception{
        when(eventService.findAllAvailable(any(Pageable.class))).thenReturn(eventSummaryDTOPage);

        ResultActions resultActions = mockMvc.perform(get("/v1/events"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
        assertJsonEventSummaryDtoPageDataIsCorrect(resultActions);
    }

    @Test
    void insertEndpointShouldReturnTheResponseContainingInBodyTheCreatedEventDetailDTO() throws Exception{
        EventInsertDTO eventInsertDTO = new EventInsertDTO(eventName, eventDescription, eventStart, eventEnd);
        when(eventService.insert(eventInsertDTO)).thenReturn(eventDetailDTO);

        ResultActions resultActions = mockMvc.perform(
                post("/v1/events").
                        contentType(MediaType.APPLICATION_JSON).
                        content(objectMapper.writeValueAsString(eventInsertDTO)))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, endsWith("/v1/events/" + eventId.toString())));

        assertJsonEventDetailDtoDataIsCorrect(resultActions);
    }

    @Nested
    class InsertValidation {

        @Test
        void insertShouldReturnBadRequestWithNullName() throws Exception {
            EventInsertDTO eventInsertDTO = new EventInsertDTO(null, eventDescription, eventStart, eventEnd);

            mockMvc.perform(post("/v1/events")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(eventInsertDTO)))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(eventService);
        }

        @Test
        void insertShouldReturnBadRequestWithBlankName() throws Exception {
            EventInsertDTO eventInsertDTO = new EventInsertDTO("          ", eventDescription, eventStart, eventEnd);

            mockMvc.perform(post("/v1/events")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(eventInsertDTO)))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(eventService);
        }

        @Test
        void insertShouldReturnBadRequestWithNameLongerThanMaximumLength() throws Exception {
            EventInsertDTO eventInsertDTO = new EventInsertDTO("a".repeat(151), eventDescription, eventStart, eventEnd);

            mockMvc.perform(post("/v1/events")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(eventInsertDTO)))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(eventService);
        }

        @Test
        void insertShouldReturnBadRequestWithNullDescription() throws Exception {
            EventInsertDTO eventInsertDTO = new EventInsertDTO(eventName, null, eventStart, eventEnd);

            mockMvc.perform(post("/v1/events")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(eventInsertDTO)))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(eventService);
        }

        @Test
        void insertShouldReturnBadRequestWithBlankDescription() throws Exception {
            EventInsertDTO eventInsertDTO = new EventInsertDTO(eventName, "          ", eventStart, eventEnd);

            mockMvc.perform(post("/v1/events")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(eventInsertDTO)))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(eventService);
        }

        @Test
        void insertShouldReturnBadRequestWithDescriptionLongerThanMaximumLength() throws Exception {
            EventInsertDTO eventInsertDTO = new EventInsertDTO(eventName, "a".repeat(2501), eventStart, eventEnd);

            mockMvc.perform(post("/v1/events")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(eventInsertDTO)))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(eventService);
        }

        @Test
        void insertShouldReturnBadRequestWithNullStartDate() throws Exception {
            EventInsertDTO eventInsertDTO = new EventInsertDTO(eventName, eventDescription, null, eventEnd);

            mockMvc.perform(post("/v1/events")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(eventInsertDTO)))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(eventService);
        }

        @Test
        void insertShouldReturnBadRequestWithPastStartDate() throws Exception {
            EventInsertDTO eventInsertDTO = new EventInsertDTO(eventName, eventDescription, Instant.now().minus(Duration.ofDays(1)), eventEnd);

            mockMvc.perform(post("/v1/events")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(eventInsertDTO)))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(eventService);
        }

        @Test
        void insertShouldReturnBadRequestWithNullEndDate() throws Exception {
            EventInsertDTO eventInsertDTO = new EventInsertDTO(eventName, eventDescription, eventStart, null);

            mockMvc.perform(post("/v1/events")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(eventInsertDTO)))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(eventService);
        }

        @Test
        void insertShouldReturnBadRequestWithPastEndDate() throws Exception {
            EventInsertDTO eventInsertDTO = new EventInsertDTO(eventName, eventDescription, eventStart, Instant.now().minus(Duration.ofDays(1)));

            mockMvc.perform(post("/v1/events")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(eventInsertDTO)))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(eventService);
        }

        @Test
        void insertShouldReturnBadRequestWhenEndDateIsNotAfterStartDate() throws Exception {
            EventInsertDTO eventInsertDTO = new EventInsertDTO(eventName, eventDescription, eventStart, eventStart.minus(Duration.ofHours(1)));

            mockMvc.perform(post("/v1/events")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(eventInsertDTO)))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(eventService);
        }
    }

    @Test
    void publishEndpointShouldReturnTheResponseContainingInTheBodyThePublishedEventDetailDTO() throws Exception {
        when(eventService.publish(eventId)).thenReturn(eventDetailDTO);

        ResultActions resultActions = mockMvc.perform(
                        post("/v1/events/{eventId}/publish", eventId))
                .andExpect(status().isOk());

        assertJsonEventDetailDtoDataIsCorrect(resultActions);
    }

    @Test
    void cancelEndpointShouldReturnTheResponseContainingInTheBodyTheCancelledEventDetailDTO() throws Exception {
        eventStatus = EventStatus.CANCELLED;
        EventDetailDTO cancelledEventDetail = new EventDetailDTO(eventId, eventName, eventDescription, eventStart,eventEnd, eventStatus, eventTicketTypes);

        when(eventService.cancel(eventId)).thenReturn(cancelledEventDetail);

        ResultActions resultActions = mockMvc.perform(
                        post("/v1/events/{eventId}/cancel", eventId))
                .andExpect(status().isOk());

        assertJsonEventDetailDtoDataIsCorrect(resultActions);
    }

    @Test
    void restoreEndpointShouldReturnTheResponseContainingInTheBodyTheRestoredEventDetailDTO() throws Exception {
        eventStatus = EventStatus.DRAFT;
        EventDetailDTO restoredEventDetailDTO = new EventDetailDTO(eventId, eventName, eventDescription, eventStart,eventEnd, eventStatus, eventTicketTypes);

        when(eventService.restore(eventId)).thenReturn(restoredEventDetailDTO);

        ResultActions resultActions = mockMvc.perform(
                        post("/v1/events/{eventId}/restore", eventId))
                .andExpect(status().isOk());

        assertJsonEventDetailDtoDataIsCorrect(resultActions);
    }

    @Test
    void updateEndpointShouldReturnTheResponseContainingInBodyTheUpdatedEventDetailDTO() throws Exception{
        EventUpdateDTO eventUpdateDTO = new EventUpdateDTO(eventName, eventDescription, eventStart, eventEnd);
        when(eventService.update(eventId, eventUpdateDTO)).thenReturn(eventDetailDTO);

        ResultActions resultActions = mockMvc.perform(patch("/v1/events/{eventId}", eventId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(eventUpdateDTO)))
                        .andExpect(status().isOk());
        assertJsonEventDetailDtoDataIsCorrect(resultActions);
    }

    @Nested
    class UpdateValidation {

        @Test
        void updateShouldReturnBadRequestWithBlankName() throws Exception{
            EventUpdateDTO eventUpdateDTO = new EventUpdateDTO("           ", eventDescription, eventStart, eventEnd);

            mockMvc.perform(patch("/v1/events/{eventId}", eventId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(eventUpdateDTO)))
                    .andExpect(status().isBadRequest());
            verifyNoInteractions(eventService);

        }

        @Test
        void updateShouldReturnBadRequestWithBlankDescription() throws Exception{
            EventUpdateDTO eventUpdateDTO = new EventUpdateDTO(eventName, "          ", eventStart, eventEnd);

            mockMvc.perform(patch("/v1/events/{eventId}", eventId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(eventUpdateDTO)))
                    .andExpect(status().isBadRequest());
            verifyNoInteractions(eventService);

        }

        @Test
        void updateShouldReturnBadRequestWithPastStartDate() throws Exception{
            EventUpdateDTO eventUpdateDTO = new EventUpdateDTO(eventName, eventDescription, eventStart.minus(Duration.ofDays(10)), eventEnd);

            mockMvc.perform(patch("/v1/events/{eventId}", eventId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(eventUpdateDTO)))
                    .andExpect(status().isBadRequest());
            verifyNoInteractions(eventService);

        }

        @Test
        void updateShouldReturnBadRequestWithPastEndDate() throws Exception{
            EventUpdateDTO eventUpdateDTO = new EventUpdateDTO(eventName, eventDescription, eventStart, eventEnd.minus(Duration.ofDays(10)));

            mockMvc.perform(patch("/v1/events/{eventId}", eventId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(eventUpdateDTO)))
                    .andExpect(status().isBadRequest());
            verifyNoInteractions(eventService);

        }
    }


    private void assertJsonEventDetailDtoDataIsCorrect(ResultActions resultActions) throws Exception {
        resultActions.andExpect(jsonPath("$.id").value(eventId.toString()))
                .andExpect(jsonPath("$.name").value(eventName))
                .andExpect(jsonPath("$.description").value(eventDescription))
                .andExpect(jsonPath("$.startsAt").value(eventStart.toString()))
                .andExpect(jsonPath("$.endsAt").value(eventEnd.toString()))
                .andExpect(jsonPath("$.status").value(eventStatus.toString()))
                .andExpect(jsonPath("$.ticketTypes").isArray())
                .andExpect(jsonPath("$.ticketTypes.length()").value(1))
                .andExpect(jsonPath("$.ticketTypes[0].name").value("Regular ticket"))
                .andExpect(jsonPath("$.ticketTypes[0].availableQuantity").value(10));
    }

    private void assertJsonEventSummaryDtoPageDataIsCorrect(ResultActions resultActions) throws Exception {
        resultActions.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(eventId.toString()))
                .andExpect(jsonPath("$.content[0].name").value(eventName))
                .andExpect(jsonPath("$.content[0].description").value(eventDescription))
                .andExpect(jsonPath("$.content[0].startsAt").value(eventStart.toString()))
        ;
    }
}
