package com.soen345.ticketreservation.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.soen345.ticketreservation.activity.MainActivity;
import com.soen345.ticketreservation.R;
import com.soen345.ticketreservation.util.InputValidator;

public class RegisterActivity extends AppCompatActivity {

    private TabLayout         tabLayout;
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

    private LinearLayout      phoneSection;
    private TextInputLayout   phoneLayout;
    private TextInputEditText phoneEditText;

    private LinearLayout      otpSection;
    private TextInputLayout   otpLayout;
    private TextInputEditText otpEditText;

    private AuthManager authManager;
    private String      verificationId;
    private boolean     isEmailMode = true;
    private boolean     otpSent     = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        authManager = AuthManager.getInstance();

        bindViews();
        setupTabs();
        setupListeners();
    }

    private void bindViews() {
        tabLayout             = findViewById(R.id.tab_layout);
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

        phoneSection  = findViewById(R.id.phone_section);
        phoneLayout   = findViewById(R.id.phone_layout);
        phoneEditText = findViewById(R.id.phone_edit_text);

        otpSection  = findViewById(R.id.otp_section);
        otpLayout   = findViewById(R.id.otp_layout);
        otpEditText = findViewById(R.id.otp_edit_text);
    }

    private void setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("Email"));
        tabLayout.addTab(tabLayout.newTab().setText("Phone"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                isEmailMode = (tab.getPosition() == 0);
                switchMode();
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupListeners() {
        actionButton.setOnClickListener(v -> {
            if (isEmailMode) {
                attemptEmailRegister();
            } else if (otpSent) {
                attemptOtpVerify();
            } else {
                sendOtp();
            }
        });

        loginLink.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void switchMode() {
        emailSection.setVisibility(isEmailMode ? View.VISIBLE : View.GONE);
        phoneSection.setVisibility(isEmailMode ? View.GONE   : View.VISIBLE);
        otpSection.setVisibility(View.GONE);
        otpSent = false;
        verificationId = null;
        updateActionButtonLabel();
        clearAllErrors();
    }

    private void updateActionButtonLabel() {
        if (isEmailMode) {
            actionButton.setText(R.string.btn_register);
        } else if (otpSent) {
            actionButton.setText(R.string.btn_verify_register);
        } else {
            actionButton.setText(R.string.btn_send_otp);
        }
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

    private void sendOtp() {
        String name  = text(nameEditText);
        String phone = text(phoneEditText);

        boolean valid = true;
        if (!InputValidator.isValidName(name)) {
            nameLayout.setError("Name is required");
            valid = false;
        } else {
            nameLayout.setError(null);
        }

        if (!InputValidator.isValidPhoneNumber(phone)) {
            phoneLayout.setError("Include country code, e.g. +15141234567");
            valid = false;
        } else {
            phoneLayout.setError(null);
        }

        if (!valid) return;

        setLoading(true);

        authManager.startPhoneVerification(phone, this,
                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                setLoading(false);
                authManager.signInWithPhoneCredential(credential, name, phone, new AuthCallback() {
                    @Override
                    public void onSuccess() { navigateToMain(); }

                    @Override
                    public void onFailure(String errorMessage) {
                        Toast.makeText(RegisterActivity.this,
                                "Auto-verification failed: " + errorMessage,
                                Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onVerificationFailed(@NonNull com.google.firebase.FirebaseException e) {
                setLoading(false);
                Toast.makeText(RegisterActivity.this,
                        "Could not send OTP: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCodeSent(@NonNull String vId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                setLoading(false);
                verificationId = vId;
                otpSent = true;
                otpSection.setVisibility(View.VISIBLE);
                phoneEditText.setEnabled(false);
                updateActionButtonLabel();
                Toast.makeText(RegisterActivity.this,
                        "OTP sent to " + phone, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void attemptOtpVerify() {
        String otp   = text(otpEditText);
        String name  = text(nameEditText);
        String phone = text(phoneEditText);

        if (!InputValidator.isValidOtp(otp)) {
            otpLayout.setError("Enter the 6-digit code");
            return;
        }
        otpLayout.setError(null);

        if (verificationId == null) {
            Toast.makeText(this, "Please request an OTP first", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);
        authManager.verifyOtpAndRegister(verificationId, otp, name, phone, new AuthCallback() {
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
                        "Verification failed: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        actionButton.setEnabled(!loading);
        tabLayout.setEnabled(!loading);
    }

    private void clearAllErrors() {
        nameLayout.setError(null);
        emailLayout.setError(null);
        passwordLayout.setError(null);
        confirmPasswordLayout.setError(null);
        phoneLayout.setError(null);
        otpLayout.setError(null);
    }

    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private static String text(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }
}
