package com.mlcdev.soldout.event.exceptions;

import lombok.Getter;

@Getter
public class NotEnoughTicketsException extends RuntimeException {

    private final int requested;
    private final int available;

    public NotEnoughTicketsException(int requested, int available) {
        super("requested " + requested + " tickets but only " + available + " are available");
        this.requested = requested;
        this.available = available;
    }
}
