package com.soen345.ticketreservation.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.ListenerRegistration;
import com.soen345.ticketreservation.R;
import com.soen345.ticketreservation.adapter.EventAdapter;
import com.soen345.ticketreservation.auth.AuthManager;
import com.soen345.ticketreservation.event.EventManager;
import com.soen345.ticketreservation.model.Event;
import com.soen345.ticketreservation.model.EventCategory;
import com.soen345.ticketreservation.model.OnEventInteractionListener;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class EventListing extends BaseActivity {

    private TextView welcomeText;
    private AuthManager authManager;
    private EventManager eventManager;
    private RecyclerView rvEvents;
    private EventAdapter eventAdapter;
    private List<Event> allEvents = new ArrayList<>();
    private List<Event> filteredEvents = new ArrayList<>();
    private ListenerRegistration eventListener;
    private ChipGroup chipGroupFilters;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_listing);

        authManager = AuthManager.getInstance();
        eventManager = EventManager.getInstance();

        welcomeText = findViewById(R.id.welcome_text);
        rvEvents = findViewById(R.id.rvEvents);
        chipGroupFilters = findViewById(R.id.chipGroupFilters);

        setupRecyclerView();
        setupFilterChips();
        loadUserGreeting();
        initAdminUI();
    }

    @Override
    protected void onStart() {
        super.onStart();
        startListeningForEvents();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (eventListener != null) {
            eventListener.remove();
        }
    }

    private void setupRecyclerView() {
        eventAdapter = new EventAdapter(filteredEvents, new OnEventInteractionListener() {
            @Override
            public void onDeleteClick(Event event, int position) {
                eventManager.deleteEvent(
                        event.getEventId(),
                        () -> Toast.makeText(EventListing.this, "Event Deleted", Toast.LENGTH_SHORT).show(),
                        e -> Toast.makeText(EventListing.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onEditClick(Event event, int position) {
                Intent intent = new Intent(EventListing.this, CreateEventActivity.class);
                intent.putExtra("EVENT_TO_EDIT", event);
                startActivity(intent);
            }
        });
        rvEvents.setLayoutManager(new LinearLayoutManager(this));
        rvEvents.setAdapter(eventAdapter);
    }

    private void setupFilterChips() {
        chipGroupFilters.setOnCheckedChangeListener((group, checkedId) -> {
            filterEvents();
        });
    }

    private void filterEvents() {
        int checkedId = chipGroupFilters.getCheckedChipId();
        String selectedCategory = "";

        if (checkedId == R.id.chipConcert) {
            selectedCategory = EventCategory.CONCERT.toString();
        } else if (checkedId == R.id.chipMovie) {
            selectedCategory = EventCategory.MOVIE.toString();
        } else if (checkedId == R.id.chipSport) {
            selectedCategory = EventCategory.SPORT.toString();
        } else if (checkedId == R.id.chipTravel) {
            selectedCategory = EventCategory.TRAVEL.toString();
        }

        filteredEvents.clear();
        if (selectedCategory.isEmpty()) {
            filteredEvents.addAll(allEvents);
        } else {
            String finalSelectedCategory = selectedCategory;
            filteredEvents.addAll(allEvents.stream()
                    .filter(e -> e.getCategory() != null && e.getCategory().equalsIgnoreCase(finalSelectedCategory))
                    .collect(Collectors.toList()));
        }
        eventAdapter.notifyDataSetChanged();
    }

    private void startListeningForEvents() {
        if (eventListener != null) {
            eventListener.remove();
        }
        eventListener = eventManager.listenToEvents(events -> {
            allEvents.clear();
            allEvents.addAll(events);
            filterEvents();
        });
    }

    private void loadUserGreeting() {
        FirebaseUser firebaseUser = authManager.getCurrentUser();
        if (firebaseUser == null) {
            logout();
            return;
        }

        authManager.getUserFromFirestore(firebaseUser.getUid(), user -> {
            String name;
            if (user != null && user.getDisplayName() != null && !user.getDisplayName().isEmpty()) {
                name = user.getDisplayName();
            } else if (firebaseUser.getDisplayName() != null && !firebaseUser.getDisplayName().isEmpty()) {
                name = firebaseUser.getDisplayName();
            } else {
                name = "User";
            }
            welcomeText.setText(getString(R.string.welcome_greeting, name));
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            logout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void initAdminUI() {
        Button btnGoCreateEvent = findViewById(R.id.btnGoToCreateEvent);
        btnGoCreateEvent.setVisibility(View.GONE);

        authManager.checkAdminStatus(isAdmin -> {
            eventAdapter.setAdmin(isAdmin);

            if (isAdmin) {
                btnGoCreateEvent.setVisibility(View.VISIBLE);
                btnGoCreateEvent.setOnClickListener(v -> {
                    Intent intent = new Intent(EventListing.this, CreateEventActivity.class);
                    startActivity(intent);
                });
            }
        });
    }


}
