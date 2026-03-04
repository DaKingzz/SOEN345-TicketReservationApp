package com.soen345.ticketreservation.activity;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.espresso.NoMatchingViewException;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.soen345.ticketreservation.R;
import com.soen345.ticketreservation.auth.LoginActivity;
import com.soen345.ticketreservation.util.AuthTestUtils;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class EventListingActivityTest {

    @Rule
    public ActivityScenarioRule<LoginActivity> activityRule =
            new ActivityScenarioRule<>(LoginActivity.class);


    private void loginAndWaitForEventList() throws InterruptedException {
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


    @Test
    public void testEventListIsDisplayed() throws InterruptedException {
        loginAndWaitForEventList();
        onView(withId(R.id.rvEvents)).check(matches(isDisplayed()));
    }

    @Test
    public void testCategoryChipsAreDisplayed() throws InterruptedException {
        loginAndWaitForEventList();
        onView(withId(R.id.chipGroupFilters)).check(matches(isDisplayed()));
        onView(withId(R.id.chipConcert)).check(matches(isDisplayed()));
        onView(withId(R.id.chipMovie)).check(matches(isDisplayed()));
        onView(withId(R.id.chipSport)).check(matches(isDisplayed()));
        onView(withId(R.id.chipTravel)).check(matches(isDisplayed()));
    }

    @Test
    public void testConcertChipIsClickable() throws InterruptedException {
        loginAndWaitForEventList();
        onView(withId(R.id.chipConcert)).perform(click());
        onView(withId(R.id.chipConcert)).check(matches(isDisplayed()));
        onView(withId(R.id.rvEvents)).check(matches(isDisplayed()));
    }

    @Test
    public void testMovieChipIsClickable() throws InterruptedException {
        loginAndWaitForEventList();
        onView(withId(R.id.chipMovie)).perform(click());
        onView(withId(R.id.chipMovie)).check(matches(isDisplayed()));
        onView(withId(R.id.rvEvents)).check(matches(isDisplayed()));
    }

    @Test
    public void testSportChipIsClickable() throws InterruptedException {
        loginAndWaitForEventList();
        onView(withId(R.id.chipSport)).perform(click());
        onView(withId(R.id.chipSport)).check(matches(isDisplayed()));
        onView(withId(R.id.rvEvents)).check(matches(isDisplayed()));
    }

    @Test
    public void testTravelChipIsClickable() throws InterruptedException {
        loginAndWaitForEventList();
        onView(withId(R.id.chipTravel)).perform(click());
        onView(withId(R.id.chipTravel)).check(matches(isDisplayed()));
        onView(withId(R.id.rvEvents)).check(matches(isDisplayed()));
    }

    @Test
    public void testLocationFilterIsDisplayedAndAcceptsInput() throws InterruptedException {
        loginAndWaitForEventList();
        onView(withId(R.id.etLocationFilter)).check(matches(isDisplayed()));
        onView(withId(R.id.etLocationFilter)).check(matches(isEnabled()));
        onView(withId(R.id.etLocationFilter))
                .perform(replaceText("Montreal"), closeSoftKeyboard());
        onView(withId(R.id.rvEvents)).check(matches(isDisplayed()));
    }

    @Test
    public void testLocationFilterClearsAndListStillShows() throws InterruptedException {
        loginAndWaitForEventList();
        onView(withId(R.id.etLocationFilter))
                .perform(replaceText("Montreal"), closeSoftKeyboard());
        onView(withId(R.id.etLocationFilter))
                .perform(replaceText(""), closeSoftKeyboard());
        onView(withId(R.id.rvEvents)).check(matches(isDisplayed()));
    }

    @Test
    public void testDateFilterButtonIsDisplayedAndClickable() throws InterruptedException {
        loginAndWaitForEventList();
        onView(withId(R.id.btnDateFilter)).check(matches(isDisplayed()));
        onView(withId(R.id.btnDateFilter)).check(matches(isEnabled()));
        onView(withId(R.id.btnDateFilter)).perform(click());
        onView(withText("Cancel")).perform(click());
        onView(withId(R.id.rvEvents)).check(matches(isDisplayed()));
    }

    @Test
    public void testCombinedCategoryAndLocationFilter() throws InterruptedException {
        loginAndWaitForEventList();
        onView(withId(R.id.chipConcert)).perform(click());
        onView(withId(R.id.etLocationFilter))
                .perform(replaceText("Montreal"), closeSoftKeyboard());
        onView(withId(R.id.rvEvents)).check(matches(isDisplayed()));
    }
}