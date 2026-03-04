package com.soen345.ticketreservation.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.soen345.ticketreservation.R;
import com.soen345.ticketreservation.auth.AuthManager;
import com.soen345.ticketreservation.auth.LoginActivity;

public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DURATION_MS = 1_200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler(Looper.getMainLooper())
                .postDelayed(this::routeUser, SPLASH_DURATION_MS);
    }

    private void routeUser() {
        Class<?> destination = AuthManager.getInstance().isLoggedIn()
                ? EventListingActivity.class
                : LoginActivity.class;

        startActivity(new Intent(this, destination));
        finish();
    }
}
