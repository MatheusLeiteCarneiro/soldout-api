package com.mlcdev.soldout.event.exception;

public class InvalidTicketTypeException extends RuntimeException {
    public InvalidTicketTypeException(String message) {
        super(message);
    }
}
