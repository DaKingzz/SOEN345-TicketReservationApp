package com.soen345.ticketreservation.event;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;

import androidx.annotation.VisibleForTesting;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.soen345.ticketreservation.auth.AuthManager;
import com.soen345.ticketreservation.model.Event;
import com.soen345.ticketreservation.model.Reservation;

import java.util.Date;

public class ReservationManager {
    private static final String RESERVATION_COLLECTION = "reservations";
    private static ReservationManager instance;
    private final FirebaseFirestore db;
    private final AuthManager authManager;
    private final EventManager eventManager;


    private ReservationManager() {
        this(FirebaseFirestore.getInstance(), AuthManager.getInstance(), EventManager.getInstance());
    }

    @VisibleForTesting
    ReservationManager(FirebaseFirestore db, AuthManager authManager, EventManager eventManager) {
        this.db = db;
        this.authManager = authManager;
        this.eventManager = eventManager;
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
        reservation.setReservationDate(new Date());
        DocumentReference newDocRef = db.collection(RESERVATION_COLLECTION).document();

        String generatedId = newDocRef.getId();
        reservation.setReservationId(generatedId);

        newDocRef.set(reservation)
                .addOnSuccessListener(aVoid -> {
                    // Handle success
                    Log.d("Firestore", "Reservation saved with internal ID: " + generatedId);
                    event.setAvailableSeats(event.getAvailableSeats() - reservation.getQuantity());
                    eventManager.updateEventAvailability(event, () -> {
                        // Handle success
                        Log.d("Firestore", "Event updated with internal ID: " + event.getEventId());
                        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
                        alertDialog.setTitle("Reservation Successful");
                        alertDialog.setMessage("Your reservation has been made.");
                        alertDialog.show();
                    }, e -> {
                        // Handle failure
                        Log.e("Firestore", "Error updating event", e);
                    });
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                    Log.e("Firestore", "Error adding reservation", e);
                });

    }
}
