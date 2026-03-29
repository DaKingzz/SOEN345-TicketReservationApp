package com.soen345.ticketreservation.auth;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.soen345.ticketreservation.model.User;

public class AuthManager {

    private static final String USERS_COLLECTION = "users";

    private static AuthManager instance;

    private final FirebaseAuth      auth;
    private final FirebaseFirestore db;

    private boolean isAdmin;

    private AuthManager() {
        auth = FirebaseAuth.getInstance();
        db   = FirebaseFirestore.getInstance();
        isAdmin=false;
    }

    @androidx.annotation.VisibleForTesting
    public AuthManager(FirebaseAuth auth, FirebaseFirestore db) {
        this.auth = auth;
        this.db = db;
        this.isAdmin = false;
    }

    public static synchronized AuthManager getInstance() {
        if (instance == null) {
            instance = new AuthManager();
        }
        return instance;
    }

    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    public FirebaseFirestore getDb(){return db;}

    public boolean isLoggedIn() {
        return auth.getCurrentUser() != null;
    }

    public void logout() {
        auth.signOut();
    }
    public void checkAdminStatus(OnAdminResult callback) {
        FirebaseUser firebaseUser = auth.getCurrentUser();

        if (firebaseUser == null) {
            callback.onResult(false);
            return;
        }

        db.collection("users").document(firebaseUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String role = documentSnapshot.getString("role");

                        boolean isAdmin = "admin".equalsIgnoreCase(role);
                        callback.onResult(isAdmin);
                    } else {
                        Log.e("Security", "User document does not exist in Firestore");
                        callback.onResult(false);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Security", "Failed to fetch user role", e);
                    callback.onResult(false);
                });
    }

    public interface OnAdminResult {
        void onResult(boolean isAdmin);
    }


    public void loginWithEmail(String email, String password, AuthCallback callback) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess();
                    } else {
                        callback.onFailure(extractMessage(task.getException(), "Login failed"));
                    }
                });
    }

    public void registerWithEmail(String email, String password, String displayName,
                                  AuthCallback callback) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        callback.onFailure(extractMessage(task.getException(), "Registration failed"));
                        return;
                    }

                    FirebaseUser firebaseUser = auth.getCurrentUser();
                    if (firebaseUser == null) {
                        callback.onFailure("Registration failed: could not retrieve user");
                        return;
                    }

                    firebaseUser.updateProfile(new UserProfileChangeRequest.Builder()
                            .setDisplayName(displayName)
                            .build());

                    User user = new User(firebaseUser.getUid(), email, null,
                            displayName, User.ROLE_CUSTOMER);
                    saveUserToFirestore(user, callback);
                });
    }

    public void sendPasswordResetEmail(String email, AuthCallback callback) {
        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess();
                    } else {
                        callback.onFailure(extractMessage(task.getException(), "Could not send reset email"));
                    }
                });
    }

    public void saveUserToFirestore(User user, AuthCallback callback) {
        db.collection(USERS_COLLECTION)
                .document(user.getUid())
                .set(user)
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void getUserFromFirestore(String uid, UserCallback callback) {
        db.collection(USERS_COLLECTION)
                .document(uid)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        callback.onUserLoaded(snapshot.toObject(User.class));
                    } else {
                        callback.onUserLoaded(null);
                    }
                })
                .addOnFailureListener(e -> callback.onUserLoaded(null));
    }

    private static String extractMessage(Exception e, String fallback) {
        return (e != null && e.getMessage() != null) ? e.getMessage() : fallback;
    }

    public interface UserCallback {
        void onUserLoaded(User user);
    }
}