package com.soen345.ticketreservation.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.soen345.ticketreservation.model.User;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@ExtendWith(MockitoExtension.class)
class AuthManagerTest {

    // ── Mocks ─────────────────────────────────────────────────────────────────

    @Mock private FirebaseAuth            mockAuth;
    @Mock private FirebaseFirestore       mockDb;
    @Mock private FirebaseUser            mockUser;
    @Mock private CollectionReference     mockCollection;
    @Mock private DocumentReference       mockDocRef;
    @Mock private DocumentSnapshot        mockDocSnapshot;
    @Mock private Task<AuthResult>        authTask;
    @Mock private Task<Void>              voidTask;
    @Mock private Task<DocumentSnapshot>  docTask;

    private AuthManager authManager;
    private MockedStatic<FirebaseAuth>      staticAuth;
    private MockedStatic<FirebaseFirestore> staticFirestore;

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    @BeforeEach
    void setUp() throws Exception {
        resetSingleton();
        staticAuth      = mockStatic(FirebaseAuth.class);
        staticFirestore = mockStatic(FirebaseFirestore.class);
        staticAuth.when(FirebaseAuth::getInstance).thenReturn(mockAuth);
        staticFirestore.when(FirebaseFirestore::getInstance).thenReturn(mockDb);
        authManager = AuthManager.getInstance();
    }

    @AfterEach
    void tearDown() throws Exception {
        staticAuth.close();
        staticFirestore.close();
        resetSingleton();
    }

    private void resetSingleton() throws Exception {
        Field field = AuthManager.class.getDeclaredField("instance");
        field.setAccessible(true);
        field.set(null, null);
    }

    // ── Stub helpers ──────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private void completeAuthSuccess() {
        when(authTask.addOnCompleteListener(any())).thenAnswer(inv -> {
            Task<AuthResult> t = mock(Task.class);
            when(t.isSuccessful()).thenReturn(true);
            ((OnCompleteListener<AuthResult>) inv.getArgument(0)).onComplete(t);
            return authTask;
        });
    }

    @SuppressWarnings("unchecked")
    private void completeAuthFailure(String message) {
        when(authTask.addOnCompleteListener(any())).thenAnswer(inv -> {
            Task<AuthResult> t = mock(Task.class);
            when(t.isSuccessful()).thenReturn(false);
            when(t.getException()).thenReturn(new Exception(message));
            ((OnCompleteListener<AuthResult>) inv.getArgument(0)).onComplete(t);
            return authTask;
        });
    }

    @SuppressWarnings("unchecked")
    private void completeVoidSuccess() {
        when(voidTask.addOnCompleteListener(any())).thenAnswer(inv -> {
            Task<Void> t = mock(Task.class);
            when(t.isSuccessful()).thenReturn(true);
            ((OnCompleteListener<Void>) inv.getArgument(0)).onComplete(t);
            return voidTask;
        });
    }

    @SuppressWarnings("unchecked")
    private void completeVoidFailure(String message) {
        when(voidTask.addOnCompleteListener(any())).thenAnswer(inv -> {
            Task<Void> t = mock(Task.class);
            when(t.isSuccessful()).thenReturn(false);
            when(t.getException()).thenReturn(new Exception(message));
            ((OnCompleteListener<Void>) inv.getArgument(0)).onComplete(t);
            return voidTask;
        });
    }

    @SuppressWarnings("unchecked")
    private void firestoreSetSuccess() {
        when(voidTask.addOnSuccessListener(any())).thenAnswer(inv -> {
            ((OnSuccessListener<Void>) inv.getArgument(0)).onSuccess(null);
            return voidTask;
        });
        when(voidTask.addOnFailureListener(any())).thenReturn(voidTask);
    }

    @SuppressWarnings("unchecked")
    private void firestoreSetFailure(String message) {
        when(voidTask.addOnSuccessListener(any())).thenReturn(voidTask);
        when(voidTask.addOnFailureListener(any())).thenAnswer(inv -> {
            ((OnFailureListener) inv.getArgument(0)).onFailure(new Exception(message));
            return voidTask;
        });
    }

    private void stubFirestoreGet(String uid) {
        when(mockDb.collection("users")).thenReturn(mockCollection);
        when(mockCollection.document(uid)).thenReturn(mockDocRef);
        when(mockDocRef.get()).thenReturn(docTask);
    }

    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    class SingletonTest {

        @Test
        void getInstance_alwaysReturnsSameObject() {
            assertSame(authManager, AuthManager.getInstance());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    class IsLoggedInTests {

        @Test
        void isLoggedIn_withCurrentUser_returnsTrue() {
            when(mockAuth.getCurrentUser()).thenReturn(mockUser);
            assertTrue(authManager.isLoggedIn());
        }

        @Test
        void isLoggedIn_withoutCurrentUser_returnsFalse() {
            when(mockAuth.getCurrentUser()).thenReturn(null);
            assertFalse(authManager.isLoggedIn());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    class LogoutTest {

        @Test
        void logout_delegatesToFirebaseSignOut() {
            authManager.logout();
            verify(mockAuth).signOut();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    class LoginWithEmailTests {

        @Test
        void loginWithEmail_success_callsOnSuccess() {
            when(mockAuth.signInWithEmailAndPassword("a@b.com", "pass"))
                    .thenReturn(authTask);
            completeAuthSuccess();

            AtomicBoolean ok = new AtomicBoolean(false);
            authManager.loginWithEmail("a@b.com", "pass", new AuthCallback() {
                public void onSuccess()           { ok.set(true); }
                public void onFailure(String msg) {}
            });

            assertTrue(ok.get());
        }

        @Test
        void loginWithEmail_failure_callsOnFailureWithServerMessage() {
            when(mockAuth.signInWithEmailAndPassword(anyString(), anyString()))
                    .thenReturn(authTask);
            completeAuthFailure("Invalid credentials");

            AtomicReference<String> err = new AtomicReference<>();
            authManager.loginWithEmail("a@b.com", "wrong", new AuthCallback() {
                public void onSuccess()           {}
                public void onFailure(String msg) { err.set(msg); }
            });

            assertEquals("Invalid credentials", err.get());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    class RegisterWithEmailTests {

        @Test
        void registerWithEmail_success_savesUserToFirestoreAndCallsOnSuccess() {
            when(mockAuth.createUserWithEmailAndPassword("a@b.com", "pass"))
                    .thenReturn(authTask);
            completeAuthSuccess();

            when(mockAuth.getCurrentUser()).thenReturn(mockUser);
            when(mockUser.getUid()).thenReturn("uid-1");

            when(mockDb.collection("users")).thenReturn(mockCollection);
            when(mockCollection.document("uid-1")).thenReturn(mockDocRef);
            when(mockDocRef.set(any(User.class))).thenReturn(voidTask);
            firestoreSetSuccess();

            AtomicBoolean ok = new AtomicBoolean(false);
            authManager.registerWithEmail("a@b.com", "pass", "Alice", new AuthCallback() {
                public void onSuccess()           { ok.set(true); }
                public void onFailure(String msg) {}
            });

            assertTrue(ok.get());
        }

        @Test
        void registerWithEmail_authTaskFails_callsOnFailure() {
            when(mockAuth.createUserWithEmailAndPassword(anyString(), anyString()))
                    .thenReturn(authTask);
            completeAuthFailure("Email already in use");

            AtomicReference<String> err = new AtomicReference<>();
            authManager.registerWithEmail("a@b.com", "pass", "Alice", new AuthCallback() {
                public void onSuccess()           {}
                public void onFailure(String msg) { err.set(msg); }
            });

            assertEquals("Email already in use", err.get());
        }

        @Test
        void registerWithEmail_authSucceedsButCurrentUserNull_callsOnFailure() {
            when(mockAuth.createUserWithEmailAndPassword(anyString(), anyString()))
                    .thenReturn(authTask);
            completeAuthSuccess();
            when(mockAuth.getCurrentUser()).thenReturn(null);

            AtomicReference<String> err = new AtomicReference<>();
            authManager.registerWithEmail("a@b.com", "pass", "Alice", new AuthCallback() {
                public void onSuccess()           {}
                public void onFailure(String msg) { err.set(msg); }
            });

            assertNotNull(err.get());
            assertTrue(err.get().contains("could not retrieve user"));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    class SendPasswordResetEmailTests {

        @Test
        void sendPasswordResetEmail_success_callsOnSuccess() {
            when(mockAuth.sendPasswordResetEmail("a@b.com")).thenReturn(voidTask);
            completeVoidSuccess();

            AtomicBoolean ok = new AtomicBoolean(false);
            authManager.sendPasswordResetEmail("a@b.com", new AuthCallback() {
                public void onSuccess()           { ok.set(true); }
                public void onFailure(String msg) {}
            });

            assertTrue(ok.get());
        }

        @Test
        void sendPasswordResetEmail_failure_callsOnFailure() {
            when(mockAuth.sendPasswordResetEmail("a@b.com")).thenReturn(voidTask);
            completeVoidFailure("No user record");

            AtomicReference<String> err = new AtomicReference<>();
            authManager.sendPasswordResetEmail("a@b.com", new AuthCallback() {
                public void onSuccess()           {}
                public void onFailure(String msg) { err.set(msg); }
            });

            assertEquals("No user record", err.get());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    class CheckAdminStatusTests {

        @Test
        void checkAdminStatus_notLoggedIn_returnsFalse() {
            // FirebaseAuth.getInstance().getCurrentUser() → null (no Firestore call expected)
            when(mockAuth.getCurrentUser()).thenReturn(null);

            AtomicBoolean result = new AtomicBoolean(true);
            authManager.checkAdminStatus(result::set);

            assertFalse(result.get());
        }

        @Test
        void checkAdminStatus_userHasAdminRole_returnsTrue() {
            when(mockAuth.getCurrentUser()).thenReturn(mockUser);
            when(mockUser.getUid()).thenReturn("uid-admin");
            stubFirestoreGet("uid-admin");

            //noinspection unchecked
            when(docTask.addOnSuccessListener(any())).thenAnswer(inv -> {
                ((OnSuccessListener<DocumentSnapshot>) inv.getArgument(0)).onSuccess(mockDocSnapshot);
                return docTask;
            });
            when(docTask.addOnFailureListener(any())).thenReturn(docTask);
            when(mockDocSnapshot.exists()).thenReturn(true);
            when(mockDocSnapshot.getString("role")).thenReturn("admin");

            AtomicBoolean result = new AtomicBoolean(false);
            authManager.checkAdminStatus(result::set);

            assertTrue(result.get());
        }

        @Test
        void checkAdminStatus_userHasCustomerRole_returnsFalse() {
            when(mockAuth.getCurrentUser()).thenReturn(mockUser);
            when(mockUser.getUid()).thenReturn("uid-cust");
            stubFirestoreGet("uid-cust");

            //noinspection unchecked
            when(docTask.addOnSuccessListener(any())).thenAnswer(inv -> {
                ((OnSuccessListener<DocumentSnapshot>) inv.getArgument(0)).onSuccess(mockDocSnapshot);
                return docTask;
            });
            when(docTask.addOnFailureListener(any())).thenReturn(docTask);
            when(mockDocSnapshot.exists()).thenReturn(true);
            when(mockDocSnapshot.getString("role")).thenReturn("customer");

            AtomicBoolean result = new AtomicBoolean(true);
            authManager.checkAdminStatus(result::set);

            assertFalse(result.get());
        }

        @Test
        void checkAdminStatus_userDocumentMissing_returnsFalse() {
            when(mockAuth.getCurrentUser()).thenReturn(mockUser);
            when(mockUser.getUid()).thenReturn("uid-gone");
            stubFirestoreGet("uid-gone");

            //noinspection unchecked
            when(docTask.addOnSuccessListener(any())).thenAnswer(inv -> {
                ((OnSuccessListener<DocumentSnapshot>) inv.getArgument(0)).onSuccess(mockDocSnapshot);
                return docTask;
            });
            when(docTask.addOnFailureListener(any())).thenReturn(docTask);
            when(mockDocSnapshot.exists()).thenReturn(false); // document not found

            AtomicBoolean result = new AtomicBoolean(true);
            authManager.checkAdminStatus(result::set);

            assertFalse(result.get());
        }

        @Test
        void checkAdminStatus_firestoreError_returnsFalse() {
            when(mockAuth.getCurrentUser()).thenReturn(mockUser);
            when(mockUser.getUid()).thenReturn("uid-err");
            stubFirestoreGet("uid-err");

            when(docTask.addOnSuccessListener(any())).thenReturn(docTask);
            //noinspection unchecked
            when(docTask.addOnFailureListener(any())).thenAnswer(inv -> {
                ((OnFailureListener) inv.getArgument(0)).onFailure(new Exception("Network error"));
                return docTask;
            });

            AtomicBoolean result = new AtomicBoolean(true);
            authManager.checkAdminStatus(result::set);

            assertFalse(result.get());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    class SaveUserToFirestoreTests {

        @Test
        void saveUserToFirestore_success_callsOnSuccess() {
            User user = new User("uid-s", "a@b.com", null, "Bob", User.ROLE_CUSTOMER);
            when(mockDb.collection("users")).thenReturn(mockCollection);
            when(mockCollection.document("uid-s")).thenReturn(mockDocRef);
            when(mockDocRef.set(user)).thenReturn(voidTask);
            firestoreSetSuccess();

            AtomicBoolean ok = new AtomicBoolean(false);
            authManager.saveUserToFirestore(user, new AuthCallback() {
                public void onSuccess()           { ok.set(true); }
                public void onFailure(String msg) {}
            });

            assertTrue(ok.get());
        }

        @Test
        void saveUserToFirestore_failure_callsOnFailure() {
            User user = new User("uid-s", "a@b.com", null, "Bob", User.ROLE_CUSTOMER);
            when(mockDb.collection("users")).thenReturn(mockCollection);
            when(mockCollection.document("uid-s")).thenReturn(mockDocRef);
            when(mockDocRef.set(user)).thenReturn(voidTask);
            firestoreSetFailure("Write failed");

            AtomicReference<String> err = new AtomicReference<>();
            authManager.saveUserToFirestore(user, new AuthCallback() {
                public void onSuccess()           {}
                public void onFailure(String msg) { err.set(msg); }
            });

            assertEquals("Write failed", err.get());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    class GetUserFromFirestoreTests {

        @Test
        void getUserFromFirestore_documentExists_returnsUser() {
            stubFirestoreGet("uid-g");

            //noinspection unchecked
            when(docTask.addOnSuccessListener(any())).thenAnswer(inv -> {
                ((OnSuccessListener<DocumentSnapshot>) inv.getArgument(0)).onSuccess(mockDocSnapshot);
                return docTask;
            });
            when(docTask.addOnFailureListener(any())).thenReturn(docTask);
            when(mockDocSnapshot.exists()).thenReturn(true);
            User expected = new User("uid-g", "a@b.com", null, "Carol", User.ROLE_CUSTOMER);
            when(mockDocSnapshot.toObject(User.class)).thenReturn(expected);

            AtomicReference<User> result = new AtomicReference<>();
            authManager.getUserFromFirestore("uid-g", result::set);

            assertSame(expected, result.get());
        }

        @Test
        void getUserFromFirestore_documentNotFound_returnsNull() {
            stubFirestoreGet("uid-missing");

            //noinspection unchecked
            when(docTask.addOnSuccessListener(any())).thenAnswer(inv -> {
                ((OnSuccessListener<DocumentSnapshot>) inv.getArgument(0)).onSuccess(mockDocSnapshot);
                return docTask;
            });
            when(docTask.addOnFailureListener(any())).thenReturn(docTask);
            when(mockDocSnapshot.exists()).thenReturn(false);

            AtomicReference<User> result = new AtomicReference<>(new User());
            authManager.getUserFromFirestore("uid-missing", result::set);

            assertNull(result.get());
        }

        @Test
        void getUserFromFirestore_firestoreError_returnsNull() {
            stubFirestoreGet("uid-fail");

            when(docTask.addOnSuccessListener(any())).thenReturn(docTask);
            //noinspection unchecked
            when(docTask.addOnFailureListener(any())).thenAnswer(inv -> {
                ((OnFailureListener) inv.getArgument(0)).onFailure(new Exception("Read error"));
                return docTask;
            });

            AtomicReference<User> result = new AtomicReference<>(new User());
            authManager.getUserFromFirestore("uid-fail", result::set);

            assertNull(result.get());
        }
    }
}
