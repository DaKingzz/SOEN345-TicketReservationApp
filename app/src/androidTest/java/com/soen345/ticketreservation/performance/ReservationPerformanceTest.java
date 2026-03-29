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
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
public class ReservationPerformanceTest {
    private FirebaseFirestore db;
    private FirebaseUser user;
    private Event eventUnderTest;
    private String eventNamePrefix;

    @Before
    public void setUp() throws Exception {
        db = FirebaseFirestore.getInstance();
        user = FirestorePerformanceTestHelper.loginTestUser();
        eventNamePrefix = FirestorePerformanceTestHelper.uniquePrefix("reservation");
    }

    @After
    public void tearDown() throws Exception {
        FirestorePerformanceTestHelper.cleanupTestEnvironment(db, eventNamePrefix);
    }

    @Test
    public void singleReservation_completesUnderThreshold() throws Exception {
        eventUnderTest = FirestorePerformanceTestHelper.seedSingleEvent(db, eventNamePrefix + "_single", 20);
        long start = System.nanoTime();
        boolean success = FirestorePerformanceTestHelper.reserveSeatsWithTransaction(db, user, eventUnderTest, 2);
        long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);

        assertTrue("Reservation should succeed", success);
        assertTrue(durationMs <= PerformanceTestConfig.MAX_SINGLE_RESERVATION_MS);
    }

    @Test
    public void repeatedReservations_reportsAverageTime() throws Exception {
        eventUnderTest = FirestorePerformanceTestHelper.seedSingleEvent(db, eventNamePrefix + "_repeated", 30);
        int attempts = 5;
        long totalMs = 0;

        for (int i = 0; i < attempts; i++) {
            long start = System.nanoTime();
            boolean success = FirestorePerformanceTestHelper.reserveSeatsWithTransaction(db, user, eventUnderTest, 2);
            totalMs += TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
            assertTrue(success);
        }
        System.out.println("Average time: " + (totalMs / attempts) + " ms");
    }
}