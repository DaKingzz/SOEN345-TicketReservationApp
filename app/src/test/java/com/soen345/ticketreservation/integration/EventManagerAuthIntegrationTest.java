package com.soen345.ticketreservation.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.soen345.ticketreservation.auth.AuthManager;
import com.soen345.ticketreservation.event.EventManager;
import com.soen345.ticketreservation.model.Event;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@ExtendWith(MockitoExtension.class)
class EventManagerAuthIntegrationTest {

    @Mock private FirebaseAuth            mockAuth;
    @Mock private FirebaseFirestore       mockDb;
    @Mock private FirebaseUser            mockUser;
    @Mock private CollectionReference     eventsCollection;
    @Mock private CollectionReference     usersCollection;
    @Mock private DocumentReference       eventDocRef;
    @Mock private DocumentReference       userDocRef;
    @Mock private DocumentSnapshot        userDocSnapshot;
    @Mock private Task<Void>              voidTask;
    @Mock private Task<DocumentSnapshot>  docTask;

    private AuthManager authManager;
    private EventManager eventManager;
    private MockedStatic<FirebaseAuth>      mockedAuth;
    private MockedStatic<FirebaseFirestore> mockedFirestore;

    @BeforeEach
    void setUp() {
        authManager  = new AuthManager(mockAuth, mockDb);
        eventManager = new EventManager(mockDb, authManager);
        mockedAuth      = mockStatic(FirebaseAuth.class);
        mockedFirestore = mockStatic(FirebaseFirestore.class);
    }

    @AfterEach
    void tearDown() {
        mockedAuth.close();
        mockedFirestore.close();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Stubs checkAdminStatus() Firestore call to return the given role. */
    @SuppressWarnings("unchecked")
    private void stubUserRole(String uid, String role) {
        when(mockDb.collection("users")).thenReturn(usersCollection);
        when(usersCollection.document(uid)).thenReturn(userDocRef);
        when(userDocRef.get()).thenReturn(docTask);
        when(docTask.addOnSuccessListener(any())).thenAnswer(inv -> {
            ((OnSuccessListener<DocumentSnapshot>) inv.getArgument(0)).onSuccess(userDocSnapshot);
            return docTask;
        });
        when(docTask.addOnFailureListener(any())).thenReturn(docTask);
        when(userDocSnapshot.exists()).thenReturn(true);
        when(userDocSnapshot.getString("role")).thenReturn(role);
    }

    /** Stubs a successful Firestore set() on the events collection. */
    @SuppressWarnings("unchecked")
    private void stubEventSaveSuccess(String generatedId) {
        when(mockDb.collection("events")).thenReturn(eventsCollection);
        when(eventsCollection.document()).thenReturn(eventDocRef);
        when(eventDocRef.getId()).thenReturn(generatedId);
        when(eventDocRef.set(any(Event.class))).thenReturn(voidTask);
        when(voidTask.addOnSuccessListener(any())).thenAnswer(inv -> {
            ((OnSuccessListener<Void>) inv.getArgument(0)).onSuccess(null);
            return voidTask;
        });
        when(voidTask.addOnFailureListener(any())).thenReturn(voidTask);
    }

    // ── createEvent ───────────────────────────────────────────────────────────

    @Test
    void createEvent_authManagerReportsNotLoggedIn_throwsIllegalStateException() {
        when(mockAuth.getCurrentUser()).thenReturn(null);

        assertThrows(IllegalStateException.class, () -> eventManager.createEvent(new Event()));
    }

    @Test
    void createEvent_authManagerReportsLoggedInAsAdmin_savesEventAndAssignsId() {
        when(mockAuth.getCurrentUser()).thenReturn(mockUser);
        when(mockUser.getUid()).thenReturn("uid-admin");
        stubUserRole("uid-admin", "admin");
        stubEventSaveSuccess("evt-integration-1");

        Event event = new Event();
        eventManager.createEvent(event);

        assertEquals("evt-integration-1", event.getEventId());
        verify(eventDocRef).set(event);
    }

    @Test
    void createEvent_authManagerReportsLoggedInAsCustomer_doesNotTouchEventsCollection() {
        when(mockAuth.getCurrentUser()).thenReturn(mockUser);
        when(mockUser.getUid()).thenReturn("uid-customer");
        stubUserRole("uid-customer", "customer");

        eventManager.createEvent(new Event());

        verify(mockDb, never()).collection("events");
    }

    // ── updateEvent ───────────────────────────────────────────────────────────

    @Test
    void updateEvent_authManagerReportsNotLoggedIn_callsOnFailureWithIllegalStateException() {
        when(mockAuth.getCurrentUser()).thenReturn(null);

        AtomicReference<Exception> err = new AtomicReference<>();
        eventManager.updateEvent(new Event(), null, err::set);

        assertNotNull(err.get());
        assertTrue(err.get() instanceof IllegalStateException);
    }

    @Test
    void updateEvent_authManagerReportsLoggedInAsAdmin_callsOnSuccess() {
        when(mockAuth.getCurrentUser()).thenReturn(mockUser);
        when(mockUser.getUid()).thenReturn("uid-admin");
        stubUserRole("uid-admin", "admin");

        Event event = new Event();
        event.setEventId("evt-42");

        when(mockDb.collection("events")).thenReturn(eventsCollection);
        when(eventsCollection.document("evt-42")).thenReturn(eventDocRef);
        when(eventDocRef.set(event)).thenReturn(voidTask);
        when(voidTask.addOnSuccessListener(any())).thenAnswer(inv -> {
            ((OnSuccessListener<Void>) inv.getArgument(0)).onSuccess(null);
            return voidTask;
        });
        when(voidTask.addOnFailureListener(any())).thenReturn(voidTask);

        AtomicBoolean ok = new AtomicBoolean(false);
        eventManager.updateEvent(event, () -> ok.set(true), null);

        assertTrue(ok.get());
    }

    // ── deleteEvent ───────────────────────────────────────────────────────────

    @Test
    void deleteEvent_authManagerReportsNotLoggedIn_callsOnFailureWithIllegalStateException() {
        when(mockAuth.getCurrentUser()).thenReturn(null);

        AtomicReference<Exception> err = new AtomicReference<>();
        eventManager.deleteEvent("evt-1", null, err::set);

        assertNotNull(err.get());
        assertTrue(err.get() instanceof IllegalStateException);
    }

    @Test
    void deleteEvent_authManagerReportsLoggedInAsAdmin_callsOnSuccess() {
        when(mockAuth.getCurrentUser()).thenReturn(mockUser);
        when(mockUser.getUid()).thenReturn("uid-admin");
        stubUserRole("uid-admin", "admin");

        when(mockDb.collection("events")).thenReturn(eventsCollection);
        when(eventsCollection.document("evt-99")).thenReturn(eventDocRef);
        when(eventDocRef.delete()).thenReturn(voidTask);
        when(voidTask.addOnSuccessListener(any())).thenAnswer(inv -> {
            ((OnSuccessListener<Void>) inv.getArgument(0)).onSuccess(null);
            return voidTask;
        });
        when(voidTask.addOnFailureListener(any())).thenReturn(voidTask);

        AtomicBoolean ok = new AtomicBoolean(false);
        eventManager.deleteEvent("evt-99", () -> ok.set(true), null);

        assertTrue(ok.get());
    }

    // ── getEvents ─────────────────────────────────────────────────────────────

    @Test
    @SuppressWarnings("unchecked")
    void getEvents_firestoreReturnsDocuments_callbackReceivesDeserializedList() {
        Task<QuerySnapshot> queryTask = mock(Task.class);
        QuerySnapshot querySnapshot   = mock(QuerySnapshot.class);
        QueryDocumentSnapshot doc     = mock(QueryDocumentSnapshot.class);
        Event expected = new Event();

        when(mockDb.collection("events")).thenReturn(eventsCollection);
        when(eventsCollection.get()).thenReturn(queryTask);
        when(queryTask.addOnSuccessListener(any())).thenAnswer(inv -> {
            ((OnSuccessListener<QuerySnapshot>) inv.getArgument(0)).onSuccess(querySnapshot);
            return queryTask;
        });
        when(queryTask.addOnFailureListener(any())).thenReturn(queryTask);
        when(doc.toObject(Event.class)).thenReturn(expected);
        when(querySnapshot.iterator()).thenReturn(Collections.singletonList(doc).iterator());

        AtomicReference<List<Event>> result = new AtomicReference<>();
        eventManager.getEvents(result::set);

        assertNotNull(result.get());
        assertEquals(1, result.get().size());
    }

    @Test
    @SuppressWarnings("unchecked")
    void getEvents_firestoreFails_callbackReceivesEmptyList() {
        Task<QuerySnapshot> queryTask = mock(Task.class);

        when(mockDb.collection("events")).thenReturn(eventsCollection);
        when(eventsCollection.get()).thenReturn(queryTask);
        when(queryTask.addOnSuccessListener(any())).thenReturn(queryTask);
        when(queryTask.addOnFailureListener(any())).thenAnswer(inv -> {
            ((OnFailureListener) inv.getArgument(0)).onFailure(new Exception("Network error"));
            return queryTask;
        });

        AtomicReference<List<Event>> result = new AtomicReference<>();
        eventManager.getEvents(result::set);

        assertNotNull(result.get());
        assertTrue(result.get().isEmpty());
    }
}
