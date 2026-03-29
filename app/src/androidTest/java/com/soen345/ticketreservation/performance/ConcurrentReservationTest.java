package com.soen345.ticketreservation.performance;

import static org.junit.Assert.assertEquals;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.soen345.ticketreservation.model.Event;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@RunWith(AndroidJUnit4.class)
public class ConcurrentReservationTest {
    private FirebaseFirestore db;
    private FirebaseUser user;
    private String testPrefix;
    private Event eventUnderTest;

    @Before
    public void setUp() throws Exception {
        db = FirebaseFirestore.getInstance();
        user = FirestorePerformanceTestHelper.loginTestUser();
        testPrefix = FirestorePerformanceTestHelper.uniquePrefix("concurrent");
    }

    @After
    public void tearDown() throws Exception {
        FirestorePerformanceTestHelper.cleanupTestEnvironment(db, testPrefix);
    }

    @Test
    public void concurrentReservations_doNotOversellSeats() throws Exception {
        int initialSeats = 10;
        int attempts = 15;
        eventUnderTest = FirestorePerformanceTestHelper.seedSingleEvent(db, testPrefix + "_event", initialSeats);

        CountDownLatch latch = new CountDownLatch(attempts);
        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < attempts; i++) {
            new Thread(() -> {
                try {
                    if (FirestorePerformanceTestHelper.reserveSeatsWithTransaction(db, user, eventUnderTest, 1)) {
                        successCount.incrementAndGet();
                    }
                } catch (Exception ignored) {}
                finally { latch.countDown(); }
            }).start();
        }

        latch.await(PerformanceTestConfig.FIRESTORE_TIMEOUT_SECONDS, TimeUnit.SECONDS);

        Event updatedEvent = FirestorePerformanceTestHelper.getEvent(db, eventUnderTest.getEventId());
        assertEquals("Should not exceed initial seats", initialSeats, successCount.get());
        assertEquals(0, updatedEvent.getAvailableSeats());
    }
}