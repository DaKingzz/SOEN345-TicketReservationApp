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
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
public class ReservationPerformanceTest {

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
    public void singleReservation_completesUnderThreshold() throws Exception {
        eventUnderTest = FirestorePerformanceTestHelper.seedSingleEvent(
                db,
                FirestorePerformanceTestHelper.uniquePrefix("single_reservation"),
                20
        );

        long start = System.nanoTime();

        boolean success = FirestorePerformanceTestHelper.reserveSeatsWithTransaction(
                db,
                user,
                eventUnderTest,
                2
        );

        long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);

        System.out.println("ReservationPerformanceTest - Single reservation took " + durationMs + " ms");

        assertTrue("Reservation should succeed", success);
        assertTrue(
                "Single reservation exceeded threshold: " + durationMs + " ms",
                durationMs <= PerformanceTestConfig.MAX_SINGLE_RESERVATION_MS
        );

        Event updatedEvent = FirestorePerformanceTestHelper.getEvent(db, eventUnderTest.getEventId());
        assertNotNull(updatedEvent);
        assertEquals(18, updatedEvent.getAvailableSeats());

        int reservationCount = FirestorePerformanceTestHelper.countReservationsForEvent(db, eventUnderTest.getEventId());
        assertEquals(1, reservationCount);
    }

    @Test
    public void repeatedReservations_reportsAverageTime() throws Exception {
        eventUnderTest = FirestorePerformanceTestHelper.seedSingleEvent(
                db,
                FirestorePerformanceTestHelper.uniquePrefix("repeated_reservation"),
                30
        );

        int attempts = 5;
        long totalMs = 0;

        for (int i = 0; i < attempts; i++) {
            long start = System.nanoTime();

            boolean success = FirestorePerformanceTestHelper.reserveSeatsWithTransaction(
                    db,
                    user,
                    eventUnderTest,
                    2
            );

            long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
            totalMs += durationMs;

            System.out.println("ReservationPerformanceTest - Attempt " + (i + 1) + " took " + durationMs + " ms");
            assertTrue("Reservation attempt " + (i + 1) + " should succeed", success);
        }

        long averageMs = totalMs / attempts;
        System.out.println("ReservationPerformanceTest - Average reservation time = " + averageMs + " ms");

        Event updatedEvent = FirestorePerformanceTestHelper.getEvent(db, eventUnderTest.getEventId());
        assertNotNull(updatedEvent);
        assertEquals(20, updatedEvent.getAvailableSeats());

        int reservationCount = FirestorePerformanceTestHelper.countReservationsForEvent(db, eventUnderTest.getEventId());
        assertEquals(5, reservationCount);
    }
}