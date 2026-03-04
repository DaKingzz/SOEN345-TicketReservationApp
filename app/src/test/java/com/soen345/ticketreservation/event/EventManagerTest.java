package com.soen345.ticketreservation.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.soen345.ticketreservation.auth.AuthManager;
import com.soen345.ticketreservation.model.Event;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

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
    void createEvent_notLoggedIn_throwsIllegalStateException() {
        when(authManager.isLoggedIn()).thenReturn(false);
        Event event = new Event();

        assertThrows(IllegalStateException.class, () -> eventManager.createEvent(event));
    }

    @Test
    void createEvent_notAdmin_doesNotSaveEvent() {
        when(authManager.isLoggedIn()).thenReturn(true);

        doAnswer(invocation -> {
            AuthManager.OnAdminResult callback = invocation.getArgument(0);
            callback.onResult(false);
            return null;
        }).when(authManager).checkAdminStatus(any());

        Event event = new Event();
        eventManager.createEvent(event);

        verify(db, never()).collection(anyString());
    }

    @Test
    void createEvent_admin_savesEventSuccessfully() {
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

        when(voidTask.addOnSuccessListener(any())).thenAnswer(invocation -> {
            OnSuccessListener<Void> listener = invocation.getArgument(0);
            listener.onSuccess(null);
            return voidTask;
        });
        when(voidTask.addOnFailureListener(any())).thenReturn(voidTask);

        Event event = new Event();
        eventManager.createEvent(event);

        assertEquals("test-event-id", event.getEventId());
        verify(documentReference).set(event);
    }

    @Test
    void createEvent_admin_saveFails() {
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
}
