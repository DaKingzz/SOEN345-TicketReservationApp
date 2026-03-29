package com.soen345.ticketreservation.performance;

import androidx.annotation.NonNull;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.soen345.ticketreservation.model.Event;
import com.soen345.ticketreservation.model.Reservation;
import org.junit.Assert;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public final class FirestorePerformanceTestHelper {

    public static final String EVENTS_COLLECTION = "events";
    public static final String RESERVATIONS_COLLECTION = "reservations";
    public static final String MAIL_COLLECTION = "mail";

    private FirestorePerformanceTestHelper() {}

    /**
     * Aggressive Cleanup: Deletes events and reservations by prefix.
     * Wrapped in try-catch to prevent cleanup issues from failing the test results.
     */
    public static void cleanupTestEnvironment(FirebaseFirestore db, String prefix) {
        try {
            List<DocumentSnapshot> eventDocs = Tasks.await(
                    db.collection(EVENTS_COLLECTION)
                            .whereGreaterThanOrEqualTo("name", prefix)
                            .whereLessThan("name", prefix + "\uf8ff")
                            .get(),
                    PerformanceTestConfig.FIRESTORE_TIMEOUT_SECONDS,
                    TimeUnit.SECONDS
            ).getDocuments();

            for (DocumentSnapshot eventDoc : eventDocs) {
                deleteReservationsForEvent(db, eventDoc.getId());
                Tasks.await(eventDoc.getReference().delete());
            }
        } catch (Exception e) {
            System.err.println("Cleanup Warning: " + e.getMessage());
        }
    }

    /**
     * Prevents FirebaseTooManyRequestsException by reusing the session.
     */
    public static FirebaseUser loginTestUser() throws ExecutionException, InterruptedException, TimeoutException {
        FirebaseAuth auth = FirebaseAuth.getInstance();

        // REUSE existing session if available
        if (auth.getCurrentUser() != null) {
            return auth.getCurrentUser();
        }

        // Only sign in if absolutely necessary
        Tasks.await(
                auth.signInWithEmailAndPassword(
                        PerformanceTestConfig.TEST_EMAIL,
                        PerformanceTestConfig.TEST_PASSWORD
                ),
                PerformanceTestConfig.AUTH_TIMEOUT_SECONDS,
                TimeUnit.SECONDS
        );

        FirebaseUser user = auth.getCurrentUser();
        Assert.assertNotNull("Login failed. Check credentials/network.", user);
        return user;
    }

    public static String uniquePrefix(String base) {
        return "perf_" + base + "_" + System.currentTimeMillis();
    }

    public static List<String> seedEvents(FirebaseFirestore db, int count, String prefix)
            throws ExecutionException, InterruptedException, TimeoutException {
        List<String> eventIds = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Event event = seedSingleEvent(db, prefix + "_event_" + i, 100);
            eventIds.add(event.getEventId());
        }
        return eventIds;
    }

    public static Event seedSingleEvent(FirebaseFirestore db, String name, int totalCapacity)
            throws ExecutionException, InterruptedException, TimeoutException {
        DocumentReference doc = db.collection(EVENTS_COLLECTION).document();
        Event event = new Event(doc.getId(), name, "Concert", new Date(), "Montreal", totalCapacity);
        event.setAvailableSeats(totalCapacity);
        Tasks.await(doc.set(event), PerformanceTestConfig.FIRESTORE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        return event;
    }

    public static boolean reserveSeatsWithTransaction(FirebaseFirestore db, FirebaseUser user, Event event, int quantity)
            throws ExecutionException, InterruptedException, TimeoutException {
        return Boolean.TRUE.equals(Tasks.await(db.runTransaction(transaction -> {
            DocumentReference eventRef = db.collection(EVENTS_COLLECTION).document(event.getEventId());
            Event latestEvent = transaction.get(eventRef).toObject(Event.class);
            if (latestEvent == null || latestEvent.getAvailableSeats() < quantity) return false;

            transaction.update(eventRef, "availableSeats", latestEvent.getAvailableSeats() - quantity);
            DocumentReference resRef = db.collection(RESERVATIONS_COLLECTION).document();
            Reservation res = new Reservation(resRef.getId(), event.getEventId(), user.getUid(), quantity, new Date(), event.getDateTime());
            transaction.set(resRef, res);
            return true;
        }), PerformanceTestConfig.FIRESTORE_TIMEOUT_SECONDS, TimeUnit.SECONDS));
    }

    public static void deleteReservationsForEvent(FirebaseFirestore db, String eventId)
            throws ExecutionException, InterruptedException, TimeoutException {
        List<DocumentSnapshot> docs = Tasks.await(db.collection(RESERVATIONS_COLLECTION).whereEqualTo("eventId", eventId).get()).getDocuments();
        for (DocumentSnapshot doc : docs) Tasks.await(doc.getReference().delete());
    }

    public static Event getEvent(FirebaseFirestore db, String eventId) throws Exception {
        return Tasks.await(db.collection(EVENTS_COLLECTION).document(eventId).get()).toObject(Event.class);
    }

    public static int countReservationsForEvent(FirebaseFirestore db, String eventId) throws Exception {
        return Tasks.await(db.collection(RESERVATIONS_COLLECTION).whereEqualTo("eventId", eventId).get()).size();
    }
}