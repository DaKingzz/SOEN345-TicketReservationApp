package com.soen345.ticketreservation.auth;

import android.app.Activity;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.soen345.ticketreservation.model.User;

import java.util.concurrent.TimeUnit;

public class AuthManager {

    private static final String USERS_COLLECTION = "users";

    private static AuthManager instance;

    private final FirebaseAuth      auth;
    private final FirebaseFirestore db;

    private AuthManager() {
        auth = FirebaseAuth.getInstance();
        db   = FirebaseFirestore.getInstance();
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

    public boolean isLoggedIn() {
        return auth.getCurrentUser() != null;
    }

    public void logout() {
        auth.signOut();
    }
    public void checkAdminStatus(OnAdminResult callback) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

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

    public void startPhoneVerification(String phoneNumber, Activity activity,
                                        PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks) {
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(activity)
                .setCallbacks(callbacks)
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    public void verifyOtpAndRegister(String verificationId, String otp,
                                      String displayName, String phoneNumber,
                                      AuthCallback callback) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otp);
        signInWithPhoneCredential(credential, displayName, phoneNumber, callback);
    }

    public void signInWithPhoneCredential(PhoneAuthCredential credential,
                                           String displayName, String phoneNumber,
                                           AuthCallback callback) {
        auth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        callback.onFailure(extractMessage(task.getException(), "OTP verification failed"));
                        return;
                    }

                    FirebaseUser firebaseUser = auth.getCurrentUser();
                    if (firebaseUser == null) {
                        callback.onFailure("Authentication succeeded but user is null");
                        return;
                    }

                    firebaseUser.updateProfile(new UserProfileChangeRequest.Builder()
                            .setDisplayName(displayName)
                            .build());

                    String uid = firebaseUser.getUid();
                    checkUserExists(uid, exists -> {
                        if (exists) {
                            callback.onSuccess();
                        } else {
                            User user = new User(uid, null, phoneNumber,
                                                 displayName, User.ROLE_CUSTOMER);
                            saveUserToFirestore(user, callback);
                        }
                    });
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

    private void checkUserExists(String uid, ExistsCallback callback) {
        db.collection(USERS_COLLECTION)
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> callback.onResult(doc.exists()))
                .addOnFailureListener(e -> callback.onResult(false));
    }

    private static String extractMessage(Exception e, String fallback) {
        return (e != null && e.getMessage() != null) ? e.getMessage() : fallback;
    }

    public interface UserCallback {
        void onUserLoaded(User user);
    }

    private interface ExistsCallback {
        void onResult(boolean exists);
    }
}
