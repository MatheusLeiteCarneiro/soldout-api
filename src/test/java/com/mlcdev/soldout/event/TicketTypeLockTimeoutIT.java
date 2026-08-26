package com.mlcdev.soldout.event;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import com.mlcdev.soldout.TestcontainersConfiguration;
import org.springframework.http.HttpHeaders;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;


@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = """
                spring.datasource.hikari.connection-init-sql=SET lock_timeout = '250ms'
                """
)
@Import(TestcontainersConfiguration.class)
class TicketTypeLockTimeoutIT {

    @LocalServerPort
    private int serverPort;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private TicketTypeRepository ticketTypeRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private UUID ticketTypeId;

    @BeforeEach
    void setUp() {
        Event event = new Event(
                "Lock timeout test",
                "Event used to test the HTTP lock timeout contract",
                Instant.now().plus(Duration.ofDays(1)),
                Instant.now().plus(Duration.ofDays(2)));
        TicketType ticketType = event.addTicketType(
                "Regular",
                "Regular ticket",
                new BigDecimal("100.0"),
                10);
        event.publish();
        eventRepository.saveAndFlush(event);
        ticketTypeId = ticketType.getId();
    }

    @AfterEach
    void tearDown() {
        eventRepository.deleteAll();
    }

    @Test
    void shouldReturnServiceUnavailableWhenTicketTypeRemainsLocked() throws Exception{
        CountDownLatch lockAcquired = new CountDownLatch(1);
        CountDownLatch releaseLock = new CountDownLatch(1);
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        try (ExecutorService executor = Executors.newSingleThreadExecutor()){
            Future<?> lockHolder = executor.submit(() -> transactionTemplate.executeWithoutResult(_ -> {
                ticketTypeRepository.findByIdWithEventForUpdate(ticketTypeId).orElseThrow();
                lockAcquired.countDown();
                awaitLatch(releaseLock);
            }));
            try {
                assertThat(lockAcquired.await(5, TimeUnit.SECONDS))
                        .as("the first transaction should acquire lock")
                        .isTrue();
                String path = "/v1/ticket-types/" + ticketTypeId + "/reserve";

                given()
                        .port(serverPort)
                        .contentType(ContentType.JSON)
                        .body("""
                                {
                                     "quantity" : 1
                                }
                                """)
                        .when()
                        .post(path)
                        .then()
                        .statusCode(503)
                        .header(HttpHeaders.RETRY_AFTER, "1")
                        .body("status", equalTo(503))
                        .body("error", equalTo("Reservation temporarily unavailable, retry later"))
                        .body("path", equalTo(path));
            }finally {
                releaseLock.countDown();
            }
        lockHolder.get(5, TimeUnit.SECONDS);
        }

        TicketType ticketType = ticketTypeRepository.findById(ticketTypeId).orElseThrow();
        assertThat(ticketType.getAvailableQuantity()).as("a timed-out reservation must not change stock")
                .isEqualTo(10);
    }

    private static void awaitLatch(CountDownLatch latch) {
        try {
            if (!latch.await(10, TimeUnit.SECONDS)) {
                throw new IllegalStateException(
                        "timed out waiting for the test latch"
                );
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(
                    "thread interrupted while waiting",
                    exception
            );
        }
    }
}
