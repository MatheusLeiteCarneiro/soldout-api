package com.mlcdev.soldout.event.exceptions;

public class InvalidTicketTypeException extends RuntimeException {
    public InvalidTicketTypeException(String message) {
        super(message);
    }
}
