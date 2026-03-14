package com.soen345.ticketreservation.activity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.soen345.ticketreservation.R;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class CreateEventActivityTest {

    @Rule
    public ActivityScenarioRule<CreateEventActivity> activityRule =
            new ActivityScenarioRule<>(CreateEventActivity.class);

    // ── Initial state ─────────────────────────────────────────────────────────

    @Test
    public void allUIElementsAreVisible() {
        activityRule.getScenario().onActivity(activity -> {
            assertEquals(View.VISIBLE, activity.findViewById(R.id.tvAdminHeader).getVisibility());
            assertEquals(View.VISIBLE, activity.findViewById(R.id.etEventName).getVisibility());
            assertEquals(View.VISIBLE, activity.findViewById(R.id.etCategory).getVisibility());
            assertEquals(View.VISIBLE, activity.findViewById(R.id.etLocation).getVisibility());
            assertEquals(View.VISIBLE, activity.findViewById(R.id.etEventDate).getVisibility());
            assertEquals(View.VISIBLE, activity.findViewById(R.id.etEventTime).getVisibility());
            assertEquals(View.VISIBLE, activity.findViewById(R.id.etCapacity).getVisibility());
            assertEquals(View.VISIBLE, activity.findViewById(R.id.btnCreateEvent).getVisibility());
        });
    }

    // ── Field input ───────────────────────────────────────────────────────────

    @Test
    public void textFieldsAcceptInput() {
        activityRule.getScenario().onActivity(activity -> {
            ((EditText) activity.findViewById(R.id.etEventName)).setText("Sample Event");
            ((EditText) activity.findViewById(R.id.etLocation)).setText("Montreal");
            ((EditText) activity.findViewById(R.id.etCapacity)).setText("50");

            assertEquals("Sample Event", ((EditText) activity.findViewById(R.id.etEventName)).getText().toString());
            assertEquals("Montreal",     ((EditText) activity.findViewById(R.id.etLocation)).getText().toString());
            assertEquals("50",           ((EditText) activity.findViewById(R.id.etCapacity)).getText().toString());
        });
    }

    // ── Category dropdown ─────────────────────────────────────────────────────

    @Test
    public void categoryDropdownAdapterIsPopulated() {
        activityRule.getScenario().onActivity(activity -> {
            AutoCompleteTextView etCategory = activity.findViewById(R.id.etCategory);
            assertNotNull(etCategory.getAdapter());
            assertTrue(etCategory.getAdapter().getCount() > 0);
        });
    }

    @Test
    public void selectingCategoryFromDropdown_setsText() {
        activityRule.getScenario().onActivity(activity -> {
            AutoCompleteTextView etCategory = activity.findViewById(R.id.etCategory);
            // Simulate selecting by setting the text directly (as the adapter would)
            etCategory.setText("Concert", false);
            assertEquals("Concert", etCategory.getText().toString());
        });
    }

    // ── Date / time pickers ───────────────────────────────────────────────────

    @Test
    public void clickingEventDateField_opensMaterialDatePicker() {
        activityRule.getScenario().onActivity(activity -> {
            activity.findViewById(R.id.etEventDate).performClick();
            activity.getSupportFragmentManager().executePendingTransactions();
            assertNotNull(activity.getSupportFragmentManager().findFragmentByTag("DATE_PICKER"));
        });
    }

    @Test
    public void clickingEventTimeField_opensMaterialTimePicker() {
        activityRule.getScenario().onActivity(activity -> {
            activity.findViewById(R.id.etEventTime).performClick();
            activity.getSupportFragmentManager().executePendingTransactions();
            assertNotNull(activity.getSupportFragmentManager().findFragmentByTag("TIME_PICKER"));
        });
    }

    // ── Validation ────────────────────────────────────────────────────────────

    @Test
    public void submitWithAllFieldsEmpty_activityRemainsOnScreen() {
        activityRule.getScenario().onActivity(activity -> {
            activity.findViewById(R.id.btnCreateEvent).performClick();
            // Toast is shown and activity stays — admin header must still be visible
            assertEquals(View.VISIBLE, activity.findViewById(R.id.tvAdminHeader).getVisibility());
        });
    }
}
