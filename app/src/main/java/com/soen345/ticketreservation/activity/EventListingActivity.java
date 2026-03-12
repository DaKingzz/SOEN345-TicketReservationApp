package com.soen345.ticketreservation.activity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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
import com.soen345.ticketreservation.event.ReservationManager;
import com.soen345.ticketreservation.model.Event;
import com.soen345.ticketreservation.model.EventCategory;
import com.soen345.ticketreservation.model.OnEventInteractionListener;
import com.soen345.ticketreservation.model.Reservation;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class EventListingActivity extends BaseActivity {

    private TextView welcomeText;
    private AuthManager authManager;
    private EventManager eventManager;
    private ReservationManager reservationManager;
    private RecyclerView rvEvents;
    private EventAdapter eventAdapter;
    private List<Event> allEvents = new ArrayList<>();
    private List<Event> filteredEvents = new ArrayList<>();
    private ListenerRegistration eventListener;
    private ChipGroup chipGroupFilters;
    
    private EditText etLocationFilter;
    private Button btnDateFilter;
    private ImageButton btnClearDate;
    private Calendar selectedCalendar = null;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_listing);

        authManager = AuthManager.getInstance();
        eventManager = EventManager.getInstance();
        reservationManager = ReservationManager.getInstance();

        welcomeText = findViewById(R.id.welcome_text);
        rvEvents = findViewById(R.id.rvEvents);
        chipGroupFilters = findViewById(R.id.chipGroupFilters);
        etLocationFilter = findViewById(R.id.etLocationFilter);
        btnDateFilter = findViewById(R.id.btnDateFilter);
        btnClearDate = findViewById(R.id.btnClearDate);

        setupRecyclerView();
        setupFilterChips();
        setupLocationFilter();
        setupDateFilter();
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
                        () -> Toast.makeText(EventListingActivity.this, "Event Deleted", Toast.LENGTH_SHORT).show(),
                        e -> Toast.makeText(EventListingActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onEditClick(Event event, int position) {
                Intent intent = new Intent(EventListingActivity.this, CreateEventActivity.class);
                intent.putExtra("EVENT_TO_EDIT", event);
                startActivity(intent);
            }

            @Override
            public void onBookClick(Event event, int position) {
                setReservationQuantity(event);
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

    private void setupLocationFilter() {
        etLocationFilter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterEvents();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupDateFilter() {
        btnDateFilter.setOnClickListener(v -> {
            Calendar calendar = selectedCalendar != null ? selectedCalendar : Calendar.getInstance();
            new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                selectedCalendar = Calendar.getInstance();
                selectedCalendar.set(year, month, dayOfMonth);
                btnDateFilter.setText(dateFormat.format(selectedCalendar.getTime()));
                btnClearDate.setVisibility(View.VISIBLE);
                filterEvents();
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        btnClearDate.setOnClickListener(v -> {
            selectedCalendar = null;
            btnDateFilter.setText("Date");
            btnClearDate.setVisibility(View.GONE);
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

        String locationQuery = etLocationFilter.getText().toString().toLowerCase().trim();
        String finalSelectedCategory = selectedCategory;

        filteredEvents.clear();
        filteredEvents.addAll(allEvents.stream()
                .filter(e -> {
                    // Category Filter
                    boolean categoryMatch = finalSelectedCategory.isEmpty() || 
                            (e.getCategory() != null && e.getCategory().equalsIgnoreCase(finalSelectedCategory));
                    
                    // Location Filter
                    boolean locationMatch = locationQuery.isEmpty() || 
                            (e.getLocation() != null && e.getLocation().toLowerCase().contains(locationQuery));
                    
                    // Date Filter
                    boolean dateMatch = true;
                    if (selectedCalendar != null && e.getDateTime() != null) {
                        Calendar eventCal = Calendar.getInstance();
                        eventCal.setTime(e.getDateTime());
                        dateMatch = eventCal.get(Calendar.YEAR) == selectedCalendar.get(Calendar.YEAR) &&
                                eventCal.get(Calendar.DAY_OF_YEAR) == selectedCalendar.get(Calendar.DAY_OF_YEAR);
                    }
                    
                    return categoryMatch && locationMatch && dateMatch;
                })
                .collect(Collectors.toList()));

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
                    Intent intent = new Intent(EventListingActivity.this, CreateEventActivity.class);
                    startActivity(intent);
                });
            }
        });
    }

    private void setReservationQuantity(Event event) {
        Log.d("EventListingActivity", "onBookClick called with position: " + event.getName());
        AlertDialog.Builder builder = new AlertDialog.Builder(EventListingActivity.this);
        builder.setTitle("How many tickets do you want to book?");
        builder.setMessage("Input amount of tickets");
        final EditText input = new EditText(EventListingActivity.this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                int value = Integer.parseInt(input.getText().toString());
                Log.d("EventListingActivity", "onBookClick called with value: " + value);
                Reservation reservation = new Reservation();
                reservation.setEventId(event.getEventId());
                reservation.setQuantity(value);
                reservation.setUserId(authManager.getCurrentUser().getUid());
                reservation.setEventDate(event.getDateTime());
                confirmReservation(reservation, event);
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.cancel();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void confirmReservation(Reservation reservation, Event event) {
        AlertDialog.Builder builder = new AlertDialog.Builder(EventListingActivity.this);
        builder.setTitle("Confirm Reservation");
        builder.setMessage("Reservation Summary");
        final TextView tvReservationSummary = new TextView(EventListingActivity.this);
        tvReservationSummary.setText(getReservationPreview(reservation, event));
        builder.setView(tvReservationSummary);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Log.d("EventListingActivity", "onBookClick called with value: " + reservation.getQuantity());
                        reservationManager.createReservation(reservation, event, EventListingActivity.this);
                    }
                });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.cancel();
                    }
                });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private String getReservationPreview(Reservation reservation, Event event) {
        return "Event: " + event.getName() + "\n" +
                "Date: " + dateFormat.format(event.getDateTime()) + "\n" +
                "Quantity: " + reservation.getQuantity();
    }
}
