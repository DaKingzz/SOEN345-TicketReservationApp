package com.soen345.ticketreservation.activity;

import android.os.Bundle;

import com.soen345.ticketreservation.R;

public class ProfileActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
    }

    @Override
    protected int getSelectedBottomNavItem() {
        return R.id.nav_profile;
    }
}