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
import com.soen345.ticketreservation.auth.LoginActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class LoginActivityTest {

    @Rule
    public ActivityScenarioRule<LoginActivity> activityRule =
            new ActivityScenarioRule<>(LoginActivity.class);

    // ── Initial state ─────────────────────────────────────────────────────────

    @Test
    public void loginButtonIsDisplayedByDefault() {
        activityRule.getScenario().onActivity(activity -> {
            Button loginButton = activity.findViewById(R.id.login_button);
            assertEquals(View.VISIBLE, loginButton.getVisibility());
        });
    }

    @Test
    public void emailFieldIsDisplayedByDefault() {
        activityRule.getScenario().onActivity(activity -> {
            assertEquals(View.VISIBLE, activity.findViewById(R.id.email_layout).getVisibility());
        });
    }

    @Test
    public void passwordFieldIsDisplayedByDefault() {
        activityRule.getScenario().onActivity(activity -> {
            assertEquals(View.VISIBLE, activity.findViewById(R.id.password_layout).getVisibility());
        });
    }

    @Test
    public void registerLinkIsDisplayedByDefault() {
        activityRule.getScenario().onActivity(activity -> {
            assertEquals(View.VISIBLE, activity.findViewById(R.id.register_link).getVisibility());
        });
    }

    @Test
    public void forgotPasswordLinkIsDisplayedByDefault() {
        activityRule.getScenario().onActivity(activity -> {
            assertEquals(View.VISIBLE, activity.findViewById(R.id.forgot_password_text).getVisibility());
        });
    }

    // ── Form validation ───────────────────────────────────────────────────────

    @Test
    public void submitWithEmptyFields_showsEmailError() {
        activityRule.getScenario().onActivity(activity -> {
            activity.findViewById(R.id.login_button).performClick();

            TextInputLayout emailLayout = activity.findViewById(R.id.email_layout);
            assertNotNull(emailLayout.getError());
            assertEquals("Enter a valid email address", emailLayout.getError().toString());
        });
    }

    @Test
    public void submitWithInvalidEmail_showsEmailError() {
        activityRule.getScenario().onActivity(activity -> {
            ((TextInputEditText) activity.findViewById(R.id.email_edit_text)).setText("not-an-email");
            ((TextInputEditText) activity.findViewById(R.id.password_edit_text)).setText("password123");

            activity.findViewById(R.id.login_button).performClick();

            TextInputLayout emailLayout = activity.findViewById(R.id.email_layout);
            assertNotNull(emailLayout.getError());
            assertEquals("Enter a valid email address", emailLayout.getError().toString());
        });
    }

    @Test
    public void submitWithValidEmailButShortPassword_showsPasswordError() {
        activityRule.getScenario().onActivity(activity -> {
            ((TextInputEditText) activity.findViewById(R.id.email_edit_text)).setText("alice@example.com");
            ((TextInputEditText) activity.findViewById(R.id.password_edit_text)).setText("123");

            activity.findViewById(R.id.login_button).performClick();

            TextInputLayout passwordLayout = activity.findViewById(R.id.password_layout);
            assertNotNull(passwordLayout.getError());
            assertEquals("Password must be at least 6 characters", passwordLayout.getError().toString());
        });
    }

    @Test
    public void emailErrorClearedWhenValidEmailProvided() {
        activityRule.getScenario().onActivity(activity -> {
            // Trigger email error with empty fields
            activity.findViewById(R.id.login_button).performClick();
            TextInputLayout emailLayout = activity.findViewById(R.id.email_layout);
            assertNotNull(emailLayout.getError());

            // Provide valid email but short password — validation fails before Firebase is called,
            // so the activity never navigates away
            ((TextInputEditText) activity.findViewById(R.id.email_edit_text)).setText("alice@example.com");
            ((TextInputEditText) activity.findViewById(R.id.password_edit_text)).setText("123");
            activity.findViewById(R.id.login_button).performClick();

            assertNull(emailLayout.getError());
            assertNotNull(((TextInputLayout) activity.findViewById(R.id.password_layout)).getError());
        });
    }
}
