package com.soen345.ticketreservation.activity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import android.view.View;
import android.widget.Button;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.soen345.ticketreservation.R;
import com.soen345.ticketreservation.auth.RegisterActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(AndroidJUnit4.class)
public class RegisterActivityTest {

    @Rule
    public ActivityScenarioRule<RegisterActivity> activityRule =
            new ActivityScenarioRule<>(RegisterActivity.class);

    // ── Initial state ─────────────────────────────────────────────────────────

    @Test
    public void emailSectionIsDisplayedByDefault() {
        activityRule.getScenario().onActivity(activity -> {
            View emailSection = activity.findViewById(R.id.email_section);
            assertEquals(View.VISIBLE, emailSection.getVisibility());
        });
    }

    @Test
    public void registerButtonIsDisplayedByDefault() {
        activityRule.getScenario().onActivity(activity -> {
            Button actionButton = activity.findViewById(R.id.action_button);
            assertEquals(View.VISIBLE, actionButton.getVisibility());
            assertEquals(activity.getString(R.string.btn_register), actionButton.getText().toString());
        });
    }

    // ── Email form validation (InputValidator + RegisterActivity integration) ──

    @Test
    public void submitEmailFormWithAllFieldsEmpty_showsNameError() {
        activityRule.getScenario().onActivity(activity -> {
            activity.findViewById(R.id.action_button).performClick();

            TextInputLayout nameLayout = activity.findViewById(R.id.name_layout);
            assertNotNull(nameLayout.getError());
            assertEquals("Name is required", nameLayout.getError().toString());
        });
    }

    @Test
    public void submitEmailFormWithInvalidEmail_showsEmailError() {
        activityRule.getScenario().onActivity(activity -> {
            ((TextInputEditText) activity.findViewById(R.id.name_edit_text)).setText("Alice");
            ((TextInputEditText) activity.findViewById(R.id.email_edit_text)).setText("not-an-email");
            ((TextInputEditText) activity.findViewById(R.id.password_edit_text)).setText("password123");
            ((TextInputEditText) activity.findViewById(R.id.confirm_password_edit_text)).setText("password123");

            activity.findViewById(R.id.action_button).performClick();

            TextInputLayout emailLayout = activity.findViewById(R.id.email_layout);
            assertNotNull(emailLayout.getError());
            assertEquals("Enter a valid email address", emailLayout.getError().toString());
        });
    }

    @Test
    public void submitEmailFormWithShortPassword_showsPasswordError() {
        activityRule.getScenario().onActivity(activity -> {
            ((TextInputEditText) activity.findViewById(R.id.name_edit_text)).setText("Alice");
            ((TextInputEditText) activity.findViewById(R.id.email_edit_text)).setText("alice@example.com");
            ((TextInputEditText) activity.findViewById(R.id.password_edit_text)).setText("123");
            ((TextInputEditText) activity.findViewById(R.id.confirm_password_edit_text)).setText("123");

            activity.findViewById(R.id.action_button).performClick();

            TextInputLayout passwordLayout = activity.findViewById(R.id.password_layout);
            assertNotNull(passwordLayout.getError());
            assertEquals("Password must be at least 6 characters", passwordLayout.getError().toString());
        });
    }

    @Test
    public void submitEmailFormWithMismatchedPasswords_showsConfirmPasswordError() {
        activityRule.getScenario().onActivity(activity -> {
            ((TextInputEditText) activity.findViewById(R.id.name_edit_text)).setText("Alice");
            ((TextInputEditText) activity.findViewById(R.id.email_edit_text)).setText("alice@example.com");
            ((TextInputEditText) activity.findViewById(R.id.password_edit_text)).setText("password123");
            ((TextInputEditText) activity.findViewById(R.id.confirm_password_edit_text)).setText("different456");

            activity.findViewById(R.id.action_button).performClick();

            TextInputLayout confirmLayout = activity.findViewById(R.id.confirm_password_layout);
            assertNotNull(confirmLayout.getError());
            assertEquals("Passwords do not match", confirmLayout.getError().toString());
        });
    }

    // ── Error cleared on valid input ──────────────────────────────────────────

    @Test
    public void nameErrorClearedWhenValidNameProvided() {
        activityRule.getScenario().onActivity(activity -> {
            // First trigger the name error with empty fields
            activity.findViewById(R.id.action_button).performClick();
            TextInputLayout nameLayout = activity.findViewById(R.id.name_layout);
            assertNotNull(nameLayout.getError());

            // Fix name but use invalid email — validation fails before Firebase is called,
            // so the activity never navigates away and cannot destroy the next test's activity
            ((TextInputEditText) activity.findViewById(R.id.name_edit_text)).setText("Alice");
            ((TextInputEditText) activity.findViewById(R.id.email_edit_text)).setText("not-an-email");
            activity.findViewById(R.id.action_button).performClick();

            assertNull(nameLayout.getError());
            assertNotNull(((TextInputLayout) activity.findViewById(R.id.email_layout)).getError());
        });
    }
}