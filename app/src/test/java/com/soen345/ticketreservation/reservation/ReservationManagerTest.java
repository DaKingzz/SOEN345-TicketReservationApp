package com.soen345.ticketreservation.reservation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import android.content.Context;

import androidx.appcompat.app.AlertDialog;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;
import com.soen345.ticketreservation.auth.AuthManager;
import com.soen345.ticketreservation.email.EmailManager;
import com.soen345.ticketreservation.event.EventManager;
import com.soen345.ticketreservation.model.Event;
import com.soen345.ticketreservation.model.Reservation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockitoAnnotations;

class ReservationManagerTest {

    @Mock FirebaseFirestore db;
    @Mock AuthManager authManager;
    @Mock EventManager eventManager;
    @Mock EmailManager emailManager;
    @Mock CollectionReference reservationsCollection;
    @Mock CollectionReference eventsCollection;
    @Mock DocumentReference resDocRef;
    @Mock DocumentReference eventDocRef;
    @Mock Transaction transaction;
    @Mock DocumentSnapshot eventSnapshot;
    @Mock FirebaseUser firebaseUser;
    @Mock Context context;
    @Mock Task<Void> transactionTask;

    ReservationManager reservationManager;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        reservationManager = new ReservationManager(db, authManager, eventManager, emailManager);

        when(db.collection("reservations")).thenReturn(reservationsCollection);
        when(db.collection("events")).thenReturn(eventsCollection);
        when(reservationsCollection.document()).thenReturn(resDocRef);
        when(resDocRef.getId()).thenReturn("res123");
        
        doReturn(transactionTask).when(db).runTransaction(any());
        
        when(transactionTask.addOnSuccessListener(any())).thenReturn(transactionTask);
        when(transactionTask.addOnFailureListener(any())).thenReturn(transactionTask);
    }

    @Test
    void createReservation_userNotLoggedIn_throwsException() {
        when(authManager.isLoggedIn()).thenReturn(false);
        assertThrows(IllegalStateException.class, 
            () -> reservationManager.createReservation(new Reservation(), new Event(), context));
    }

    @Test
    @SuppressWarnings("unchecked")
    void createReservation_successfulTransaction() throws FirebaseFirestoreException {
        try (MockedConstruction<AlertDialog.Builder> mocked = mockConstruction(AlertDialog.Builder.class,
                (mock, context) -> {
                    when(mock.setTitle(anyString())).thenReturn(mock);
                    when(mock.setMessage(anyString())).thenReturn(mock);
                    when(mock.setPositiveButton(anyString(), any())).thenReturn(mock);
                    when(mock.show()).thenReturn(mock(AlertDialog.class));
                })) {
            
            when(authManager.isLoggedIn()).thenReturn(true);
            when(authManager.getCurrentUser()).thenReturn(firebaseUser);
            when(firebaseUser.getEmail()).thenReturn("test@test.com");

            Reservation reservation = new Reservation();
            reservation.setQuantity(2);
            Event event = new Event("event123", "Concert", "Music", null, "Montreal", 100);
            event.setAvailableSeats(10);

            when(eventsCollection.document("event123")).thenReturn(eventDocRef);
            when(transaction.get(eventDocRef)).thenReturn(eventSnapshot);
            when(eventSnapshot.toObject(Event.class)).thenReturn(event);

            reservationManager.createReservation(reservation, event, context);

            ArgumentCaptor<Transaction.Function<Void>> transactionCaptor = ArgumentCaptor.forClass(Transaction.Function.class);
            verify(db).runTransaction(transactionCaptor.capture());
            transactionCaptor.getValue().apply(transaction);

            verify(transaction).update(eventDocRef, "availableSeats", 8);
            verify(transaction).set(eq(resDocRef), any(Reservation.class));
            assertEquals("res123", reservation.getReservationId());
            
            ArgumentCaptor<OnSuccessListener<Void>> successCaptor = ArgumentCaptor.forClass(OnSuccessListener.class);
            verify(transactionTask).addOnSuccessListener(successCaptor.capture());
            successCaptor.getValue().onSuccess(null);
            
            verify(emailManager).sendConfirmation("test@test.com", "Concert", 2);
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void createReservation_insufficientSeats_failsTransaction() throws FirebaseFirestoreException {
        when(authManager.isLoggedIn()).thenReturn(true);
        Reservation reservation = new Reservation();
        reservation.setQuantity(15); 
        Event event = new Event("event123", "Concert", "Music", null, "Montreal", 100);
        event.setAvailableSeats(10);

        when(eventsCollection.document("event123")).thenReturn(eventDocRef);
        when(transaction.get(eventDocRef)).thenReturn(eventSnapshot);
        when(eventSnapshot.toObject(Event.class)).thenReturn(event);

        reservationManager.createReservation(reservation, event, context);
        
        ArgumentCaptor<Transaction.Function<Void>> transactionCaptor = ArgumentCaptor.forClass(Transaction.Function.class);
        verify(db).runTransaction(transactionCaptor.capture());
        
        FirebaseFirestoreException ex = assertThrows(FirebaseFirestoreException.class, 
            () -> transactionCaptor.getValue().apply(transaction));
        
        assertTrue(ex.getMessage().contains("Not enough seats"));
        verify(transaction, never()).update(any(), anyString(), anyInt());
        verify(transaction, never()).set(any(), any());
    }

    @Test
    @SuppressWarnings("unchecked")
    void createReservation_transactionFailure_showsError() {
        try (MockedConstruction<AlertDialog.Builder> mocked = mockConstruction(AlertDialog.Builder.class,
                (mock, context) -> {
                    when(mock.setTitle(anyString())).thenReturn(mock);
                    when(mock.setMessage(anyString())).thenReturn(mock);
                    when(mock.setPositiveButton(anyString(), any())).thenReturn(mock);
                    when(mock.show()).thenReturn(mock(AlertDialog.class));
                })) {
            
            when(authManager.isLoggedIn()).thenReturn(true);
            reservationManager.createReservation(new Reservation(), new Event("id", "n", "c", null, "l", 10), context);

            ArgumentCaptor<OnFailureListener> failureCaptor = ArgumentCaptor.forClass(OnFailureListener.class);
            verify(transactionTask).addOnFailureListener(failureCaptor.capture());

            Exception error = new Exception("Transaction failed");
            failureCaptor.getValue().onFailure(error);

            verifyNoInteractions(emailManager);
        }
    }
}
