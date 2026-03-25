package com.soen345.ticketreservation.performance;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.soen345.ticketreservation.model.Event;
import com.soen345.ticketreservation.model.Reservation;

import org.junit.Assert;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public final class FirestorePerformanceTestHelper {

    public static final String EVENTS_COLLECTION = "events";
    public static final String RESERVATIONS_COLLECTION = "reservations";
    public static final String MAIL_COLLECTION = "mail";

    private FirestorePerformanceTestHelper() {}

    public static FirebaseUser loginTestUser() throws ExecutionException, InterruptedException, TimeoutException {
        FirebaseAuth auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() != null) {
            return auth.getCurrentUser();
        }

        Tasks.await(
                auth.signInWithEmailAndPassword(
                        PerformanceTestConfig.TEST_EMAIL,
                        PerformanceTestConfig.TEST_PASSWORD
                ),
                PerformanceTestConfig.AUTH_TIMEOUT_SECONDS,
                TimeUnit.SECONDS
        );

        FirebaseUser user = auth.getCurrentUser();
        Assert.assertNotNull("Firebase test user login failed. Check test credentials.", user);
        return user;
    }

    public static String uniquePrefix(String base) {
        return "perf_" + base + "_" + System.currentTimeMillis() + "_" + UUID.randomUUID();
    }

    public static List<String> seedEvents(FirebaseFirestore db, int count, String prefix)
            throws ExecutionException, InterruptedException, TimeoutException {

        List<String> eventIds = new ArrayList<>();
        CollectionReference eventsRef = db.collection(EVENTS_COLLECTION);

        for (int i = 0; i < count; i++) {
            DocumentReference doc = eventsRef.document();
            String eventId = doc.getId();

            Event event = new Event(
                    eventId,
                    prefix + "_event_" + i,
                    "Concert",
                    new Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(10 + i)),
                    "Montreal",
                    100
            );
            event.setAvailableSeats(100);

            Tasks.await(
                    doc.set(event),
                    PerformanceTestConfig.FIRESTORE_TIMEOUT_SECONDS,
                    TimeUnit.SECONDS
            );

            eventIds.add(eventId);
        }

        return eventIds;
    }

    public static Event seedSingleEvent(FirebaseFirestore db, String name, int totalCapacity)
            throws ExecutionException, InterruptedException, TimeoutException {

        DocumentReference doc = db.collection(EVENTS_COLLECTION).document();
        String eventId = doc.getId();

        Event event = new Event(
                eventId,
                name,
                "Concert",
                new Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(7)),
                "Montreal",
                totalCapacity
        );
        event.setAvailableSeats(totalCapacity);

        Tasks.await(
                doc.set(event),
                PerformanceTestConfig.FIRESTORE_TIMEOUT_SECONDS,
                TimeUnit.SECONDS
        );

        return event;
    }

    public static Reservation createReservationObject(@NonNull FirebaseUser user, @NonNull Event event, int quantity) {
        Reservation reservation = new Reservation();
        reservation.setUserId(user.getUid());
        reservation.setEventId(event.getEventId());
        reservation.setQuantity(quantity);
        reservation.setReservationDate(new Date());
        reservation.setEventDate(event.getDateTime());
        return reservation;
    }

    public static boolean reserveSeatsWithTransaction(
            FirebaseFirestore db,
            FirebaseUser user,
            Event event,
            int quantity
    ) throws ExecutionException, InterruptedException, TimeoutException {

        Boolean result = Tasks.await(
                db.runTransaction(transaction -> {
                    DocumentReference eventRef = db.collection(EVENTS_COLLECTION).document(event.getEventId());
                    Event latestEvent = transaction.get(eventRef).toObject(Event.class);

                    if (latestEvent == null) {
                        return false;
                    }

                    if (latestEvent.getAvailableSeats() < quantity) {
                        return false;
                    }

                    int newAvailableSeats = latestEvent.getAvailableSeats() - quantity;
                    transaction.update(eventRef, "availableSeats", newAvailableSeats);

                    DocumentReference reservationRef = db.collection(RESERVATIONS_COLLECTION).document();
                    Reservation reservation = new Reservation(
                            reservationRef.getId(),
                            event.getEventId(),
                            user.getUid(),
                            quantity,
                            new Date(),
                            event.getDateTime()
                    );
                    transaction.set(reservationRef, reservation);

                    return true;
                }),
                PerformanceTestConfig.FIRESTORE_TIMEOUT_SECONDS,
                TimeUnit.SECONDS
        );

        return Boolean.TRUE.equals(result);
    }

    public static int countReservationsForEvent(FirebaseFirestore db, String eventId)
            throws ExecutionException, InterruptedException, TimeoutException {

        return Tasks.await(
                db.collection(RESERVATIONS_COLLECTION)
                        .whereEqualTo("eventId", eventId)
                        .get(),
                PerformanceTestConfig.FIRESTORE_TIMEOUT_SECONDS,
                TimeUnit.SECONDS
        ).size();
    }

    public static Event getEvent(FirebaseFirestore db, String eventId)
            throws ExecutionException, InterruptedException, TimeoutException {

        return Tasks.await(
                db.collection(EVENTS_COLLECTION).document(eventId).get(),
                PerformanceTestConfig.FIRESTORE_TIMEOUT_SECONDS,
                TimeUnit.SECONDS
        ).toObject(Event.class);
    }

    public static void deleteEvents(FirebaseFirestore db, List<String> eventIds)
            throws ExecutionException, InterruptedException, TimeoutException {

        if (eventIds == null) return;

        for (String eventId : eventIds) {
            Tasks.await(
                    db.collection(EVENTS_COLLECTION).document(eventId).delete(),
                    PerformanceTestConfig.FIRESTORE_TIMEOUT_SECONDS,
                    TimeUnit.SECONDS
            );
        }
    }

    public static void deleteReservationsForEvent(FirebaseFirestore db, String eventId)
            throws ExecutionException, InterruptedException, TimeoutException {

        List<DocumentSnapshot> docs = Tasks.await(
                db.collection(RESERVATIONS_COLLECTION)
                        .whereEqualTo("eventId", eventId)
                        .get(),
                PerformanceTestConfig.FIRESTORE_TIMEOUT_SECONDS,
                TimeUnit.SECONDS
        ).getDocuments();

        for (DocumentSnapshot doc : docs) {
            Tasks.await(
                    doc.getReference().delete(),
                    PerformanceTestConfig.FIRESTORE_TIMEOUT_SECONDS,
                    TimeUnit.SECONDS
            );
        }
    }

    public static void deleteMailDocsBySubject(FirebaseFirestore db, String subject)
            throws ExecutionException, InterruptedException, TimeoutException {

        List<DocumentSnapshot> docs = Tasks.await(
                db.collection(MAIL_COLLECTION)
                        .whereEqualTo("message.subject", subject)
                        .get(),
                PerformanceTestConfig.FIRESTORE_TIMEOUT_SECONDS,
                TimeUnit.SECONDS
        ).getDocuments();

        for (DocumentSnapshot doc : docs) {
            Tasks.await(
                    doc.getReference().delete(),
                    PerformanceTestConfig.FIRESTORE_TIMEOUT_SECONDS,
                    TimeUnit.SECONDS
            );
        }
    }
}