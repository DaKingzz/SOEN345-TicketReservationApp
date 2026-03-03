package com.soen345.ticketreservation.event;

import android.util.Log;

import androidx.annotation.VisibleForTesting;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.soen345.ticketreservation.auth.AuthManager;
import com.soen345.ticketreservation.model.Event;

public class EventManager {
    private static final String EVENTS_COLLECTION = "events";

    private static EventManager instance;
    private final FirebaseFirestore db;
    private final AuthManager authManager;

    private EventManager() {
        this(FirebaseFirestore.getInstance(), AuthManager.getInstance());
    }

    @VisibleForTesting
    EventManager(FirebaseFirestore db, AuthManager authManager) {
        this.db = db;
        this.authManager = authManager;
    }

    public static synchronized EventManager getInstance() {
        if (instance == null) {
            instance = new EventManager();
        }
        return instance;
    }

    public void createEvent(Event event) {
        if (!authManager.isLoggedIn()) {
            throw new IllegalStateException("User is not logged in");
        }

        authManager.checkAdminStatus(isAdmin -> {
            if (isAdmin) {
                DocumentReference newDocRef = db.collection(EVENTS_COLLECTION).document();

                String generatedId = newDocRef.getId();
                event.setEventId(generatedId);

                newDocRef.set(event)
                        .addOnSuccessListener(aVoid -> {
                            Log.d("Firestore", "Event saved with internal ID: " + generatedId);
                        })
                        .addOnFailureListener(e -> {
                            Log.e("Firestore", "Error adding event", e);
                        });
            } else {
                Log.e("Security", "User is not an admin");
            }
        });
    }
}
