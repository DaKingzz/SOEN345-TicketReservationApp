package com.soen345.ticketreservation.activity;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.soen345.ticketreservation.R;
import com.soen345.ticketreservation.event.EventManager;
import com.soen345.ticketreservation.model.Event;
import com.soen345.ticketreservation.model.EventCategory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CreateEventActivity extends BaseActivity {

    private Event eventToEdit;
    private boolean isEditMode = false;
    private SimpleDateFormat dateDisplayFormat = new SimpleDateFormat("MMM d, yyyy", Locale.US);
    private SimpleDateFormat timeDisplayFormat = new SimpleDateFormat("HH:mm", Locale.US);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        eventToEdit = (Event) getIntent().getSerializableExtra("EVENT_TO_EDIT");
        if (eventToEdit != null) {
            isEditMode = true;
        }

        initCategoryDropdown();
        setupUI();
        createEventListener();
        createDateListener();
        createTimeListener();
    }

    @Override
    protected int getSelectedBottomNavItem() {
        return 0;
    }

    private void setupUI() {
        if (isEditMode) {
            ((EditText) findViewById(R.id.etEventName)).setText(eventToEdit.getName());
            ((AutoCompleteTextView) findViewById(R.id.etCategory)).setText(eventToEdit.getCategory(), false);
            ((EditText) findViewById(R.id.etLocation)).setText(eventToEdit.getLocation());
            ((EditText) findViewById(R.id.etCapacity)).setText(String.valueOf(eventToEdit.getTotalCapacity()));

            if (eventToEdit.getDateTime() != null) {
                ((EditText) findViewById(R.id.etEventDate)).setText(dateDisplayFormat.format(eventToEdit.getDateTime()));
                ((EditText) findViewById(R.id.etEventTime)).setText(timeDisplayFormat.format(eventToEdit.getDateTime()));
            }

            Button btnCreateEvent = findViewById(R.id.btnCreateEvent);
            btnCreateEvent.setText("Confirm");
        }
    }

    public void createEventListener() {
        Button btnCreateEvent = findViewById(R.id.btnCreateEvent);

        btnCreateEvent.setOnClickListener(v -> {
            String eventName = ((EditText) findViewById(R.id.etEventName)).getText().toString();
            String category = ((EditText) findViewById(R.id.etCategory)).getText().toString();
            String location = ((EditText) findViewById(R.id.etLocation)).getText().toString();
            String eventDate = ((EditText) findViewById(R.id.etEventDate)).getText().toString();
            String eventTime = ((EditText) findViewById(R.id.etEventTime)).getText().toString();
            String capacityStr = ((EditText) findViewById(R.id.etCapacity)).getText().toString();

            if (eventName.isEmpty() || category.isEmpty() || location.isEmpty() ||
                    eventDate.isEmpty() || eventTime.isEmpty() || capacityStr.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            String combinedDateTime = eventDate + " " + eventTime;
            SimpleDateFormat parser = new SimpleDateFormat("MMM d, yyyy HH:mm", Locale.US);

            try {
                Date dateObject = parser.parse(combinedDateTime);
                int capacity = Integer.parseInt(capacityStr);

                if (isEditMode) {
                    eventToEdit.setName(eventName);
                    eventToEdit.setCategory(category);
                    eventToEdit.setLocation(location);
                    eventToEdit.setDateTime(dateObject);
                    eventToEdit.setTotalCapacity(capacity);
                    // Update available seats if needed, or leave logic to business rules
                    eventToEdit.setAvailableSeats(capacity);

                    EventManager.getInstance().updateEvent(eventToEdit,
                            () -> {
                                Toast.makeText(this, "Event updated successfully", Toast.LENGTH_SHORT).show();
                                finish();
                            },
                            e -> Toast.makeText(this, "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
                } else {
                    Event event = new Event(
                            null,
                            eventName,
                            category,
                            dateObject,
                            location,
                            capacity
                    );

                    EventManager.getInstance().createEvent(event);
                    Toast.makeText(this, "Event created successfully", Toast.LENGTH_SHORT).show();
                    finish();
                }

            } catch (ParseException e) {
                Log.e("Validation", "Date/Time parsing failed", e);
                Toast.makeText(this, "Invalid Date/Time format", Toast.LENGTH_SHORT).show();
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid capacity value", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initCategoryDropdown(){
        EventCategory[] categories = EventCategory.values();
        String[] categoryStrings = new String[categories.length];

        for (int i = 0; i < categories.length; i++) {
            categoryStrings[i] = categories[i].toString();
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                categoryStrings
        );

        AutoCompleteTextView etCategory = findViewById(R.id.etCategory);
        etCategory.setAdapter(adapter);
    }

    private void createDateListener() {
        EditText etEventDate = findViewById(R.id.etEventDate);

        etEventDate.setOnClickListener(v -> {
            MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Select Event Date")
                    .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                    .build();

            datePicker.show(getSupportFragmentManager(), "DATE_PICKER");

            datePicker.addOnPositiveButtonClickListener(selection -> {
                etEventDate.setText(dateDisplayFormat.format(new Date(selection)));
            });
        });
    }

    private void createTimeListener() {
        EditText etEventTime = findViewById(R.id.etEventTime);

        etEventTime.setOnClickListener(v -> {
            MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
                    .setTimeFormat(TimeFormat.CLOCK_24H)
                    .setHour(12)
                    .setMinute(0)
                    .setTitleText("Select Event Time")
                    .build();

            timePicker.show(getSupportFragmentManager(), "TIME_PICKER");

            timePicker.addOnPositiveButtonClickListener(view -> {
                String formattedTime = String.format(Locale.US, "%02d:%02d", timePicker.getHour(), timePicker.getMinute());
                etEventTime.setText(formattedTime);
            });
        });
    }

}