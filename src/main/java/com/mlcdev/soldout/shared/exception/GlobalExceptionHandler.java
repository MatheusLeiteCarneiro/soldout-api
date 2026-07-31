package com.mlcdev.soldout.shared.exception;

import com.mlcdev.soldout.event.exception.InvalidEventPeriodException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;


@RestControllerAdvice
class GlobalExceptionHandler {


    @ExceptionHandler(MethodArgumentNotValidException.class)
    private ResponseEntity<ApiErrorDTO> handleValidation(MethodArgumentNotValidException e, HttpServletRequest request){
        int status = HttpStatus.BAD_REQUEST.value();
        List<FieldErrorDTO> fieldErrors = new ArrayList<>();
        e.getBindingResult().getFieldErrors().forEach(f -> fieldErrors.add(new FieldErrorDTO(f.getField(), f.getDefaultMessage())));
        ApiErrorDTO error = new ApiErrorDTO(status, "data validation not passed", request.getRequestURI(), fieldErrors);
        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(InvalidEventPeriodException.class)
    private ResponseEntity<ApiErrorDTO> handleInvalidEventPeriod(InvalidEventPeriodException e, HttpServletRequest request){
        int status = HttpStatus.BAD_REQUEST.value();
        List<FieldErrorDTO> fieldErrors = List.of(new FieldErrorDTO("endsAt", e.getMessage()));
        ApiErrorDTO error = new ApiErrorDTO(status, "data validation not passed", request.getRequestURI(), fieldErrors);
        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    private ResponseEntity<ApiErrorDTO> handleResourceNotFound(ResourceNotFoundException e, HttpServletRequest request){
        int status = HttpStatus.NOT_FOUND.value();
        ApiErrorDTO error = new ApiErrorDTO(status, e.getMessage(), request.getRequestURI());
        return ResponseEntity.status(status).body(error);
    }
}
