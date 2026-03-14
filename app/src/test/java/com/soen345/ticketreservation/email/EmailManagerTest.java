package com.soen345.ticketreservation.email;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

@ExtendWith(MockitoExtension.class)
public class EmailManagerTest {
    private EmailManager emailManager;
    @Mock
    private FirebaseFirestore db;
    @Mock
    private CollectionReference collectionReference;
    @Mock
    private DocumentReference documentReference;

    private MockedStatic<FirebaseFirestore> mockedFirestore;


    @BeforeEach
    void setUp() {
        emailManager = new EmailManager(db);
        mockedFirestore = mockStatic(FirebaseFirestore.class);
    }
    @AfterEach
    void tearDown() {
        mockedFirestore.close();
    }
    @Test
    void getInstance_returnsSameInstance() {
        mockedFirestore.when(FirebaseFirestore::getInstance).thenReturn(db);

        EmailManager instance1 = EmailManager.getInstance();
        EmailManager instance2 = EmailManager.getInstance();

        assertSame(instance1, instance2);
    }
    @Test
    void sendConfirmation_sendsEmail() {

        when(db.collection("mail")).thenReturn(collectionReference);
        when(collectionReference.document()).thenReturn(documentReference);
        when(documentReference.getId()).thenReturn("test-id");

        Task<Void> voidTask = mock(Task.class);

        when(documentReference.set(any(Map.class))).thenReturn(voidTask);

        when(voidTask.addOnSuccessListener(any())).thenReturn(voidTask);
        when(voidTask.addOnFailureListener(any())).thenReturn(voidTask);

        emailManager.sendConfirmation(
                "james.c.mcreynolds@example-pet-store.com",
                "Test Event",
                2
        );

        verify(documentReference).set(any(Map.class));
    }
}
