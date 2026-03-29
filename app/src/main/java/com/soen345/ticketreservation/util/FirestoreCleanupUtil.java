package com.soen345.ticketreservation.util;

import android.util.Log;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public final class FirestoreCleanupUtil {

    private FirestoreCleanupUtil() {}

    public static void deletePerfEvents() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("events")
                .whereGreaterThanOrEqualTo("name", "perf_")
                .whereLessThan("name", "perf_\uf8ff")
                .get()
                .addOnSuccessListener(querySnapshot -> deleteDocuments(querySnapshot, "events"))
                .addOnFailureListener(e ->
                        Log.e("FirestoreCleanupUtil", "Failed to fetch perf events", e));
    }

    private static void deleteDocuments(QuerySnapshot querySnapshot, String collectionName) {
        if (querySnapshot.isEmpty()) {
            Log.d("FirestoreCleanupUtil", "No matching documents found in " + collectionName);
            return;
        }

        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
            doc.getReference()
                    .delete()
                    .addOnSuccessListener(unused ->
                            Log.d("FirestoreCleanupUtil", "Deleted doc: " + doc.getId()))
                    .addOnFailureListener(e ->
                            Log.e("FirestoreCleanupUtil", "Failed to delete doc: " + doc.getId(), e));
        }
    }
}