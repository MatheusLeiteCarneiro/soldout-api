package com.mlcdev.soldout.event;

import com.mlcdev.soldout.event.exception.InvalidTicketTypeException;
import com.mlcdev.soldout.event.exception.InventoryInconsistencyException;
import com.mlcdev.soldout.event.exception.NotEnoughTicketsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TicketTypeTest {

    private static final Duration ONE_DAY = Duration.ofDays(1);
    private static final BigDecimal PRICE = new BigDecimal("50.00");
    private static final String NAME = "Student";
    private static final String DESCRIPTION = "Student ticket";
    private static final int TOTAL_QUANTITY = 200;

    private Event event;

    @BeforeEach
    void setUp() {
        event = new Event(
                "valid name",
                "valid description",
                Instant.now().plus(ONE_DAY),
                Instant.now().plus(ONE_DAY.multipliedBy(2)));
    }

    private TicketType ticketType() {
        return ticketTypeWithStock(TOTAL_QUANTITY);
    }

    private TicketType ticketTypeWithStock(int quantity) {
        return new TicketType(NAME, DESCRIPTION, PRICE, quantity, event);
    }

    @Nested
    @DisplayName("creation")
    class Creation {

        @Test
        void shouldCreateTicketTypeWithFullAvailability() {
            TicketType ticketType = ticketType();

            assertThat(ticketType.getId()).isNotNull();
            assertThat(ticketType.getName()).isEqualTo(NAME);
            assertThat(ticketType.getDescription()).isEqualTo(DESCRIPTION);
            assertThat(ticketType.getPrice()).isEqualByComparingTo(PRICE);
            assertThat(ticketType.getTotalQuantity()).isEqualTo(TOTAL_QUANTITY);
            assertThat(ticketType.getAvailableQuantity()).isEqualTo(TOTAL_QUANTITY);
            assertThat(ticketType.getEvent()).isSameAs(event);
        }

        @Test
        void shouldTrimNameAndDescription() {
            TicketType ticketType =
                    new TicketType("  spaced name  ", "  spaced description  ", PRICE, TOTAL_QUANTITY, event);

            assertThat(ticketType.getName()).isEqualTo("spaced name");
            assertThat(ticketType.getDescription()).isEqualTo("spaced description");
        }

        @Test
        void shouldGenerateUniqueIds() {
            TicketType first = ticketType();
            TicketType second = ticketType();

            assertThat(first.getId()).isNotEqualTo(second.getId());
        }

        @Test
        void shouldAcceptZeroPriceForFreeEvents() {
            TicketType ticketType =
                    new TicketType(NAME, DESCRIPTION, BigDecimal.ZERO, TOTAL_QUANTITY, event);

            assertThat(ticketType.getPrice()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        void shouldRejectNegativePrice() {
            BigDecimal negativePrice = new BigDecimal("-1.00");

            assertThatThrownBy(() -> new TicketType(NAME, DESCRIPTION, negativePrice, TOTAL_QUANTITY, event))
                    .isInstanceOf(InvalidTicketTypeException.class)
                    .hasMessageContaining("price must be 0 or greater");
        }

        @Test
        void shouldRejectZeroTotalQuantity() {
            assertThatThrownBy(() -> new TicketType(NAME, DESCRIPTION, PRICE, 0, event))
                    .isInstanceOf(InvalidTicketTypeException.class)
                    .hasMessageContaining("quantity must be greater than 0");
        }

        @Test
        void shouldRejectNegativeTotalQuantity() {
            assertThatThrownBy(() -> new TicketType(NAME, DESCRIPTION, PRICE, -10, event))
                    .isInstanceOf(InvalidTicketTypeException.class)
                    .hasMessageContaining("quantity must be greater than 0");
        }

        @Test
        void shouldRejectNullName() {
            assertThatThrownBy(() -> new TicketType(null, DESCRIPTION, PRICE, TOTAL_QUANTITY, event))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        void shouldRejectNullDescription() {
            assertThatThrownBy(() -> new TicketType(NAME, null, PRICE, TOTAL_QUANTITY, event))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        void shouldRejectNullPrice() {
            assertThatThrownBy(() -> new TicketType(NAME, DESCRIPTION, null, TOTAL_QUANTITY, event))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        void shouldRejectNullEvent() {
            assertThatThrownBy(() -> new TicketType(NAME, DESCRIPTION, PRICE, TOTAL_QUANTITY, null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("reserve")
    class Reserve {

        @Test
        void shouldDecreaseAvailabilityWithoutChangingTotal() {
            TicketType ticketType = ticketType();

            ticketType.reserve(50);

            assertThat(ticketType.getAvailableQuantity()).isEqualTo(150);
            assertThat(ticketType.getTotalQuantity()).isEqualTo(TOTAL_QUANTITY);
        }

        @Test
        void shouldAllowReservingAllRemainingTickets() {
            TicketType ticketType = ticketTypeWithStock(1);

            ticketType.reserve(1);

            assertThat(ticketType.getAvailableQuantity()).isZero();
        }

        @Test
        void shouldAccumulateSuccessiveReservations() {
            TicketType ticketType = ticketType();

            ticketType.reserve(30);
            ticketType.reserve(20);
            ticketType.reserve(50);

            assertThat(ticketType.getAvailableQuantity()).isEqualTo(100);
        }

        @Test
        void shouldRejectReservingMoreThanAvailable() {
            TicketType ticketType = ticketTypeWithStock(10);

            assertThatThrownBy(() -> ticketType.reserve(11))
                    .isInstanceOf(NotEnoughTicketsException.class);
        }

        @Test
        void shouldExposeRequestedAndAvailableOnFailure() {
            TicketType ticketType = ticketTypeWithStock(10);

            assertThatThrownBy(() -> ticketType.reserve(25))
                    .isInstanceOfSatisfying(NotEnoughTicketsException.class, exception -> {
                        assertThat(exception.getRequested()).isEqualTo(25);
                        assertThat(exception.getAvailable()).isEqualTo(10);
                    });
        }

        @Test
        void shouldNotChangeAvailabilityWhenReservationFails() {
            TicketType ticketType = ticketTypeWithStock(10);

            assertThatThrownBy(() -> ticketType.reserve(50))
                    .isInstanceOf(NotEnoughTicketsException.class);

            assertThat(ticketType.getAvailableQuantity()).isEqualTo(10);
        }

        @Test
        void shouldRejectZeroQuantity() {
            TicketType ticketType = ticketType();

            assertThatThrownBy(() -> ticketType.reserve(0))
                    .isInstanceOf(InvalidTicketTypeException.class)
                    .hasMessageContaining("quantity must be greater than 0");
        }

        @Test
        void shouldRejectNegativeQuantity() {
            TicketType ticketType = ticketType();

            assertThatThrownBy(() -> ticketType.reserve(-5))
                    .isInstanceOf(InvalidTicketTypeException.class)
                    .hasMessageContaining("quantity must be greater than 0");
        }

        @Test
        void shouldNotIncreaseAvailabilityWithNegativeQuantity() {
            TicketType ticketType = ticketType();

            assertThatThrownBy(() -> ticketType.reserve(-50))
                    .isInstanceOf(InvalidTicketTypeException.class);

            assertThat(ticketType.getAvailableQuantity()).isEqualTo(TOTAL_QUANTITY);
        }
    }

    @Nested
    @DisplayName("release")
    class Release {

        @Test
        void shouldRestorePartialAvailability() {
            TicketType ticketType = ticketType();
            ticketType.reserve(50);

            ticketType.release(30);

            assertThat(ticketType.getAvailableQuantity()).isEqualTo(180);
        }

        @Test
        void shouldRestoreFullAvailability() {
            TicketType ticketType = ticketType();
            ticketType.reserve(50);

            ticketType.release(50);

            assertThat(ticketType.getAvailableQuantity()).isEqualTo(TOTAL_QUANTITY);
        }

        @Test
        void shouldRejectReleasingBeyondTotalQuantity() {
            TicketType ticketType = ticketType();
            ticketType.reserve(10);

            assertThatThrownBy(() -> ticketType.release(11))
                    .isInstanceOf(InventoryInconsistencyException.class);
        }

        @Test
        void shouldRejectReleasingWhenNothingWasReserved() {
            TicketType ticketType = ticketType();

            assertThatThrownBy(() -> ticketType.release(1))
                    .isInstanceOf(InventoryInconsistencyException.class);
        }

        @Test
        void shouldExposeInventoryStateOnFailure() {
            TicketType ticketType = ticketTypeWithStock(100);
            ticketType.reserve(10);

            assertThatThrownBy(() -> ticketType.release(50))
                    .isInstanceOfSatisfying(InventoryInconsistencyException.class, exception -> {
                        assertThat(exception.getTicketTypeId()).isEqualTo(ticketType.getId());
                        assertThat(exception.getAttemptedRelease()).isEqualTo(50);
                        assertThat(exception.getAvailableQuantity()).isEqualTo(90);
                        assertThat(exception.getTotalQuantity()).isEqualTo(100);
                    });
        }

        @Test
        void shouldNotChangeAvailabilityWhenReleaseFails() {
            TicketType ticketType = ticketType();
            ticketType.reserve(10);

            assertThatThrownBy(() -> ticketType.release(100))
                    .isInstanceOf(InventoryInconsistencyException.class);

            assertThat(ticketType.getAvailableQuantity()).isEqualTo(190);
        }

        @Test
        void shouldRejectZeroQuantity() {
            TicketType ticketType = ticketType();
            ticketType.reserve(10);

            assertThatThrownBy(() -> ticketType.release(0))
                    .isInstanceOf(InvalidTicketTypeException.class)
                    .hasMessageContaining("quantity must be greater than 0");
        }

        @Test
        void shouldRejectNegativeQuantity() {
            TicketType ticketType = ticketType();
            ticketType.reserve(10);

            assertThatThrownBy(() -> ticketType.release(-5))
                    .isInstanceOf(InvalidTicketTypeException.class)
                    .hasMessageContaining("quantity must be greater than 0");
        }
    }

    @Nested
    @DisplayName("reserve and release cycle")
    class Cycle {

        @Test
        void shouldReturnToInitialStateAfterFullCycle() {
            TicketType ticketType = ticketType();

            ticketType.reserve(30);
            ticketType.reserve(20);
            ticketType.release(20);
            ticketType.release(30);

            assertThat(ticketType.getAvailableQuantity()).isEqualTo(TOTAL_QUANTITY);
        }

        @Test
        void shouldNeverExceedTotalQuantityAcrossOperations() {
            TicketType ticketType = ticketTypeWithStock(5);

            ticketType.reserve(5);
            ticketType.release(5);

            assertThat(ticketType.getAvailableQuantity())
                    .isEqualTo(ticketType.getTotalQuantity());
        }
    }
}