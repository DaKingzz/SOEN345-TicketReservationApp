package com.soen345.ticketreservation.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseUser;
import com.soen345.ticketreservation.R;
import com.soen345.ticketreservation.auth.AuthManager;

public class MainActivity extends BaseActivity {

    private TextView    welcomeText;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        authManager = AuthManager.getInstance();

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
