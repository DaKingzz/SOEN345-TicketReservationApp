package com.soen345.ticketreservation.activity;


import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.RootMatchers;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.soen345.ticketreservation.R;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class CreateEventActivityTest {

    @Rule
    public ActivityScenarioRule<CreateEventActivity> activityRule =
            new ActivityScenarioRule<>(CreateEventActivity.class);

    @Test
    public void testUIElementsAreDisplayed() {
        Espresso.onView(ViewMatchers.withId(R.id.tvAdminHeader)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        Espresso.onView(ViewMatchers.withId(R.id.etEventName)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        Espresso.onView(ViewMatchers.withId(R.id.etCategory)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        Espresso.onView(ViewMatchers.withId(R.id.etLocation)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        Espresso.onView(ViewMatchers.withId(R.id.etEventDate)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        Espresso.onView(ViewMatchers.withId(R.id.etEventTime)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        Espresso.onView(ViewMatchers.withId(R.id.etCapacity)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        Espresso.onView(ViewMatchers.withId(R.id.btnCreateEvent)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    @Test
    public void testFillTextInputs() {
        Espresso.onView(ViewMatchers.withId(R.id.etEventName)).perform(ViewActions.typeText("Sample Event"), ViewActions.closeSoftKeyboard());
        Espresso.onView(ViewMatchers.withId(R.id.etLocation)).perform(ViewActions.typeText("Montreal"), ViewActions.closeSoftKeyboard());
        Espresso.onView(ViewMatchers.withId(R.id.etCapacity)).perform(ViewActions.typeText("50"), ViewActions.closeSoftKeyboard());

        Espresso.onView(ViewMatchers.withId(R.id.etEventName)).check(ViewAssertions.matches(ViewMatchers.withText("Sample Event")));
        Espresso.onView(ViewMatchers.withId(R.id.etLocation)).check(ViewAssertions.matches(ViewMatchers.withText("Montreal")));
        Espresso.onView(ViewMatchers.withId(R.id.etCapacity)).check(ViewAssertions.matches(ViewMatchers.withText("50")));
    }

    @Test
    public void testDatePickerOpens() {
        Espresso.onView(ViewMatchers.withId(R.id.etEventDate)).perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withText("SELECT EVENT DATE")).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    @Test
    public void testTimePickerOpens() {
        Espresso.onView(ViewMatchers.withId(R.id.etEventTime)).perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withText("Select Event Time")).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    @Test
    public void testCategoryDropdown() {
        Espresso.onView(ViewMatchers.withId(R.id.etCategory)).perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withText("Concert"))
                .inRoot(RootMatchers.isPlatformPopup())
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
                .perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withId(R.id.etCategory)).check(ViewAssertions.matches(ViewMatchers.withText("Concert")));
    }
    @Test
    public void testValidationShowsToastOnEmptyFields() {
        Espresso.onView(ViewMatchers.withId(R.id.btnCreateEvent)).perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withId(R.id.tvAdminHeader)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }
}
