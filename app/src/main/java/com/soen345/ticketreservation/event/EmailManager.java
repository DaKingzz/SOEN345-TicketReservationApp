package com.soen345.ticketreservation.event;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EmailManager {
    private static final String EMAIL_COLLECTION = "mail";

    private static EmailManager instance;

    private final FirebaseFirestore db;

    private EmailManager() {
        this(FirebaseFirestore.getInstance());
    }

    public EmailManager(FirebaseFirestore db) {
        this.db = db;
    }

    public static synchronized EmailManager getInstance() {
        if (instance == null) {
            instance = new EmailManager();
        }
        return instance;
    }

    public void sendConfirmation(String emailAddress, String eventName, int quantity) {
        Map<String, Object> email = new HashMap<>();
        email.put("to", emailAddress);

        Map<String, String> message = new HashMap<>();
        message.put("subject", "Reservation Confirmation");
        message.put("text", "Your reservation to " + eventName + " for " + quantity + " tickets" + " has been confirmed.");

        email.put("message", message);

        DocumentReference newDocRef = db.collection(EMAIL_COLLECTION).document();
        String generatedId = newDocRef.getId();
        email.put("id", generatedId);

        newDocRef.set(email)
                .addOnSuccessListener(aVoid -> {
                    System.out.println("Email saved with internal ID: " + generatedId);
                })
                .addOnFailureListener(e -> {
                    System.err.println("Error adding email: " + e.getMessage());
                });
    }
}
