package com.soen345.ticketreservation.reservation;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.appcompat.app.AlertDialog;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.Transaction;
import com.soen345.ticketreservation.auth.AuthManager;
import com.soen345.ticketreservation.email.EmailManager;
import com.soen345.ticketreservation.event.EventManager;
import com.soen345.ticketreservation.model.Event;
import com.soen345.ticketreservation.model.Reservation;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

public class ReservationManager {
    private static final String RESERVATION_COLLECTION = "reservations";
    private static final String EVENTS_COLLECTION = "events";
    private static ReservationManager instance;
    private final FirebaseFirestore db;
    private final AuthManager authManager;
    private final EventManager eventManager;
    private final EmailManager emailManager;

    private ReservationManager() {
        this(FirebaseFirestore.getInstance(), AuthManager.getInstance(), EventManager.getInstance(), EmailManager.getInstance());
    }

    @VisibleForTesting
    ReservationManager(FirebaseFirestore db, AuthManager authManager, EventManager eventManager, EmailManager emailManager) {
        this.db = db;
        this.authManager = authManager;
        this.eventManager = eventManager;
        this.emailManager = emailManager;
    }

    public static synchronized ReservationManager getInstance() {
        if (instance == null) {
            instance = new ReservationManager();
        }
        return instance;
    }

    public void createReservation(Reservation reservation, Event event, Context context) {
        if (!authManager.isLoggedIn()) {
            throw new IllegalStateException("User is not logged in");
        }

        db.runTransaction(transaction -> {
            DocumentReference eventRef = db.collection(EVENTS_COLLECTION).document(event.getEventId());
            Event latestEvent = transaction.get(eventRef).toObject(Event.class);

            if (latestEvent == null) {
                throw new FirebaseFirestoreException("Event not found", FirebaseFirestoreException.Code.NOT_FOUND);
            }

            if (latestEvent.getAvailableSeats() < reservation.getQuantity()) {
                throw new FirebaseFirestoreException("Not enough seats available", FirebaseFirestoreException.Code.ABORTED);
            }

            // Update event availability
            int newAvailableSeats = latestEvent.getAvailableSeats() - reservation.getQuantity();
            transaction.update(eventRef, "availableSeats", newAvailableSeats);

            // Create reservation
            DocumentReference reservationRef = db.collection(RESERVATION_COLLECTION).document();
            reservation.setReservationId(reservationRef.getId());
            reservation.setReservationDate(new Date());
            transaction.set(reservationRef, reservation);

            return null;
        }).addOnSuccessListener(aVoid -> {
            Log.d("Firestore", "Transaction success!");
            emailManager.sendConfirmation(authManager.getCurrentUser().getEmail(), event.getName(), reservation.getQuantity());
            new AlertDialog.Builder(context)
                    .setTitle("Reservation Successful")
                    .setMessage("Your reservation has been made.")
                    .setPositiveButton("OK", null)
                    .show();
        }).addOnFailureListener(e -> {
            Log.e("Firestore", "Transaction failure.", e);
            String message = (e.getMessage() != null && e.getMessage().contains("Not enough seats"))
                    ? "Sorry, there are not enough seats available for this event."
                    : "There was an error making your reservation.";

            new AlertDialog.Builder(context)
                    .setTitle("Reservation Failed")
                    .setMessage(message)
                    .setPositiveButton("OK", null)
                    .show();
        });
    }

    public ListenerRegistration listenToReservations(Consumer<List<Reservation>> callback) {
        if (!authManager.isLoggedIn()) {
            throw new IllegalStateException("User is not logged in");
        }

        FirebaseUser currentUser = authManager.getCurrentUser();
        if (currentUser == null) {
            throw new IllegalStateException("User is not logged in");
        }
        String userId = currentUser.getUid();

        return db.collection(RESERVATION_COLLECTION).whereEqualTo("userId", userId)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("Firestore", "Listen failed.", error);
                        return;
                    }

                    List<Reservation> reservations = new ArrayList<>();
                    if (value != null) {
                        for (QueryDocumentSnapshot document : value) {
                            Reservation reservation = document.toObject(Reservation.class);
                            reservations.add(reservation);
                        }
                    }
                    callback.accept(reservations);
                });
    }

    public void cancelReservation(Reservation reservation, Event event, Context context) {
        if (!authManager.isLoggedIn()) {
            throw new IllegalStateException("User is not logged in");
        }

        db.runTransaction(new Transaction.Function<Void>() {
            @Override
            public Void apply(Transaction transaction) throws FirebaseFirestoreException {
                DocumentReference reservationRef = db.collection(RESERVATION_COLLECTION).document(reservation.getReservationId());
                DocumentReference eventRef = db.collection(EVENTS_COLLECTION).document(event.getEventId());

                transaction.delete(reservationRef);

                event.setAvailableSeats(event.getAvailableSeats() + reservation.getQuantity());
                transaction.update(eventRef, "availableSeats", event.getAvailableSeats());

                return null;
            }
        }).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Transaction success!");
                        new AlertDialog.Builder(context)
                                .setTitle("Reservation Cancelled")
                                .setMessage("Your reservation has been cancelled.")
                                .setPositiveButton("OK", null)
                                .show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Transaction failure.", e);
                        new AlertDialog.Builder(context)
                                .setTitle("Reservation Cancellation Failed")
                                .setMessage("There was an error cancelling your reservation.")
                                .setPositiveButton("OK", null)
                                .show();
                    }
                });
    }
}
