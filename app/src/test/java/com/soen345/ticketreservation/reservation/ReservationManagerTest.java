package com.soen345.ticketreservation.reservation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import android.content.Context;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.soen345.ticketreservation.auth.AuthManager;
import com.soen345.ticketreservation.email.EmailManager;
import com.soen345.ticketreservation.event.EventManager;
import com.soen345.ticketreservation.model.Event;
import com.soen345.ticketreservation.model.Reservation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class ReservationManagerTest {

    @Mock
    FirebaseFirestore db;

    @Mock
    AuthManager authManager;

    @Mock
    EventManager eventManager;

    @Mock
    EmailManager emailManager;

    @Mock
    CollectionReference collectionReference;

    @Mock
    DocumentReference documentReference;

    @Mock
    FirebaseUser firebaseUser;

    @Mock
    Context context;

    ReservationManager reservationManager;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        reservationManager = new ReservationManager(
                db,
                authManager,
                eventManager,
                emailManager
        );
    }

    @Test
    void createReservation_userNotLoggedIn_throwsException() {

        when(authManager.isLoggedIn()).thenReturn(false);

        Reservation reservation = new Reservation();
        Event event = new Event();

        assertThrows(
                IllegalStateException.class,
                () -> reservationManager.createReservation(reservation, event, context)
        );
    }

    @Test
    void createReservation_savesReservation() {

        when(authManager.isLoggedIn()).thenReturn(true);
        when(authManager.getCurrentUser()).thenReturn(firebaseUser);
        when(firebaseUser.getEmail()).thenReturn("test@test.com");

        when(db.collection("reservations")).thenReturn(collectionReference);
        when(collectionReference.document()).thenReturn(documentReference);
        when(documentReference.getId()).thenReturn("res123");

        Task<Void> task = mock(Task.class);

        when(documentReference.set(any())).thenReturn(task);
        when(task.addOnSuccessListener(any())).thenReturn(task);
        when(task.addOnFailureListener(any())).thenReturn(task);

        Reservation reservation = new Reservation();
        reservation.setQuantity(2);

        Event event = mock(Event.class);
        when(event.getAvailableSeats()).thenReturn(10);
        when(event.getName()).thenReturn("Test Event");

        reservationManager.createReservation(reservation, event, context);

        verify(documentReference).set(reservation);
        assertEquals("res123", reservation.getReservationId());
    }
}