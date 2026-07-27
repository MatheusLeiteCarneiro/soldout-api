package com.mlcdev.soldout.event;

import com.mlcdev.soldout.TestcontainersConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class TicketTypeConcurrencyIT {

    private static final int CONCURRENT_BUYERS = 50;
    private static final int AVAILABLE_TICKETS = 1;

    @Autowired
    private TicketTypeService ticketTypeService;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private TicketTypeRepository ticketTypeRepository;

    private UUID ticketTypeId;

    @BeforeEach
    void setUp() {
        Event event = new Event(
                "Concurrency test event",
                "An event with a single ticket available",
                Instant.now().plus(Duration.ofDays(1)),
                Instant.now().plus(Duration.ofDays(2)));

        event.addTicketType("Last one", "The only ticket left",
                new BigDecimal("100.00"), AVAILABLE_TICKETS);

        eventRepository.save(event);

        ticketTypeId = event.getTicketTypes().iterator().next().getId();
    }

    @AfterEach
    void tearDown() {
        eventRepository.deleteAll();
    }

    @Test
    void shouldSellOnlyOneTicketWhenBuyersCompeteForTheLastOne() throws InterruptedException {
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(CONCURRENT_BUYERS);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failureCount = new AtomicInteger();
        ConcurrentLinkedQueue<Exception> failures = new ConcurrentLinkedQueue<>();

        try (ExecutorService executor = Executors.newFixedThreadPool(CONCURRENT_BUYERS)) {

            for (int i = 0; i < CONCURRENT_BUYERS; i++) {
                executor.submit(() -> {
                    try {
                        startLatch.await();
                        ticketTypeService.reserve(ticketTypeId, 1);
                        successCount.incrementAndGet();
                    } catch (Exception e) {
                        failureCount.incrementAndGet();
                        failures.add(e);
                    } finally {
                        doneLatch.countDown();
                    }
                });
            }

            startLatch.countDown();

            boolean finished = doneLatch.await(30, TimeUnit.SECONDS);
            assertThat(finished)
                    .as("all buyers should have finished within the timeout")
                    .isTrue();
        }

        printFailureSummary(failures);

        TicketType result = ticketTypeRepository.findById(ticketTypeId).orElseThrow();

        assertThat(successCount.get())
                .as("only one buyer should have reserved the last ticket")
                .isEqualTo(AVAILABLE_TICKETS);

        assertThat(failureCount.get())
                .isEqualTo(CONCURRENT_BUYERS - AVAILABLE_TICKETS);

        assertThat(result.getAvailableQuantity())
                .as("stock must never go below zero")
                .isZero();
    }

    private void printFailureSummary(ConcurrentLinkedQueue<Exception> failures) {
        List.copyOf(failures).stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        e -> e.getClass().getSimpleName(),
                        java.util.stream.Collectors.counting()))
                .forEach((type, count) -> System.out.printf("%-45s %d%n", type, count));
    }
}