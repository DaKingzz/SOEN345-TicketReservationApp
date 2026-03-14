package com.soen345.ticketreservation.activity;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.ListenerRegistration;
import com.soen345.ticketreservation.R;
import com.soen345.ticketreservation.adapter.ReservationAdapter;
import com.soen345.ticketreservation.event.EventManager;
import com.soen345.ticketreservation.reservation.ReservationManager;
import com.soen345.ticketreservation.model.Event;
import com.soen345.ticketreservation.model.OnReservationInteractionListener;
import com.soen345.ticketreservation.model.Reservation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReservationListingActivity extends BaseActivity{
    private ReservationManager reservationManager;
    private EventManager eventManager;
    private ListenerRegistration reservationListener;
    private ListenerRegistration eventListener;
    private List<Reservation> allReservations = new ArrayList<>();
    private List<Event> allEvents = new ArrayList<>();
    private RecyclerView rvReservations;
    private ReservationAdapter reservationAdapter;
    private Map<String, Event> eventsMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation_listing);

        reservationManager = ReservationManager.getInstance();
        eventManager = EventManager.getInstance();

        setUpRecyclerView();
    }
    @Override
    protected void onStart() {
        super.onStart();
        startListeningForReservations();
        startListeningForEvents();
    }
    @Override
    protected void onStop() {
        super.onStop();
        if (reservationListener != null) {
            reservationListener.remove();
        }
        if (eventListener != null) {
            eventListener.remove();
        }
    }
    private void startListeningForReservations() {
        if (reservationListener != null) {
            reservationListener.remove();
        }
        reservationListener = reservationManager.listenToReservations(reservations -> {
            allReservations.clear();
            allReservations.addAll(reservations);
            if (reservationAdapter != null) {
                reservationAdapter.notifyDataSetChanged();
            }
        });
    }

    private void startListeningForEvents() {
        if (eventListener != null) {
            eventListener.remove();
        }
        eventListener = eventManager.listenToEvents(events -> {
            allEvents.clear();
            allEvents.addAll(events);
            mapEvents();
            if (reservationAdapter != null) {
                reservationAdapter.notifyDataSetChanged();
            }
        });
    }

    private void mapEvents() {
        for (Event event : allEvents) {
            eventsMap.put(event.getEventId(), event);
        }
    }

    private void setUpRecyclerView() {
        rvReservations = findViewById(R.id.rvReservations);
        reservationAdapter = new ReservationAdapter(allReservations, eventsMap, new OnReservationInteractionListener() {
            @Override
            public void onCancelClick(Reservation reservation, Event event, int position) {
                Log.d("ReservationListingActivity", "onCancelClick called with position: " + position);
                AlertDialog.Builder builder = new AlertDialog.Builder(ReservationListingActivity.this);
                builder.setTitle("Confirm Cancellation");
                builder.setMessage("Are you sure you want to cancel this reservation?");
                builder.setPositiveButton("Yes", (dialog, which) -> {
                    reservationManager.cancelReservation(reservation, event, ReservationListingActivity.this);
                        })
                        .setNegativeButton("No", (dialog, which) -> {
                            dialog.dismiss();
                        });
                AlertDialog dialog = builder.create();
                dialog.show();

            }
        });
        rvReservations.setLayoutManager(new LinearLayoutManager(this));
        rvReservations.setAdapter(reservationAdapter);
    }
}
