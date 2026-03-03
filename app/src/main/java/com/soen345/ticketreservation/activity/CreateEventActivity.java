package com.soen345.ticketreservation.activity;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);
        initCategoryDropdown();
        createEventListener();
        createDateListener();
        createTimeListener();
    }

    public void createEventListener() {
        Button btnCreateEvent = findViewById(R.id.btnCreateEvent);

        btnCreateEvent.setOnClickListener(v -> {
            String eventName = ((EditText)findViewById(R.id.etEventName)).getText().toString();
            String category = ((EditText)findViewById(R.id.etCategory)).getText().toString();
            String location = ((EditText)findViewById(R.id.etLocation)).getText().toString();
            String eventDate = ((EditText)findViewById(R.id.etEventDate)).getText().toString();
            String eventTime = ((EditText)findViewById(R.id.etEventTime)).getText().toString();
            String capacity = ((EditText)findViewById(R.id.etCapacity)).getText().toString();

            if (eventName.isEmpty() || category.isEmpty() || location.isEmpty() ||
                    eventDate.isEmpty() || eventTime.isEmpty() || capacity.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            String combinedDateTime = eventDate + " " + eventTime;
            SimpleDateFormat parser = new SimpleDateFormat("MMM d, yyyy HH:mm", Locale.US);

            try {
                Date dateObject = parser.parse(combinedDateTime);

                Event event = new Event(
                        null,
                        eventName,
                        category,
                        dateObject,
                        location,
                        Integer.parseInt(capacity)
                );

                EventManager.getInstance().createEvent(event);
                Toast.makeText(this, "Event created successfully", Toast.LENGTH_SHORT).show();
                finish();

            } catch (ParseException e) {
                Log.e("Validation", "Date/Time parsing failed", e);
                Toast.makeText(this, "Invalid Date/Time format", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void initCategoryDropdown(){
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

    public void createDateListener() {
        EditText etEventDateTime = findViewById(R.id.etEventDate);

        etEventDateTime.setOnClickListener(v -> {
            MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Select Event Date")
                    .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                    .build();

            datePicker.show(getSupportFragmentManager(), "DATE_PICKER");

            datePicker.addOnPositiveButtonClickListener(selection -> {
                etEventDateTime.setText(datePicker.getHeaderText());
            });
        });
    }
    public void createTimeListener(){
        EditText etEventTime = findViewById(R.id.etEventTime);

        etEventTime.setOnClickListener(v -> {
            MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
                    .setTimeFormat(TimeFormat.CLOCK_12H)
                    .setHour(12)
                    .setMinute(0)
                    .setTitleText("Select Event Time")
                    .build();

            timePicker.show(getSupportFragmentManager(), "TIME_PICKER");

            timePicker.addOnPositiveButtonClickListener(view -> {
                String formattedTime = String.format("%02d:%02d", timePicker.getHour(), timePicker.getMinute());
                etEventTime.setText(formattedTime);
            });
        });
    }

}
