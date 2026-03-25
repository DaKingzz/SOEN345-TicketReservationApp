package com.soen345.ticketreservation.performance;

import static org.junit.Assert.assertTrue;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.soen345.ticketreservation.model.Event;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
public class EventLoadPerformanceTest {

    private FirebaseFirestore db;
    private final List<String> createdEventIds = new ArrayList<>();
    private String testPrefix;

    @Before
    public void setUp() throws Exception {
        db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirestorePerformanceTestHelper.loginTestUser();
        assertTrue(user != null);
        testPrefix = FirestorePerformanceTestHelper.uniquePrefix("event_load");
    }

    @After
    public void tearDown() throws Exception {
        FirestorePerformanceTestHelper.deleteEvents(db, createdEventIds);
    }

    @Test
    public void load100Events_underReasonableTime() throws Exception {
        createdEventIds.addAll(
                FirestorePerformanceTestHelper.seedEvents(
                        db,
                        PerformanceTestConfig.MEDIUM_EVENT_DATASET,
                        testPrefix
                )
        );

        long start = System.nanoTime();

        List<Event> matchingEvents = Tasks.await(
                db.collection(FirestorePerformanceTestHelper.EVENTS_COLLECTION)
                        .whereGreaterThanOrEqualTo("name", testPrefix)
                        .whereLessThan("name", testPrefix + "\uf8ff")
                        .get(),
                PerformanceTestConfig.FIRESTORE_TIMEOUT_SECONDS,
                TimeUnit.SECONDS
        ).toObjects(Event.class);

        long end = System.nanoTime();
        long durationMs = TimeUnit.NANOSECONDS.toMillis(end - start);

        System.out.println("EventLoadPerformanceTest - Loaded " + matchingEvents.size()
                + " events in " + durationMs + " ms");

        assertTrue("Expected at least 100 test events to load", matchingEvents.size() >= 100);
        assertTrue(
                "Event loading took too long: " + durationMs + " ms",
                durationMs <= PerformanceTestConfig.MAX_EVENT_LOAD_MS_100
        );
    }

    @Test
    public void compareSmallVsLargeDataset() throws Exception {
        String smallPrefix = FirestorePerformanceTestHelper.uniquePrefix("small");
        String largePrefix = FirestorePerformanceTestHelper.uniquePrefix("large");

        createdEventIds.addAll(
                FirestorePerformanceTestHelper.seedEvents(
                        db,
                        PerformanceTestConfig.SMALL_EVENT_DATASET,
                        smallPrefix
                )
        );

        createdEventIds.addAll(
                FirestorePerformanceTestHelper.seedEvents(
                        db,
                        PerformanceTestConfig.LARGE_EVENT_DATASET,
                        largePrefix
                )
        );

        long smallStart = System.nanoTime();
        int smallCount = Tasks.await(
                db.collection(FirestorePerformanceTestHelper.EVENTS_COLLECTION)
                        .whereGreaterThanOrEqualTo("name", smallPrefix)
                        .whereLessThan("name", smallPrefix + "\uf8ff")
                        .get(),
                PerformanceTestConfig.FIRESTORE_TIMEOUT_SECONDS,
                TimeUnit.SECONDS
        ).size();
        long smallMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - smallStart);

        long largeStart = System.nanoTime();
        int largeCount = Tasks.await(
                db.collection(FirestorePerformanceTestHelper.EVENTS_COLLECTION)
                        .whereGreaterThanOrEqualTo("name", largePrefix)
                        .whereLessThan("name", largePrefix + "\uf8ff")
                        .get(),
                PerformanceTestConfig.FIRESTORE_TIMEOUT_SECONDS,
                TimeUnit.SECONDS
        ).size();
        long largeMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - largeStart);

        System.out.println("EventLoadPerformanceTest - small dataset: " + smallCount + " events in " + smallMs + " ms");
        System.out.println("EventLoadPerformanceTest - large dataset: " + largeCount + " events in " + largeMs + " ms");

        assertTrue("Expected small dataset to contain at least 10 events", smallCount >= 10);
        assertTrue("Expected large dataset to contain at least 500 events", largeCount >= 500);
    }
}