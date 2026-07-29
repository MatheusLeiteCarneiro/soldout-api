package com.mlcdev.soldout.event.exception;

import lombok.Getter;

import java.util.UUID;

@Getter
public class InventoryInconsistencyException extends IllegalStateException {
    private final UUID ticketTypeId;
    private final int attemptedRelease;
    private final int availableQuantity;
    private final int totalQuantity;

    public InventoryInconsistencyException(UUID ticketTypeId, int attemptedRelease, int availableQuantity, int totalQuantity) {
        super("cannot release %d tickets from type %s: available %d, total %d"
                .formatted(attemptedRelease, ticketTypeId, availableQuantity, totalQuantity));
        this.ticketTypeId = ticketTypeId;
        this.attemptedRelease = attemptedRelease;
        this.availableQuantity = availableQuantity;
        this.totalQuantity = totalQuantity;
    }
}
