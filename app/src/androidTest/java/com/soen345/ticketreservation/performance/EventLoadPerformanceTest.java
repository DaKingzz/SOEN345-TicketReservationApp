package com.soen345.ticketreservation.performance;

import static org.junit.Assert.assertTrue;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.FirebaseFirestore;
import com.soen345.ticketreservation.model.Event;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public class EventLoadPerformanceTest {
    private FirebaseFirestore db;
    private String testPrefix;

    @Before
    public void setUp() throws Exception {
        db = FirebaseFirestore.getInstance();
        FirestorePerformanceTestHelper.loginTestUser();
        testPrefix = FirestorePerformanceTestHelper.uniquePrefix("load");
    }

    @After
    public void tearDown() throws Exception {
        FirestorePerformanceTestHelper.cleanupTestEnvironment(db, testPrefix);
    }

    @Test
    public void load100Events_underReasonableTime() throws Exception {
        FirestorePerformanceTestHelper.seedEvents(db, PerformanceTestConfig.MEDIUM_EVENT_DATASET, testPrefix);
        long start = System.currentTimeMillis();
        List<Event> events = Tasks.await(db.collection("events")
                .whereGreaterThanOrEqualTo("name", testPrefix)
                .whereLessThan("name", testPrefix + "\uf8ff").get()).toObjects(Event.class);
        long duration = System.currentTimeMillis() - start;

        assertTrue(events.size() >= 100);
        assertTrue("Load time exceeded limit", duration <= PerformanceTestConfig.MAX_EVENT_LOAD_MS_100);
    }
}