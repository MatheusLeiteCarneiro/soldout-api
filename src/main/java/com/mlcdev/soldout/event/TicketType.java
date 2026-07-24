package com.mlcdev.soldout.event;

import com.mlcdev.soldout.event.exceptions.InvalidTicketTypeException;
import com.mlcdev.soldout.event.exceptions.InventoryInconsistencyException;
import com.mlcdev.soldout.event.exceptions.NotEnoughTicketsException;
import com.mlcdev.soldout.shared.IdGenerator;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "ticket_types")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TicketType {

    @Id
    @EqualsAndHashCode.Include
    private UUID id;
    @Column(nullable = false, length = 100)
    private String name;
    @Column(nullable = false, length = 500)
    private String description;
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
    @Column(nullable = false, name = "total_quantity")
    private int totalQuantity;
    @Column(nullable = false, name = "available_quantity")
    private int availableQuantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    public TicketType(@NonNull String name, @NonNull String description, @NonNull BigDecimal price, int totalQuantity, @NonNull Event event) {
        validateQuantityAboveZero(totalQuantity);
        validateNonNegativePrice(price);
        this.id = IdGenerator.newId();
        this.name = name.trim();
        this.description = description.trim();
        this.price = price;
        this.totalQuantity = totalQuantity;
        this.availableQuantity = totalQuantity;
        this.event = event;
    }


    public void reserve(int quantity) {
        validateQuantityAboveZero(quantity);
        if (quantity > availableQuantity) {
            throw new NotEnoughTicketsException(quantity, availableQuantity);
        }
        availableQuantity -= quantity;
    }

    public void release(int quantity) {
        validateQuantityAboveZero(quantity);
        if (availableQuantity + quantity > totalQuantity) {
            throw new InventoryInconsistencyException(id, quantity, availableQuantity, totalQuantity);
        }
        availableQuantity += quantity;
    }

    private void validateQuantityAboveZero(int quantity) {
        if (quantity <= 0) {
            throw new InvalidTicketTypeException("the quantity must be greater than 0");
        }
    }

    private void validateNonNegativePrice(BigDecimal price) {
        if (price.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidTicketTypeException("the price must be 0 or greater");
        }
    }
}
