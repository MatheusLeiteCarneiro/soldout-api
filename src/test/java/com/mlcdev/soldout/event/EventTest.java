package com.mlcdev.soldout.event;

import com.mlcdev.soldout.event.exception.InvalidEventException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EventTest {

    private static final Duration ONE_DAY = Duration.ofDays(1);
    private static final BigDecimal TICKET_PRICE = new BigDecimal("50.00");

    private String name;
    private String description;
    private Instant startsAt;
    private Instant endsAt;

    @BeforeEach
    void setUp() {
        name = "valid name";
        description = "valid description";
        startsAt = Instant.now().plus(ONE_DAY);
        endsAt = Instant.now().plus(ONE_DAY.multipliedBy(2));
    }

    private Event draftEvent() {
        return new Event(name, description, startsAt, endsAt);
    }

    private Event publishedEvent() {
        Event event = draftEvent();
        event.addTicketType("Student", "Student ticket", TICKET_PRICE, 200);
        event.publish();
        return event;
    }

    private Event finishedEvent() {
        Event event = publishedEvent();
        event.finish();
        return event;
    }

    private Event cancelledEvent() {
        Event event = draftEvent();
        event.cancel();
        return event;
    }

    @Nested
    @DisplayName("creation")
    class Creation {

        @Test
        void shouldCreateEventInDraftStatus() {
            Event event = draftEvent();

            assertThat(event.getId()).isNotNull();
            assertThat(event.getStatus()).isEqualTo(EventStatus.DRAFT);
            assertThat(event.getName()).isEqualTo(name);
            assertThat(event.getDescription()).isEqualTo(description);
            assertThat(event.getStartsAt()).isEqualTo(startsAt);
            assertThat(event.getEndsAt()).isEqualTo(endsAt);
            assertThat(event.getTicketTypes()).isEmpty();
        }

        @Test
        void shouldTrimNameAndDescription() {
            Event event = new Event("  spaced name  ", "  spaced description  ", startsAt, endsAt);

            assertThat(event.getName()).isEqualTo("spaced name");
            assertThat(event.getDescription()).isEqualTo("spaced description");
        }

        @Test
        void shouldGenerateUniqueIds() {
            Event first = draftEvent();
            Event second = draftEvent();

            assertThat(first.getId()).isNotEqualTo(second.getId());
        }

        @Test
        void shouldRejectNullName() {
            assertThatThrownBy(() -> new Event(null, description, startsAt, endsAt))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        void shouldRejectNullDescription() {
            assertThatThrownBy(() -> new Event(name, null, startsAt, endsAt))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        void shouldRejectNullStartDate() {
            assertThatThrownBy(() -> new Event(name, description, null, endsAt))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        void shouldRejectNullEndDate() {
            assertThatThrownBy(() -> new Event(name, description, startsAt, null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        void shouldRejectEndDateBeforeStartDate() {
            assertThatThrownBy(() -> new Event(name, description, endsAt, startsAt))
                    .isInstanceOf(InvalidEventException.class)
                    .hasMessageContaining("endsAt must be after startsAt");
        }

        @Test
        void shouldRejectZeroLengthPeriod() {
            assertThatThrownBy(() -> new Event(name, description, startsAt, startsAt))
                    .isInstanceOf(InvalidEventException.class)
                    .hasMessageContaining("endsAt must be after startsAt");
        }
    }

    @Nested
    @DisplayName("publish")
    class Publish {

        @Test
        void shouldPublishDraftEvent() {
            Event event = draftEvent();
            event.addTicketType("Student", "Student ticket", TICKET_PRICE, 200);

            event.publish();

            assertThat(event.getStatus()).isEqualTo(EventStatus.PUBLISHED);
        }

        @Test
        void shouldRejectPublishingEventWithoutTicketTypes() {
            Event event = draftEvent();

            assertThatThrownBy(event::publish)
                    .isInstanceOf(InvalidEventException.class)
                    .hasMessageContaining("without ticket types");

            assertThat(event.getStatus()).isEqualTo(EventStatus.DRAFT);
        }

        @Test
        void shouldRejectPublishingAlreadyPublishedEvent() {
            Event event = publishedEvent();

            assertThatThrownBy(event::publish)
                    .isInstanceOf(InvalidEventException.class)
                    .hasMessageContaining("only draft events can be published");
        }

        @Test
        void shouldRejectPublishingCancelledEvent() {
            Event event = cancelledEvent();

            assertThatThrownBy(event::publish)
                    .isInstanceOf(InvalidEventException.class)
                    .hasMessageContaining("only draft events can be published");
        }
    }

    @Nested
    @DisplayName("cancel")
    class Cancel {

        @Test
        void shouldCancelDraftEvent() {
            Event event = draftEvent();

            event.cancel();

            assertThat(event.getStatus()).isEqualTo(EventStatus.CANCELLED);
        }

        @Test
        void shouldCancelPublishedEvent() {
            Event event = publishedEvent();

            event.cancel();

            assertThat(event.getStatus()).isEqualTo(EventStatus.CANCELLED);
        }

        @Test
        void shouldRejectCancellingFinishedEvent() {
            Event event = finishedEvent();

            assertThatThrownBy(event::cancel)
                    .isInstanceOf(InvalidEventException.class)
                    .hasMessageContaining("finished event cannot be cancelled");
        }

        @Test
        void shouldRejectCancellingTwice() {
            Event event = cancelledEvent();

            assertThatThrownBy(event::cancel)
                    .isInstanceOf(InvalidEventException.class)
                    .hasMessageContaining("already cancelled");
        }
    }

    @Nested
    @DisplayName("restore")
    class Restore {

        @Test
        void shouldRestoreCancelledEvent() {
            Event event = cancelledEvent();

            event.restore();

            assertThat(event.getStatus()).isEqualTo(EventStatus.DRAFT);
        }

        @Test
        void shouldRejectRestorePublishedEvent() {
            Event event = publishedEvent();

            assertThatThrownBy(event::restore)
                    .isInstanceOf(InvalidEventException.class)
                    .hasMessageContaining("only cancelled events can be restored");
        }

        @Test
        void shouldRejectRestoreFinishedEvent() {
            Event event = finishedEvent();

            assertThatThrownBy(event::restore)
                    .isInstanceOf(InvalidEventException.class)
                    .hasMessageContaining("only cancelled events can be restored");
        }

        @Test
        void shouldRejectRestoreDraftEvent() {
            Event event = draftEvent();

            assertThatThrownBy(event::restore)
                    .isInstanceOf(InvalidEventException.class)
                    .hasMessageContaining("only cancelled events can be restored");
        }

        @Test
        void shouldRejectRestoreTwice() {
            Event event = cancelledEvent();

            event.restore();

            assertThatThrownBy(event::restore)
                    .isInstanceOf(InvalidEventException.class)
                    .hasMessageContaining("only cancelled events can be restored");
        }
    }

    @Nested
    @DisplayName("finish")
    class Finish {

        @Test
        void shouldFinishPublishedEvent() {
            Event event = publishedEvent();

            event.finish();

            assertThat(event.getStatus()).isEqualTo(EventStatus.FINISHED);
        }

        @Test
        void shouldRejectFinishingDraftEvent() {
            Event event = draftEvent();

            assertThatThrownBy(event::finish)
                    .isInstanceOf(InvalidEventException.class)
                    .hasMessageContaining("only published events can be finished");
        }

        @Test
        void shouldRejectFinishingCancelledEvent() {
            Event event = cancelledEvent();

            assertThatThrownBy(event::finish)
                    .isInstanceOf(InvalidEventException.class)
                    .hasMessageContaining("only published events can be finished");
        }
    }

    @Nested
    @DisplayName("ensureOpenForReservation")
    class EnsureOpenForReservation {

        private Event endedPublishedEvent() {
            Event event = publishedEvent();
            event.reschedule(Instant.now().minus(ONE_DAY.multipliedBy(2)), Instant.now().minus(ONE_DAY));
            return event;
        }

        @Test
        void shouldAcceptPublishedEventThatHasNotEnded() {
            Event event = publishedEvent();

            assertThatCode(event::ensureOpenForReservation).doesNotThrowAnyException();
        }

        @Test
        void shouldRejectDraftEvent() {
            Event event = draftEvent();

            assertThatThrownBy(event::ensureOpenForReservation)
                    .isInstanceOf(InvalidEventException.class)
                    .hasMessageContaining("only be reserved for published events");
        }

        @Test
        void shouldRejectCancelledEvent() {
            Event event = cancelledEvent();

            assertThatThrownBy(event::ensureOpenForReservation)
                    .isInstanceOf(InvalidEventException.class)
                    .hasMessageContaining("only be reserved for published events");
        }

        @Test
        void shouldRejectFinishedEvent() {
            Event event = finishedEvent();

            assertThatThrownBy(event::ensureOpenForReservation)
                    .isInstanceOf(InvalidEventException.class)
                    .hasMessageContaining("only be reserved for published events");
        }

        @Test
        void shouldRejectPublishedEventThatAlreadyEnded() {
            Event event = endedPublishedEvent();

            assertThatThrownBy(event::ensureOpenForReservation)
                    .isInstanceOf(InvalidEventException.class)
                    .hasMessageContaining("already ended");
        }
    }

    @Nested
    @DisplayName("updateDetails")
    class UpdateDetails {

        @Test
        void shouldUpdateDetailsOfDraftEvent() {
            Event event = draftEvent();

            event.updateDetails("  new name  ", "  new description  ");

            assertThat(event.getName()).isEqualTo("new name");
            assertThat(event.getDescription()).isEqualTo("new description");
        }

        @Test
        void shouldUpdateDetailsOfPublishedEvent() {
            Event event = publishedEvent();

            event.updateDetails("new name", "new description");

            assertThat(event.getName()).isEqualTo("new name");
        }

        @Test
        void shouldRejectNullName() {
            Event event = draftEvent();

            assertThatThrownBy(() -> event.updateDetails(null, "new description"))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        void shouldRejectNullDescription() {
            Event event = draftEvent();

            assertThatThrownBy(() -> event.updateDetails("new name", null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        void shouldRejectUpdatingFinishedEvent() {
            Event event = finishedEvent();

            assertThatThrownBy(() -> event.updateDetails("new name", "new description"))
                    .isInstanceOf(InvalidEventException.class)
                    .hasMessageContaining("cannot modify an event with status FINISHED");
        }

        @Test
        void shouldRejectUpdatingCancelledEvent() {
            Event event = cancelledEvent();

            assertThatThrownBy(() -> event.updateDetails("new name", "new description"))
                    .isInstanceOf(InvalidEventException.class)
                    .hasMessageContaining("cannot modify an event with status CANCELLED");
        }
    }

    @Nested
    @DisplayName("reschedule")
    class Reschedule {

        @Test
        void shouldRescheduleDraftEvent() {
            Event event = draftEvent();
            Instant newStart = startsAt.plus(ONE_DAY);
            Instant newEnd = endsAt.plus(ONE_DAY);

            event.reschedule(newStart, newEnd);

            assertThat(event.getStartsAt()).isEqualTo(newStart);
            assertThat(event.getEndsAt()).isEqualTo(newEnd);
        }

        @Test
        void shouldRejectInvalidPeriodOnReschedule() {
            Event event = draftEvent();

            assertThatThrownBy(() -> event.reschedule(endsAt, startsAt))
                    .isInstanceOf(InvalidEventException.class)
                    .hasMessageContaining("endsAt must be after startsAt");
        }

        @Test
        void shouldRejectNullStartDate() {
            Event event = draftEvent();
            Instant newEnd = endsAt.plus(ONE_DAY);

            assertThatThrownBy(() -> event.reschedule(null, newEnd))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        void shouldRejectNullEndDate() {
            Event event = draftEvent();
            Instant newStart = startsAt.plus(ONE_DAY);

            assertThatThrownBy(() -> event.reschedule(newStart, null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        void shouldRejectReschedulingFinishedEvent() {
            Event event = finishedEvent();
            Instant newStart = startsAt.plus(ONE_DAY);
            Instant newEnd = endsAt.plus(ONE_DAY);

            assertThatThrownBy(() -> event.reschedule(newStart, newEnd))
                    .isInstanceOf(InvalidEventException.class)
                    .hasMessageContaining("cannot modify an event with status FINISHED");
        }

        @Test
        void shouldRejectReschedulingCancelledEvent() {
            Event event = cancelledEvent();
            Instant newStart = startsAt.plus(ONE_DAY);
            Instant newEnd = endsAt.plus(ONE_DAY);

            assertThatThrownBy(() -> event.reschedule(newStart, newEnd))
                    .isInstanceOf(InvalidEventException.class)
                    .hasMessageContaining("cannot modify an event with status CANCELLED");
        }
    }

    @Nested
    @DisplayName("addTicketType")
    class AddTicketType {

        @Test
        void shouldAddTicketTypeToDraftEvent() {
            Event event = draftEvent();

            event.addTicketType("Student", "Student ticket", TICKET_PRICE, 200);

            assertThat(event.getTicketTypes())
                    .singleElement()
                    .satisfies(added -> {
                        assertThat(added.getName()).isEqualTo("Student");
                        assertThat(added.getEvent()).isSameAs(event);
                        assertThat(added.getTotalQuantity()).isEqualTo(200);
                        assertThat(added.getAvailableQuantity()).isEqualTo(200);
                    });
        }

        @Test
        void shouldAddMultipleTicketTypes() {
            Event event = draftEvent();

            event.addTicketType("Student", "Student ticket", TICKET_PRICE, 200);
            event.addTicketType("Regular", "Regular ticket", new BigDecimal("150.00"), 500);

            assertThat(event.getTicketTypes()).hasSize(2);
        }

        @Test
        void shouldRejectNullTicketName() {
            Event event = draftEvent();

            assertThatThrownBy(() -> event.addTicketType(null, "Student ticket", TICKET_PRICE, 200))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        void shouldRejectNullTicketDescription() {
            Event event = draftEvent();

            assertThatThrownBy(() -> event.addTicketType("Student", null, TICKET_PRICE, 200))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        void shouldRejectNullTicketPrice() {
            Event event = draftEvent();

            assertThatThrownBy(() -> event.addTicketType("Student", "Student ticket", null, 200))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        void shouldRejectAddingTicketTypeToFinishedEvent() {
            Event event = finishedEvent();

            assertThatThrownBy(() -> event.addTicketType("Student", "Student ticket", TICKET_PRICE, 200))
                    .isInstanceOf(InvalidEventException.class)
                    .hasMessageContaining("cannot modify an event with status FINISHED");
        }

        @Test
        void shouldRejectAddingTicketTypeToCancelledEvent() {
            Event event = cancelledEvent();

            assertThatThrownBy(() -> event.addTicketType("Student", "Student ticket", TICKET_PRICE, 200))
                    .isInstanceOf(InvalidEventException.class)
                    .hasMessageContaining("cannot modify an event with status CANCELLED");
        }

        @Test
        void shouldReturnUnmodifiableTicketTypes() {
            Event event = draftEvent();
            event.addTicketType("Student", "Student ticket", TICKET_PRICE, 200);
            Set<TicketType> ticketTypes = event.getTicketTypes();

            assertThatThrownBy(ticketTypes::clear)
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }
}