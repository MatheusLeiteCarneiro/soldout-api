package com.mlcdev.soldout.event;

import com.mlcdev.soldout.event.dto.ReserveTicketTypeDTO;
import com.mlcdev.soldout.event.dto.TicketTypeDTO;
import com.mlcdev.soldout.event.dto.TicketTypeInsertDTO;
import com.mlcdev.soldout.event.dto.UpdateTicketTypeDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import tools.jackson.databind.ObjectMapper;


import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.endsWith;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(TicketTypeController.class)
class TicketTypeControllerTest {

    @MockitoBean
    private TicketTypeService ticketTypeService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    UUID eventId, ticketTypeId;
    String ticketName, ticketDescription;
    BigDecimal ticketPrice;
    int ticketInitialQuantity;
    TicketTypeDTO ticketTypeDTO;


    @BeforeEach
    void setUp() {
        eventId = UUID.randomUUID();
        ticketTypeId = UUID.randomUUID();
        ticketName = "Ticket name";
        ticketDescription = "Ticket description";
        ticketPrice = new BigDecimal("100.00");
        ticketInitialQuantity = 10;
        ticketTypeDTO = new TicketTypeDTO(ticketTypeId, ticketName, ticketDescription, ticketPrice, ticketInitialQuantity);
    }

    @Test
    void getAllByEventShouldReturnOnResponseAllTicketTypesForEvent() throws Exception {
        when(ticketTypeService.findAllFromEvent(eventId)).thenReturn(List.of(ticketTypeDTO));

        ResultActions resultActions = mockMvc
                .perform(get("/v1/events/{eventId}/ticket-types", eventId))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
        assertJsonTicketTypeDtoDataIsCorrect(resultActions, "$.[0]");
    }

    @Test
    void getByIdShouldReturnOnResponseTicketTypeDTO() throws Exception {
        when(ticketTypeService.findById(ticketTypeId)).thenReturn(ticketTypeDTO);

        ResultActions resultActions = mockMvc
                .perform(get("/v1/ticket-types/{ticketTypeId}", ticketTypeId))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
        assertJsonTicketTypeDtoDataIsCorrect(resultActions);
    }

    @Test
    void insertShouldReturnOnResponseCreatedTicketTypeDTO() throws Exception {
        TicketTypeInsertDTO insertDTO = new TicketTypeInsertDTO(ticketName, ticketDescription,ticketPrice, ticketInitialQuantity);
        when(ticketTypeService.insert(eventId, insertDTO)).thenReturn(ticketTypeDTO);

        ResultActions resultActions = mockMvc.perform(post("/v1/events/{eventId}/ticket-types", eventId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(insertDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(header().string(HttpHeaders.LOCATION, endsWith("/v1/ticket-types/" + ticketTypeId.toString())));

        assertJsonTicketTypeDtoDataIsCorrect(resultActions);
    }

    @Nested
    class InsertValidation {

        @Test
        void insertShouldReturnBadRequestWithNullName() throws Exception {
            TicketTypeInsertDTO insertDTO = new TicketTypeInsertDTO(null, ticketDescription, ticketPrice, ticketInitialQuantity);

            mockMvc.perform(post("/v1/events/{eventId}/ticket-types", eventId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(insertDTO)))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(ticketTypeService);
        }

        @Test
        void insertShouldReturnBadRequestWithBlankName() throws Exception {
            TicketTypeInsertDTO insertDTO = new TicketTypeInsertDTO("          ", ticketDescription, ticketPrice, ticketInitialQuantity);

            mockMvc.perform(post("/v1/events/{eventId}/ticket-types", eventId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(insertDTO)))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(ticketTypeService);
        }

        @Test
        void insertShouldReturnBadRequestWithNameLongerThanMaximumLength() throws Exception {
            TicketTypeInsertDTO insertDTO = new TicketTypeInsertDTO("a".repeat(101), ticketDescription, ticketPrice, ticketInitialQuantity);

            mockMvc.perform(post("/v1/events/{eventId}/ticket-types", eventId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(insertDTO)))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(ticketTypeService);
        }

        @Test
        void insertShouldReturnBadRequestWithNullDescription() throws Exception {
            TicketTypeInsertDTO insertDTO = new TicketTypeInsertDTO(ticketName, null, ticketPrice, ticketInitialQuantity);

            mockMvc.perform(post("/v1/events/{eventId}/ticket-types", eventId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(insertDTO)))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(ticketTypeService);
        }

        @Test
        void insertShouldReturnBadRequestWithBlankDescription() throws Exception {
            TicketTypeInsertDTO insertDTO = new TicketTypeInsertDTO(ticketName, "          ", ticketPrice, ticketInitialQuantity);

            mockMvc.perform(post("/v1/events/{eventId}/ticket-types", eventId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(insertDTO)))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(ticketTypeService);
        }

        @Test
        void insertShouldReturnBadRequestWithDescriptionLongerThanMaximumLength() throws Exception {
            TicketTypeInsertDTO insertDTO = new TicketTypeInsertDTO(ticketName, "a".repeat(501), ticketPrice, ticketInitialQuantity);

            mockMvc.perform(post("/v1/events/{eventId}/ticket-types", eventId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(insertDTO)))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(ticketTypeService);
        }

        @Test
        void insertShouldReturnBadRequestWithNullPrice() throws Exception {
            TicketTypeInsertDTO insertDTO = new TicketTypeInsertDTO(ticketName, ticketDescription, null, ticketInitialQuantity);

            mockMvc.perform(post("/v1/events/{eventId}/ticket-types", eventId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(insertDTO)))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(ticketTypeService);
        }

        @Test
        void insertShouldReturnBadRequestWithNegativePrice() throws Exception {
            TicketTypeInsertDTO insertDTO = new TicketTypeInsertDTO(ticketName, ticketDescription, new BigDecimal("-0.01"), ticketInitialQuantity);

            mockMvc.perform(post("/v1/events/{eventId}/ticket-types", eventId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(insertDTO)))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(ticketTypeService);
        }

        @Test
        void insertShouldReturnBadRequestWithNullTotalQuantity() throws Exception {
            TicketTypeInsertDTO insertDTO = new TicketTypeInsertDTO(ticketName, ticketDescription, ticketPrice, null);

            mockMvc.perform(post("/v1/events/{eventId}/ticket-types", eventId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(insertDTO)))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(ticketTypeService);
        }

        @SuppressWarnings("DataFlowIssue")
        @Test
        void insertShouldReturnBadRequestWithZeroTotalQuantity() throws Exception {
            TicketTypeInsertDTO insertDTO = new TicketTypeInsertDTO(ticketName, ticketDescription, ticketPrice, 0);

            mockMvc.perform(post("/v1/events/{eventId}/ticket-types", eventId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(insertDTO)))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(ticketTypeService);
        }

        @SuppressWarnings("DataFlowIssue")
        @Test
        void insertShouldReturnBadRequestWithNegativeTotalQuantity() throws Exception {
            TicketTypeInsertDTO insertDTO = new TicketTypeInsertDTO(ticketName, ticketDescription, ticketPrice, -1);

            mockMvc.perform(post("/v1/events/{eventId}/ticket-types", eventId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(insertDTO)))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(ticketTypeService);
        }
    }

    @Test
    void reserveShouldReturnOnResponseTheReservedTicketTypeDTO() throws Exception {
        int reservedQuantity = 2;
        ReserveTicketTypeDTO reserveDTO = new ReserveTicketTypeDTO(reservedQuantity);
        ticketInitialQuantity -= reservedQuantity;
        TicketTypeDTO reservedTicketTypeDTO = new TicketTypeDTO(ticketTypeId, ticketName, ticketDescription, ticketPrice, ticketInitialQuantity);
        when(ticketTypeService.reserve(ticketTypeId, reserveDTO)).thenReturn(reservedTicketTypeDTO);

        ResultActions resultActions = mockMvc.perform(post("/v1/ticket-types/{ticketTypeId}/reserve", ticketTypeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reserveDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        assertJsonTicketTypeDtoDataIsCorrect(resultActions);
    }

    @Nested
    class ReserveValidation {

        @Test
        void reserveShouldReturnBadRequestWithNullQuantity() throws Exception {
            ReserveTicketTypeDTO reserveDTO = new ReserveTicketTypeDTO(null);

            mockMvc.perform(post("/v1/ticket-types/{ticketTypeId}/reserve", ticketTypeId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(reserveDTO)))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(ticketTypeService);
        }

        @SuppressWarnings("DataFlowIssue")
        @Test
        void reserveShouldReturnBadRequestWithZeroQuantity() throws Exception {
            ReserveTicketTypeDTO reserveDTO = new ReserveTicketTypeDTO(0);

            mockMvc.perform(post("/v1/ticket-types/{ticketTypeId}/reserve", ticketTypeId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(reserveDTO)))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(ticketTypeService);
        }

        @SuppressWarnings("DataFlowIssue")
        @Test
        void reserveShouldReturnBadRequestWithNegativeQuantity() throws Exception {
            ReserveTicketTypeDTO reserveDTO = new ReserveTicketTypeDTO(-1);

            mockMvc.perform(post("/v1/ticket-types/{ticketTypeId}/reserve", ticketTypeId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(reserveDTO)))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(ticketTypeService);
        }
    }

    @Test
    void updateShouldReturnOnResponseTheUpdatedTicketTypeDTO() throws Exception {
        ticketName = "Updated ticket name";
        ticketDescription = "Updated ticket description";
        ticketPrice = new BigDecimal("150.00");
        UpdateTicketTypeDTO updateDTO = new UpdateTicketTypeDTO(ticketName, ticketDescription, ticketPrice);
        TicketTypeDTO updatedTicketTypeDTO = new TicketTypeDTO(ticketTypeId, ticketName, ticketDescription, ticketPrice, ticketInitialQuantity);
        when(ticketTypeService.update(ticketTypeId, updateDTO)).thenReturn(updatedTicketTypeDTO);

        ResultActions resultActions = mockMvc.perform(patch("/v1/ticket-types/{ticketTypeId}", ticketTypeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        assertJsonTicketTypeDtoDataIsCorrect(resultActions);
    }

    @Nested
    class UpdateValidation {

        @Test
        void updateShouldReturnBadRequestWithBlankName() throws Exception {
            UpdateTicketTypeDTO updateDTO = new UpdateTicketTypeDTO("          ", ticketDescription, ticketPrice);

            mockMvc.perform(patch("/v1/ticket-types/{ticketTypeId}", ticketTypeId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDTO)))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(ticketTypeService);
        }

        @Test
        void updateShouldReturnBadRequestWithNameLongerThanMaximumLength() throws Exception {
            UpdateTicketTypeDTO updateDTO = new UpdateTicketTypeDTO("a".repeat(101), ticketDescription, ticketPrice);

            mockMvc.perform(patch("/v1/ticket-types/{ticketTypeId}", ticketTypeId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDTO)))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(ticketTypeService);
        }

        @Test
        void updateShouldReturnBadRequestWithBlankDescription() throws Exception {
            UpdateTicketTypeDTO updateDTO = new UpdateTicketTypeDTO(ticketName, "          ", ticketPrice);

            mockMvc.perform(patch("/v1/ticket-types/{ticketTypeId}", ticketTypeId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDTO)))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(ticketTypeService);
        }

        @Test
        void updateShouldReturnBadRequestWithDescriptionLongerThanMaximumLength() throws Exception {
            UpdateTicketTypeDTO updateDTO = new UpdateTicketTypeDTO(ticketName, "a".repeat(501), ticketPrice);

            mockMvc.perform(patch("/v1/ticket-types/{ticketTypeId}", ticketTypeId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDTO)))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(ticketTypeService);
        }

        @Test
        void updateShouldReturnBadRequestWithNegativePrice() throws Exception {
            UpdateTicketTypeDTO updateDTO = new UpdateTicketTypeDTO(ticketName, ticketDescription, new BigDecimal("-0.01"));

            mockMvc.perform(patch("/v1/ticket-types/{ticketTypeId}", ticketTypeId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDTO)))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(ticketTypeService);
        }
    }


    private void assertJsonTicketTypeDtoDataIsCorrect(ResultActions resultActions, String prefix) throws Exception {
        resultActions.andExpect(jsonPath(prefix + ".id").value(ticketTypeId.toString()))
                .andExpect(jsonPath(prefix + ".name").value(ticketName))
                .andExpect(jsonPath(prefix + ".description").value(ticketDescription))
                .andExpect(jsonPath(prefix + ".price").value(ticketPrice.doubleValue()))
                .andExpect(jsonPath(prefix + ".availableQuantity").value(ticketInitialQuantity))
        ;
    }

    private void assertJsonTicketTypeDtoDataIsCorrect(ResultActions resultActions) throws Exception {
        assertJsonTicketTypeDtoDataIsCorrect(resultActions, "$");
    }
}
