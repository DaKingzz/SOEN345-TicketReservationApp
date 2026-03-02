package com.soen345.ticketreservation.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseUser;
import com.soen345.ticketreservation.R;
import com.soen345.ticketreservation.auth.AuthManager;
import com.soen345.ticketreservation.auth.LoginActivity;

public class MainActivity extends AppCompatActivity {

    private TextView    welcomeText;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        authManager = AuthManager.getInstance();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        welcomeText = findViewById(R.id.welcome_text);

        loadUserGreeting();
        initGoToCreateEventListener();
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

    private void logout() {
        authManager.logout();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    public void initGoToCreateEventListener() {
        Button btnGoCreateEvent = findViewById(R.id.btnGoToCreateEvent);
        btnGoCreateEvent.setVisibility(View.GONE);

        authManager.checkAdminStatus(isAdmin -> {
            if (isAdmin) {
                btnGoCreateEvent.setVisibility(View.VISIBLE);
                btnGoCreateEvent.setOnClickListener(v -> {
                    Intent intent = new Intent(MainActivity.this, CreateEventActivity.class);
                    startActivity(intent);
                });
            }
        });
    }


}
