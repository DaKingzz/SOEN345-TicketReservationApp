package com.soen345.ticketreservation.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.soen345.ticketreservation.MainActivity;
import com.soen345.ticketreservation.R;
import com.soen345.ticketreservation.util.InputValidator;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout   emailLayout;
    private TextInputLayout   passwordLayout;
    private TextInputEditText emailEditText;
    private TextInputEditText passwordEditText;
    private Button            loginButton;
    private TextView          forgotPasswordText;
    private TextView          registerLink;
    private ProgressBar       progressBar;

    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        authManager = AuthManager.getInstance();

        bindViews();
        setupListeners();
    }

    private void bindViews() {
        emailLayout        = findViewById(R.id.email_layout);
        passwordLayout     = findViewById(R.id.password_layout);
        emailEditText      = findViewById(R.id.email_edit_text);
        passwordEditText   = findViewById(R.id.password_edit_text);
        loginButton        = findViewById(R.id.login_button);
        forgotPasswordText = findViewById(R.id.forgot_password_text);
        registerLink       = findViewById(R.id.register_link);
        progressBar        = findViewById(R.id.progress_bar);
    }

    private void setupListeners() {
        loginButton.setOnClickListener(v -> attemptLogin());
        forgotPasswordText.setOnClickListener(v -> showForgotPasswordDialog());
        registerLink.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void attemptLogin() {
        String email    = text(emailEditText);
        String password = text(passwordEditText);

        if (!validate(email, password)) return;

        setLoading(true);
        authManager.loginWithEmail(email, password, new AuthCallback() {
            @Override
            public void onSuccess() {
                setLoading(false);
                navigateToMain();
            }

            @Override
            public void onFailure(String errorMessage) {
                setLoading(false);
                Toast.makeText(LoginActivity.this,
                        "Login failed: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private boolean validate(String email, String password) {
        boolean valid = true;

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

        return valid;
    }

    private void showForgotPasswordDialog() {
        TextInputEditText emailInput = new TextInputEditText(this);
        emailInput.setHint("Your email address");
        emailInput.setPadding(48, 24, 48, 24);

        String current = text(emailEditText);
        if (!current.isEmpty()) emailInput.setText(current);

        new AlertDialog.Builder(this)
                .setTitle("Reset Password")
                .setMessage("We'll send a reset link to your email.")
                .setView(emailInput)
                .setPositiveButton("Send", (dialog, which) -> {
                    String email = emailInput.getText() != null
                            ? emailInput.getText().toString().trim() : "";
                    if (InputValidator.isValidEmail(email)) {
                        sendPasswordReset(email);
                    } else {
                        Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void sendPasswordReset(String email) {
        authManager.sendPasswordResetEmail(email, new AuthCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(LoginActivity.this,
                        "Reset link sent to " + email, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(LoginActivity.this,
                        "Could not send reset email: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        loginButton.setEnabled(!loading);
        emailEditText.setEnabled(!loading);
        passwordEditText.setEnabled(!loading);
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
