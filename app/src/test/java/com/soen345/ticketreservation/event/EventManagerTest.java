package com.soen345.ticketreservation.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.soen345.ticketreservation.auth.AuthManager;
import com.soen345.ticketreservation.model.Event;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@ExtendWith(MockitoExtension.class)
class EventManagerTest {

    @Mock
    private FirebaseFirestore db;

    @Mock
    private AuthManager authManager;

    @Mock
    private CollectionReference collectionReference;

    @Mock
    private DocumentReference documentReference;

    @Mock
    private Task<Void> voidTask;

    @Mock
    private Task<QuerySnapshot> querySnapshotTask;

    @Mock
    private QuerySnapshot querySnapshot;

    @Mock
    private ListenerRegistration listenerRegistration;

    private EventManager eventManager;
    private MockedStatic<FirebaseFirestore> mockedFirestore;
    private MockedStatic<AuthManager> mockedAuthManager;

    @BeforeEach
    void setUp() {
        eventManager = new EventManager(db, authManager);
        mockedFirestore = mockStatic(FirebaseFirestore.class);
        mockedAuthManager = mockStatic(AuthManager.class);
    }

    @AfterEach
    void tearDown() {
        mockedFirestore.close();
        mockedAuthManager.close();
    }

    @Test
    void getInstance_returnsSameInstance() {
        mockedFirestore.when(FirebaseFirestore::getInstance).thenReturn(db);
        mockedAuthManager.when(AuthManager::getInstance).thenReturn(authManager);

        EventManager instance1 = EventManager.getInstance();
        EventManager instance2 = EventManager.getInstance();
        assertNotNull(instance1);
        assertSame(instance1, instance2);
    }

    @Test
    void createEvent_admin_saveFails_logsError() {
        when(authManager.isLoggedIn()).thenReturn(true);
        doAnswer(invocation -> {
            AuthManager.OnAdminResult callback = invocation.getArgument(0);
            callback.onResult(true);
            return null;
        }).when(authManager).checkAdminStatus(any());

        when(db.collection("events")).thenReturn(collectionReference);
        when(collectionReference.document()).thenReturn(documentReference);
        when(documentReference.getId()).thenReturn("test-event-id");
        when(documentReference.set(any(Event.class))).thenReturn(voidTask);

        when(voidTask.addOnSuccessListener(any())).thenReturn(voidTask);
        when(voidTask.addOnFailureListener(any())).thenAnswer(invocation -> {
            OnFailureListener listener = invocation.getArgument(0);
            listener.onFailure(new Exception("Firestore error"));
            return voidTask;
        });

        Event event = new Event();
        eventManager.createEvent(event);

        assertEquals("test-event-id", event.getEventId());
        verify(documentReference).set(event);
    }

    @Test
    void updateEvent_notAdmin_callsOnFailure() {
        when(authManager.isLoggedIn()).thenReturn(true);
        doAnswer(invocation -> {
            AuthManager.OnAdminResult callback = invocation.getArgument(0);
            callback.onResult(false);
            return null;
        }).when(authManager).checkAdminStatus(any());

        AtomicReference<Exception> capturedException = new AtomicReference<>();
        eventManager.updateEvent(new Event(), null, capturedException::set);

        assertNotNull(capturedException.get());
        assertTrue(capturedException.get().getMessage().contains("Admin status required"));
    }

    @Test
    void updateEvent_missingId_callsOnFailure() {
        when(authManager.isLoggedIn()).thenReturn(true);
        doAnswer(invocation -> {
            AuthManager.OnAdminResult callback = invocation.getArgument(0);
            callback.onResult(true);
            return null;
        }).when(authManager).checkAdminStatus(any());

        AtomicReference<Exception> capturedException = new AtomicReference<>();
        Event event = new Event(); // No ID set
        eventManager.updateEvent(event, null, capturedException::set);

        assertNotNull(capturedException.get());
        assertTrue(capturedException.get() instanceof IllegalArgumentException);
    }

    @Test
    void updateEvent_failure_callsOnFailure() {
        when(authManager.isLoggedIn()).thenReturn(true);
        doAnswer(invocation -> {
            AuthManager.OnAdminResult callback = invocation.getArgument(0);
            callback.onResult(true);
            return null;
        }).when(authManager).checkAdminStatus(any());

        Event event = new Event();
        event.setEventId("id123");

        when(db.collection("events")).thenReturn(collectionReference);
        when(collectionReference.document("id123")).thenReturn(documentReference);
        when(documentReference.set(event)).thenReturn(voidTask);
        when(voidTask.addOnSuccessListener(any())).thenReturn(voidTask);
        Exception exception = new Exception("Update failed");
        when(voidTask.addOnFailureListener(any())).thenAnswer(invocation -> {
            ((OnFailureListener) invocation.getArgument(0)).onFailure(exception);
            return voidTask;
        });

        AtomicReference<Exception> result = new AtomicReference<>();
        eventManager.updateEvent(event, null, result::set);

        assertSame(exception, result.get());
    }

    @Test
    void deleteEvent_notAdmin_callsOnFailure() {
        when(authManager.isLoggedIn()).thenReturn(true);
        doAnswer(invocation -> {
            AuthManager.OnAdminResult callback = invocation.getArgument(0);
            callback.onResult(false);
            return null;
        }).when(authManager).checkAdminStatus(any());

        AtomicReference<Exception> result = new AtomicReference<>();
        eventManager.deleteEvent("id123", null, result::set);
        assertTrue(result.get().getMessage().contains("Admin status required"));
    }

    @Test
    void deleteEvent_failure_callsOnFailure() {
        when(authManager.isLoggedIn()).thenReturn(true);
        doAnswer(invocation -> {
            AuthManager.OnAdminResult callback = invocation.getArgument(0);
            callback.onResult(true);
            return null;
        }).when(authManager).checkAdminStatus(any());

        when(db.collection("events")).thenReturn(collectionReference);
        when(collectionReference.document("id123")).thenReturn(documentReference);
        when(documentReference.delete()).thenReturn(voidTask);
        when(voidTask.addOnSuccessListener(any())).thenReturn(voidTask);
        Exception exception = new Exception("Delete failed");
        when(voidTask.addOnFailureListener(any())).thenAnswer(invocation -> {
            ((OnFailureListener) invocation.getArgument(0)).onFailure(exception);
            return voidTask;
        });

        AtomicReference<Exception> result = new AtomicReference<>();
        eventManager.deleteEvent("id123", null, result::set);

        assertSame(exception, result.get());
    }

    @Test
    void listenToEvents_triggersCallback() {
        when(db.collection("events")).thenReturn(collectionReference);
        ArgumentCaptor<EventListener<QuerySnapshot>> listenerCaptor = ArgumentCaptor.forClass(EventListener.class);
        when(collectionReference.addSnapshotListener(listenerCaptor.capture())).thenReturn(listenerRegistration);

        AtomicReference<List<Event>> result = new AtomicReference<>();
        eventManager.listenToEvents(result::set);

        QueryDocumentSnapshot doc = mock(QueryDocumentSnapshot.class);
        Event event = new Event();
        when(doc.toObject(Event.class)).thenReturn(event);
        when(querySnapshot.iterator()).thenReturn(Collections.singletonList(doc).iterator());

        listenerCaptor.getValue().onEvent(querySnapshot, null);

        assertNotNull(result.get());
        assertEquals(1, result.get().size());
        assertSame(event, result.get().get(0));
    }

    @Test
    void listenToEvents_error_doesNotTriggerCallback() {
        when(db.collection("events")).thenReturn(collectionReference);
        ArgumentCaptor<EventListener<QuerySnapshot>> listenerCaptor = ArgumentCaptor.forClass(EventListener.class);
        when(collectionReference.addSnapshotListener(listenerCaptor.capture())).thenReturn(listenerRegistration);

        AtomicReference<List<Event>> result = new AtomicReference<>();
        eventManager.listenToEvents(result::set);

        FirebaseFirestoreException firestoreException = new FirebaseFirestoreException("Listen error", FirebaseFirestoreException.Code.UNKNOWN);
        listenerCaptor.getValue().onEvent(null, firestoreException);

        assertTrue(result.get() == null);
    }
}
