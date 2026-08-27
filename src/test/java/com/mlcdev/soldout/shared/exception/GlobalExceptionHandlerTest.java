package com.mlcdev.soldout.shared.exception;

import com.mlcdev.soldout.event.exception.EventNotFoundException;
import com.mlcdev.soldout.event.exception.InvalidEventException;
import com.mlcdev.soldout.event.exception.InvalidEventPeriodException;
import com.mlcdev.soldout.event.exception.InvalidTicketTypeException;
import com.mlcdev.soldout.event.exception.InventoryInconsistencyException;
import com.mlcdev.soldout.event.exception.NotEnoughTicketsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.Objects;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private MockHttpServletRequest request;
    private String requestPath;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = new MockHttpServletRequest();
        requestPath = "/v1/test";
        request.setRequestURI(requestPath);
    }

    @Test
    void invalidEventPeriodShouldReturnBadRequestWithEndsAtFieldError() {
        String message = "event endsAt must be after startsAt";
        InvalidEventPeriodException exception = new InvalidEventPeriodException(message);

        ResponseEntity<ApiErrorDTO> response = handler.handleInvalidEventPeriod(exception, request);

        ApiErrorDTO body = assertBasicError(response, HttpStatus.BAD_REQUEST, "data validation not passed");
        assertThat(body.errors())
                .containsExactly(new FieldErrorDTO("endsAt", message));
    }

    @Test
    void invalidEventShouldReturnConflictWithExceptionMessage() {
        String message = "event cannot be modified";
        InvalidEventException exception = new InvalidEventException(message);

        ResponseEntity<ApiErrorDTO> response = handler.handleInvalidEvent(exception, request);

        assertBasicError(response, HttpStatus.CONFLICT, message);
    }

    @Test
    void invalidTicketTypeShouldReturnBadRequestWithExceptionMessage() {
        String message = "ticket type is invalid";
        InvalidTicketTypeException exception = new InvalidTicketTypeException(message);

        ResponseEntity<ApiErrorDTO> response = handler.handleInvalidTicketType(exception, request);

        assertBasicError(response, HttpStatus.BAD_REQUEST, message);
    }

    @Test
    void notEnoughTicketsShouldReturnConflictWithExceptionMessage() {
        NotEnoughTicketsException exception = new NotEnoughTicketsException(5, 2);

        ResponseEntity<ApiErrorDTO> response = handler.handleNotEnoughTickets(exception, request);

        assertBasicError(response, HttpStatus.CONFLICT, exception.getMessage());
    }

    @Test
    void resourceNotFoundShouldReturnNotFoundWithExceptionMessage() {
        EventNotFoundException exception = new EventNotFoundException(UUID.randomUUID());

        ResponseEntity<ApiErrorDTO> response = handler.handleResourceNotFound(exception, request);

        assertBasicError(response, HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @Test
    void inventoryInconsistencyShouldReturnInternalServerErrorWithoutExposingDetails() {
        InventoryInconsistencyException exception = new InventoryInconsistencyException(
                UUID.randomUUID(), 5, 8, 10
        );

        ResponseEntity<ApiErrorDTO> response = handler.handleInventoryInconsistency(exception, request);

        ApiErrorDTO body = assertBasicError(response, HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error");
        assertThat(body.error()).doesNotContain(exception.getMessage());
    }

    @Test
    void pessimisticLockTimeoutShouldReturnServiceUnavailableWithRetryAfterHeader() {
        ResponseEntity<ApiErrorDTO> response = handler.handlePessimisticLockTimeoutException(request);

        assertBasicError(
                response,
                HttpStatus.SERVICE_UNAVAILABLE,
                "Reservation temporarily unavailable, retry later"
        );
        assertThat(response.getHeaders().getFirst(HttpHeaders.RETRY_AFTER)).isEqualTo("1");
    }

    @Test
    void unexpectedExceptionShouldReturnInternalServerErrorWithoutExposingDetails() {
        Exception exception = new Exception("database password must not be exposed");

        ResponseEntity<ApiErrorDTO> response = handler.handleException(exception, request);

        ApiErrorDTO body = assertBasicError(response, HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error");
        assertThat(body.error()).doesNotContain(exception.getMessage());
    }

    private ApiErrorDTO assertBasicError(
            ResponseEntity<ApiErrorDTO> response,
            HttpStatus expectedStatus,
            String expectedMessage
    ) {
        ApiErrorDTO body = Objects.requireNonNull(response.getBody(), "response body must not be null");

        assertThat(response.getStatusCode()).isEqualTo(expectedStatus);
        assertThat(body.timestamp()).isNotNull();
        assertThat(body.status()).isEqualTo(expectedStatus.value());
        assertThat(body.error()).isEqualTo(expectedMessage);
        assertThat(body.path()).isEqualTo(requestPath);
        return body;
    }
}
