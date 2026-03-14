package com.soen345.ticketreservation.activity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.firebase.auth.FirebaseAuth;
import com.soen345.ticketreservation.R;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
public class EventListingActivityTest {

    @BeforeClass
    public static void signIn() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        FirebaseAuth.getInstance()
                .signInWithEmailAndPassword("robertlouissoccer@outlook.com", "soccer")
                .addOnCompleteListener(task -> latch.countDown());
        latch.await(15, TimeUnit.SECONDS);
    }

    @AfterClass
    public static void signOut() {
        FirebaseAuth.getInstance().signOut();
    }

    @Rule
    public ActivityScenarioRule<EventListingActivity> activityRule =
            new ActivityScenarioRule<>(EventListingActivity.class);

    // ── Initial state ─────────────────────────────────────────────────────────

    @Test
    public void eventListRecyclerViewIsDisplayed() {
        activityRule.getScenario().onActivity(activity -> {
            assertEquals(View.VISIBLE, activity.findViewById(R.id.rvEvents).getVisibility());
        });
    }

    @Test
    public void chipGroupIsDisplayed() {
        activityRule.getScenario().onActivity(activity -> {
            assertEquals(View.VISIBLE, activity.findViewById(R.id.chipGroupFilters).getVisibility());
        });
    }

    @Test
    public void allCategoryChipsAreDisplayed() {
        activityRule.getScenario().onActivity(activity -> {
            assertEquals(View.VISIBLE, activity.findViewById(R.id.chipConcert).getVisibility());
            assertEquals(View.VISIBLE, activity.findViewById(R.id.chipMovie).getVisibility());
            assertEquals(View.VISIBLE, activity.findViewById(R.id.chipSport).getVisibility());
            assertEquals(View.VISIBLE, activity.findViewById(R.id.chipTravel).getVisibility());
        });
    }

    @Test
    public void locationFilterIsDisplayedAndEnabled() {
        activityRule.getScenario().onActivity(activity -> {
            EditText locationFilter = activity.findViewById(R.id.etLocationFilter);
            assertEquals(View.VISIBLE, locationFilter.getVisibility());
            assertTrue(locationFilter.isEnabled());
        });
    }

    @Test
    public void dateFilterButtonIsDisplayedAndEnabled() {
        activityRule.getScenario().onActivity(activity -> {
            Button btnDateFilter = activity.findViewById(R.id.btnDateFilter);
            assertEquals(View.VISIBLE, btnDateFilter.getVisibility());
            assertTrue(btnDateFilter.isEnabled());
        });
    }

    // ── Category chip interaction ─────────────────────────────────────────────

    @Test
    public void selectingConcertChip_listRemainsVisible() {
        activityRule.getScenario().onActivity(activity -> {
            activity.findViewById(R.id.chipConcert).performClick();
            assertEquals(View.VISIBLE, activity.findViewById(R.id.rvEvents).getVisibility());
        });
    }

    @Test
    public void selectingMovieChip_listRemainsVisible() {
        activityRule.getScenario().onActivity(activity -> {
            activity.findViewById(R.id.chipMovie).performClick();
            assertEquals(View.VISIBLE, activity.findViewById(R.id.rvEvents).getVisibility());
        });
    }

    @Test
    public void selectingSportChip_listRemainsVisible() {
        activityRule.getScenario().onActivity(activity -> {
            activity.findViewById(R.id.chipSport).performClick();
            assertEquals(View.VISIBLE, activity.findViewById(R.id.rvEvents).getVisibility());
        });
    }

    @Test
    public void selectingTravelChip_listRemainsVisible() {
        activityRule.getScenario().onActivity(activity -> {
            activity.findViewById(R.id.chipTravel).performClick();
            assertEquals(View.VISIBLE, activity.findViewById(R.id.rvEvents).getVisibility());
        });
    }

    // ── Location filter interaction ───────────────────────────────────────────

    @Test
    public void locationFilterAcceptsInput_listRemainsVisible() {
        activityRule.getScenario().onActivity(activity -> {
            ((EditText) activity.findViewById(R.id.etLocationFilter)).setText("Montreal");
            assertEquals("Montreal", ((EditText) activity.findViewById(R.id.etLocationFilter)).getText().toString());
            assertEquals(View.VISIBLE, activity.findViewById(R.id.rvEvents).getVisibility());
        });
    }

    @Test
    public void clearingLocationFilter_listRemainsVisible() {
        activityRule.getScenario().onActivity(activity -> {
            ((EditText) activity.findViewById(R.id.etLocationFilter)).setText("Montreal");
            ((EditText) activity.findViewById(R.id.etLocationFilter)).setText("");
            assertEquals(View.VISIBLE, activity.findViewById(R.id.rvEvents).getVisibility());
        });
    }

    // ── Combined filter ───────────────────────────────────────────────────────

    @Test
    public void combinedCategoryAndLocationFilter_listRemainsVisible() {
        activityRule.getScenario().onActivity(activity -> {
            activity.findViewById(R.id.chipConcert).performClick();
            ((EditText) activity.findViewById(R.id.etLocationFilter)).setText("Montreal");
            assertEquals(View.VISIBLE, activity.findViewById(R.id.rvEvents).getVisibility());
        });
    }
}
