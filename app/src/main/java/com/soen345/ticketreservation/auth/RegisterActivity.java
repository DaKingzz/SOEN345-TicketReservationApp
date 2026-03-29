package com.soen345.ticketreservation.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.soen345.ticketreservation.activity.EventListingActivity;
import com.soen345.ticketreservation.R;
import com.soen345.ticketreservation.util.InputValidator;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout   nameLayout;
    private TextInputEditText nameEditText;
    private ProgressBar       progressBar;
    private Button            actionButton;
    private TextView          loginLink;

    private LinearLayout      emailSection;
    private TextInputLayout   emailLayout;
    private TextInputLayout   passwordLayout;
    private TextInputLayout   confirmPasswordLayout;
    private TextInputEditText emailEditText;
    private TextInputEditText passwordEditText;
    private TextInputEditText confirmPasswordEditText;

    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        authManager = AuthManager.getInstance();

        bindViews();
        setupListeners();
    }

    private void bindViews() {
        nameLayout            = findViewById(R.id.name_layout);
        nameEditText          = findViewById(R.id.name_edit_text);
        progressBar           = findViewById(R.id.progress_bar);
        actionButton          = findViewById(R.id.action_button);
        loginLink             = findViewById(R.id.login_link);

        emailSection          = findViewById(R.id.email_section);
        emailLayout           = findViewById(R.id.email_layout);
        passwordLayout        = findViewById(R.id.password_layout);
        confirmPasswordLayout = findViewById(R.id.confirm_password_layout);
        emailEditText         = findViewById(R.id.email_edit_text);
        passwordEditText      = findViewById(R.id.password_edit_text);
        confirmPasswordEditText = findViewById(R.id.confirm_password_edit_text);
    }

    private void setupListeners() {
        actionButton.setOnClickListener(v -> {
            attemptEmailRegister();
        });

        loginLink.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void attemptEmailRegister() {
        String name            = text(nameEditText);
        String email           = text(emailEditText);
        String password        = text(passwordEditText);
        String confirmPassword = text(confirmPasswordEditText);

        if (!validateEmailForm(name, email, password, confirmPassword)) return;

        setLoading(true);
        authManager.registerWithEmail(email, password, name, new AuthCallback() {
            @Override
            public void onSuccess() {
                setLoading(false);
                Toast.makeText(RegisterActivity.this,
                        "Account created successfully!", Toast.LENGTH_SHORT).show();
                navigateToMain();
            }

            @Override
            public void onFailure(String errorMessage) {
                setLoading(false);
                Toast.makeText(RegisterActivity.this,
                        "Registration failed: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private boolean validateEmailForm(String name, String email,
                                      String password, String confirmPassword) {
        boolean valid = true;

        if (!InputValidator.isValidName(name)) {
            nameLayout.setError("Name is required");
            valid = false;
        } else {
            nameLayout.setError(null);
        }

        if (!InputValidator.isValidEmail(email)) {
            emailLayout.setError("Enter a valid email address");
            valid = false;
        } else {
            emailLayout.setError(null);
        }

        if (!InputValidator.isValidPassword(password)) {
            passwordLayout.setError("Password must be at least 6 characters");
            valid = false;
        } else {
            passwordLayout.setError(null);
        }

        if (!InputValidator.passwordsMatch(password, confirmPassword)) {
            confirmPasswordLayout.setError("Passwords do not match");
            valid = false;
        } else {
            confirmPasswordLayout.setError(null);
        }

        return valid;
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        actionButton.setEnabled(!loading);
    }

    private void clearAllErrors() {
        nameLayout.setError(null);
        emailLayout.setError(null);
        passwordLayout.setError(null);
        confirmPasswordLayout.setError(null);
    }

    private void navigateToMain() {
        Intent intent = new Intent(this, EventListingActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private static String text(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }
}