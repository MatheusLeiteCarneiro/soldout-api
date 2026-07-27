package com.mlcdev.soldout.event;

import com.mlcdev.soldout.TestcontainersConfiguration;
import jakarta.persistence.Query;
import org.hibernate.SessionFactory;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class EventRepositoryIT {

    private static final int TICKET_TYPE_TOTAL_QUANTITY = 10;
    private static final BigDecimal TICKET_TYPE_PRICE = new BigDecimal("100.00");

    @Autowired
    private TestEntityManager testEntityManager;

    @Autowired
    private EventRepository eventRepository;

    private String name;
    private String description;
    private Instant startsAt;
    private Instant endsAt;
    private String ticketTypeName;
    private String ticketTypeDescription;

    @BeforeEach
    void setUp() {
        name = "Valid event name";
        description = "Valid event description";
        startsAt = Instant.now().plus(Duration.ofDays(1)).truncatedTo(ChronoUnit.MICROS);
        endsAt = Instant.now().plus(Duration.ofDays(2)).truncatedTo(ChronoUnit.MICROS);
        ticketTypeName = "Valid ticketType name";
        ticketTypeDescription = "Valid ticketType description";
    }

    private Event persistedEventWithTicketType() {
        Event event = new Event(name, description, startsAt, endsAt);
        event.addTicketType(ticketTypeName, ticketTypeDescription,
                TICKET_TYPE_PRICE, TICKET_TYPE_TOTAL_QUANTITY);

        eventRepository.save(event);
        testEntityManager.flush();
        testEntityManager.clear();

        return event;
    }

    private UUID ticketTypeIdOf(Event event) {
        return event.getTicketTypes().iterator().next().getId();
    }

    private Query nativeQuery(String sql) {
        return testEntityManager.getEntityManager().createNativeQuery(sql);
    }

    @Nested
    @DisplayName("mapping")
    class Mapping {

        private Statistics resetStatistics() {
            Statistics stats = testEntityManager.getEntityManager()
                    .getEntityManagerFactory()
                    .unwrap(SessionFactory.class)
                    .getStatistics();

            stats.setStatisticsEnabled(true);
            stats.clear();

            return stats;
        }

        @Test
        void shouldPersistAndRestoreEventWithTicketType() {
            Event event = persistedEventWithTicketType();

            Event found = eventRepository.findById(event.getId()).orElseThrow();

            assertThat(found.getId()).isEqualTo(event.getId());
            assertThat(found.getName()).isEqualTo(event.getName());
            assertThat(found.getDescription()).isEqualTo(event.getDescription());
            assertThat(found.getStartsAt()).isEqualTo(event.getStartsAt());
            assertThat(found.getEndsAt()).isEqualTo(event.getEndsAt());
            assertThat(found.getStatus()).isEqualTo(event.getStatus());

            TicketType original = event.getTicketTypes().iterator().next();

            assertThat(found.getTicketTypes())
                    .singleElement()
                    .satisfies(persisted -> {
                        assertThat(persisted.getId()).isEqualTo(original.getId());
                        assertThat(persisted.getName()).isEqualTo(original.getName());
                        assertThat(persisted.getDescription()).isEqualTo(original.getDescription());
                        assertThat(persisted.getPrice()).isEqualByComparingTo(original.getPrice());
                        assertThat(persisted.getTotalQuantity()).isEqualTo(original.getTotalQuantity());
                        assertThat(persisted.getAvailableQuantity()).isEqualTo(original.getAvailableQuantity());
                    });
        }

        @Test
        void findByIdShouldLoadTicketTypesInASeparateQuery() {
            Event event = persistedEventWithTicketType();
            Statistics stats = resetStatistics();

            Event found = eventRepository.findById(event.getId()).orElseThrow();

            // make another query to load the ticket types
            found.getTicketTypes().iterator().next();

            assertThat(stats.getPrepareStatementCount()).isEqualTo(2);
        }

        @Test
        void findByIdWithTicketTypesShouldLoadEverythingInASingleQuery() {
            Event event = persistedEventWithTicketType();
            Statistics stats = resetStatistics();

            Event found = eventRepository.findByIdWithTicketTypes(event.getId()).orElseThrow();
            // ticket types already loaded with event
            found.getTicketTypes().iterator().next();

            assertThat(stats.getPrepareStatementCount()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("database constraints")
    class Constraints {

        @Test
        void shouldRejectStartDateAfterEndDate() {
            Event event = persistedEventWithTicketType();

            Query query = nativeQuery("UPDATE events SET starts_at = :invalidStart WHERE id = :eventId")
                    .setParameter("invalidStart", endsAt.plus(Duration.ofDays(1)))
                    .setParameter("eventId", event.getId());

            assertThatThrownBy(query::executeUpdate)
                    .isInstanceOf(ConstraintViolationException.class)
                    .hasMessageContaining("chk_events_period");
        }

        @Test
        void shouldRejectUnknownEventStatus() {
            Event event = persistedEventWithTicketType();

            Query query = nativeQuery("UPDATE events SET status = :invalidStatus WHERE id = :eventId")
                    .setParameter("invalidStatus", "INVALID")
                    .setParameter("eventId", event.getId());

            assertThatThrownBy(query::executeUpdate)
                    .isInstanceOf(ConstraintViolationException.class)
                    .hasMessageContaining("chk_events_status");
        }

        @Test
        void shouldRejectNegativePrice() {
            Event event = persistedEventWithTicketType();

            Query query = nativeQuery("UPDATE ticket_types SET price = :invalidPrice WHERE id = :ticketTypeId")
                    .setParameter("invalidPrice", new BigDecimal("-10.00"))
                    .setParameter("ticketTypeId", ticketTypeIdOf(event));

            assertThatThrownBy(query::executeUpdate)
                    .isInstanceOf(ConstraintViolationException.class)
                    .hasMessageContaining("chk_ticket_types_price_not_negative");
        }

        @Test
        void shouldRejectNegativeAvailableQuantity() {
            Event event = persistedEventWithTicketType();

            Query query = nativeQuery("UPDATE ticket_types SET available_quantity = :invalidQuantity WHERE id = :ticketTypeId")
                    .setParameter("invalidQuantity", -1)
                    .setParameter("ticketTypeId", ticketTypeIdOf(event));

            assertThatThrownBy(query::executeUpdate)
                    .isInstanceOf(ConstraintViolationException.class)
                    .hasMessageContaining("chk_ticket_types_available_not_negative");
        }

        @Test
        void shouldRejectAvailableQuantityGreaterThanTotal() {
            Event event = persistedEventWithTicketType();

            Query query = nativeQuery("""
                            UPDATE ticket_types
                               SET available_quantity = :available, total_quantity = :total
                             WHERE id = :ticketTypeId
                            """)
                    .setParameter("available", TICKET_TYPE_TOTAL_QUANTITY)
                    .setParameter("total", TICKET_TYPE_TOTAL_QUANTITY - 1)
                    .setParameter("ticketTypeId", ticketTypeIdOf(event));

            assertThatThrownBy(query::executeUpdate)
                    .isInstanceOf(ConstraintViolationException.class)
                    .hasMessageContaining("chk_ticket_types_available_within_total");
        }

        @Test
        void shouldRejectZeroTotalQuantity() {
            Event event = persistedEventWithTicketType();

            // available is zero so only the total_positive constraint is violated
            Query query = nativeQuery("""
                            UPDATE ticket_types
                               SET available_quantity = 0, total_quantity = 0
                             WHERE id = :ticketTypeId
                            """)
                    .setParameter("ticketTypeId", ticketTypeIdOf(event));

            assertThatThrownBy(query::executeUpdate)
                    .isInstanceOf(ConstraintViolationException.class)
                    .hasMessageContaining("chk_ticket_types_total_positive");
        }
    }
}