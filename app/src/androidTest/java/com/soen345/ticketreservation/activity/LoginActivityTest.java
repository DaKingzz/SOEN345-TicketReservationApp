package com.soen345.ticketreservation.activity;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.espresso.NoMatchingViewException;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.soen345.ticketreservation.R;
import com.soen345.ticketreservation.auth.LoginActivity;
import com.soen345.ticketreservation.util.AuthTestUtils;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class LoginActivityTest {

    @Rule
    public ActivityScenarioRule<LoginActivity> activityRule =
            new ActivityScenarioRule<>(LoginActivity.class);

    @Test
    public void testSuccessfulLogin() throws InterruptedException {
        AuthTestUtils.login("robertlouissoccer@outlook.com", "soccer");

        long timeout = System.currentTimeMillis() + 10_000;
        while (System.currentTimeMillis() < timeout) {
            try {
                onView(withId(R.id.rvEvents)).check(matches(isDisplayed()));
                return;
            } catch (NoMatchingViewException | AssertionError e) {
                Thread.sleep(500);
            }
        }

        onView(withId(R.id.rvEvents)).check(matches(isDisplayed()));
    }
}