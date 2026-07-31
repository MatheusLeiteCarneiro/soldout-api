package com.mlcdev.soldout.shared.exception;

import com.mlcdev.soldout.event.exception.InvalidEventException;
import com.mlcdev.soldout.event.exception.InvalidEventPeriodException;
import com.mlcdev.soldout.event.exception.InvalidTicketTypeException;
import com.mlcdev.soldout.event.exception.InventoryInconsistencyException;
import com.mlcdev.soldout.event.exception.NotEnoughTicketsException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@RestControllerAdvice
class GlobalExceptionHandler extends ResponseEntityExceptionHandler {


    // standardize MVC exceptions.

    @SuppressWarnings("java:S2638")
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException e, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        List<FieldErrorDTO> fieldErrors = new ArrayList<>();
        e.getBindingResult().getFieldErrors().forEach(f -> fieldErrors.add(new FieldErrorDTO(f.getField(), Objects.requireNonNullElse(f.getDefaultMessage(), "invalid value"))));
        ApiErrorDTO error = new ApiErrorDTO(status.value(), "data validation not passed", requestUri(request), fieldErrors);
        return ResponseEntity.status(status).body(error);
    }

    @SuppressWarnings("java:S2638")
    @Override
    protected @Nullable ResponseEntity<Object> handleExceptionInternal(Exception e, @Nullable Object body, HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {
        String message = HttpStatus.valueOf(statusCode.value()).getReasonPhrase();
        ApiErrorDTO error = new ApiErrorDTO(statusCode.value(), message, requestUri(request));
        return super.handleExceptionInternal(e, error, headers, statusCode, request);
    }

    private String requestUri(WebRequest request) {
        return ((ServletWebRequest) request).getRequest().getRequestURI();
    }


     // domain exceptions.


    @ExceptionHandler(InvalidEventPeriodException.class)
    public ResponseEntity<ApiErrorDTO> handleInvalidEventPeriod(InvalidEventPeriodException e, HttpServletRequest request){
        int status = HttpStatus.BAD_REQUEST.value();
        List<FieldErrorDTO> fieldErrors = List.of(new FieldErrorDTO("endsAt", e.getMessage()));
        ApiErrorDTO error = new ApiErrorDTO(status, "data validation not passed", request.getRequestURI(), fieldErrors);
        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(InvalidEventException.class)
    public ResponseEntity<ApiErrorDTO> handleInvalidEvent(InvalidEventException e, HttpServletRequest request){
        int status = HttpStatus.CONFLICT.value();
        ApiErrorDTO error = new ApiErrorDTO(status, e.getMessage(), request.getRequestURI());
        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(InvalidTicketTypeException.class)
    public ResponseEntity<ApiErrorDTO> handleInvalidTicketType(InvalidTicketTypeException e, HttpServletRequest request){
        int status = HttpStatus.BAD_REQUEST.value();
        ApiErrorDTO error = new ApiErrorDTO(status, e.getMessage(), request.getRequestURI());
        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(NotEnoughTicketsException.class)
    public ResponseEntity<ApiErrorDTO> handleNotEnoughTickets(NotEnoughTicketsException e, HttpServletRequest request){
        int status = HttpStatus.CONFLICT.value();
        ApiErrorDTO error = new ApiErrorDTO(status, e.getMessage(), request.getRequestURI());
        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorDTO> handleResourceNotFound(ResourceNotFoundException e, HttpServletRequest request){
        int status = HttpStatus.NOT_FOUND.value();
        ApiErrorDTO error = new ApiErrorDTO(status, e.getMessage(), request.getRequestURI());
        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(InventoryInconsistencyException.class)
    public ResponseEntity<ApiErrorDTO> handleInventoryInconsistency(InventoryInconsistencyException e, HttpServletRequest request){
        int status = HttpStatus.INTERNAL_SERVER_ERROR.value();
        ApiErrorDTO error = new ApiErrorDTO(status, "Unexpected error", request.getRequestURI());
        log.error("Unexpected inventory inconsistency error: {}",e.getMessage(), e);
        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorDTO> handleException(Exception e, HttpServletRequest request){
        int status = HttpStatus.INTERNAL_SERVER_ERROR.value();
        ApiErrorDTO error = new ApiErrorDTO(status, "Unexpected error", request.getRequestURI());
        log.error("Unexpected error: {}",e.getMessage(), e);
        return ResponseEntity.status(status).body(error);
    }
}
