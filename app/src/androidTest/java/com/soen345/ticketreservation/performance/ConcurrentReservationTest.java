package com.soen345.ticketreservation.performance;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.soen345.ticketreservation.model.Event;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@RunWith(AndroidJUnit4.class)
public class ConcurrentReservationTest {

    private FirebaseFirestore db;
    private FirebaseUser user;
    private Event eventUnderTest;

    @Before
    public void setUp() throws Exception {
        db = FirebaseFirestore.getInstance();
        user = FirestorePerformanceTestHelper.loginTestUser();
        assertNotNull(user);
    }

    @After
    public void tearDown() throws Exception {
        if (eventUnderTest != null) {
            FirestorePerformanceTestHelper.deleteReservationsForEvent(db, eventUnderTest.getEventId());
            FirestorePerformanceTestHelper.deleteEvents(db, Collections.singletonList(eventUnderTest.getEventId()));
        }
    }

    @Test
    public void concurrentReservations_doNotOversellSeats() throws Exception {
        int initialSeats = 10;
        int attempts = 15;
        int quantityPerReservation = 1;

        eventUnderTest = FirestorePerformanceTestHelper.seedSingleEvent(
                db,
                FirestorePerformanceTestHelper.uniquePrefix("concurrent_reservation"),
                initialSeats
        );

        CountDownLatch latch = new CountDownLatch(attempts);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        long start = System.nanoTime();

        for (int i = 0; i < attempts; i++) {
            new Thread(() -> {
                try {
                    boolean success = FirestorePerformanceTestHelper.reserveSeatsWithTransaction(
                            db,
                            user,
                            eventUnderTest,
                            quantityPerReservation
                    );

                    if (success) {
                        successCount.incrementAndGet();
                    } else {
                        failureCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                    System.out.println("ConcurrentReservationTest - Thread failed: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            }).start();
        }

        boolean completed = latch.await(
                PerformanceTestConfig.FIRESTORE_TIMEOUT_SECONDS,
                TimeUnit.SECONDS
        );

        long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);

        assertTrue("Concurrent reservation test did not finish in time", completed);

        Event updatedEvent = FirestorePerformanceTestHelper.getEvent(db, eventUnderTest.getEventId());
        assertNotNull(updatedEvent);

        int reservationCount = FirestorePerformanceTestHelper.countReservationsForEvent(db, eventUnderTest.getEventId());

        System.out.println("ConcurrentReservationTest - successCount = " + successCount.get());
        System.out.println("ConcurrentReservationTest - failureCount = " + failureCount.get());
        System.out.println("ConcurrentReservationTest - final available seats = " + updatedEvent.getAvailableSeats());
        System.out.println("ConcurrentReservationTest - reservation count = " + reservationCount);
        System.out.println("ConcurrentReservationTest - total duration = " + durationMs + " ms");

        assertEquals("At most 10 reservations should succeed", initialSeats, successCount.get());
        assertEquals("The remaining attempts should fail", attempts - initialSeats, failureCount.get());
        assertEquals("Available seats should never go below zero", 0, updatedEvent.getAvailableSeats());
        assertEquals("Stored reservations should match successful bookings", initialSeats, reservationCount);
        assertTrue(
                "Concurrent test took too long: " + durationMs + " ms",
                durationMs <= PerformanceTestConfig.MAX_CONCURRENT_TEST_TOTAL_MS
        );
    }
}