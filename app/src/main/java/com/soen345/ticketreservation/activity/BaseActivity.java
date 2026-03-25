package com.soen345.ticketreservation.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.LayoutRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.soen345.ticketreservation.R;
import com.soen345.ticketreservation.auth.AuthManager;
import com.soen345.ticketreservation.auth.LoginActivity;

public abstract class BaseActivity extends AppCompatActivity {

    protected Toolbar toolbar;
    protected BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        View baseView = getLayoutInflater().inflate(R.layout.activity_base, null);
        FrameLayout activityContainer = baseView.findViewById(R.id.activity_content);
        getLayoutInflater().inflate(layoutResID, activityContainer, true);
        super.setContentView(baseView);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        setupBottomNavigation();

        int selectedItemId = getSelectedBottomNavItem();
        if (selectedItemId != 0) {
            bottomNavigationView.setSelectedItemId(selectedItemId);
        }
    }

    protected abstract int getSelectedBottomNavItem();

    private void setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == getSelectedBottomNavItem()) {
                return true;
            }

            if (itemId == R.id.nav_home) {
                startActivity(new Intent(this, EventListingActivity.class));
                finish();
                return true;

            } else if (itemId == R.id.nav_my_tickets) {
                startActivity(new Intent(this, ReservationListingActivity.class));
                finish();
                return true;

            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                finish();
                return true;
            }

            return false;
        });
    }

    protected void logout() {
        AuthManager.getInstance().logout();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}