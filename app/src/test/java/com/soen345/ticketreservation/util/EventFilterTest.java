package com.soen345.ticketreservation.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.soen345.ticketreservation.model.Event;
import com.soen345.ticketreservation.model.EventCategory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

class EventFilterTest {

    private List<Event> eventList;
    private Calendar today;
    private Calendar tomorrow;

    @BeforeEach
    void setUp() {
        eventList = new ArrayList<>();
        today = Calendar.getInstance();
        tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DAY_OF_YEAR, 1);

        eventList.add(new Event("1", "Rock Concert", EventCategory.CONCERT.toString(), today.getTime(), "Montreal", 100));
        eventList.add(new Event("2", "Jazz Night", EventCategory.CONCERT.toString(), tomorrow.getTime(), "Toronto", 50));
        eventList.add(new Event("3", "Action Movie", EventCategory.MOVIE.toString(), today.getTime(), "Montreal", 200));
        eventList.add(new Event("4", "Soccer Game", EventCategory.SPORT.toString(), tomorrow.getTime(), "Vancouver", 500));
    }

    @Test
    void filter_noFilters_returnsAllEvents() {
        List<Event> result = EventFilter.filter(eventList, "", "", null);
        assertEquals(4, result.size());
    }

    @Test
    void filter_byCategory_returnsMatchingEvents() {
        List<Event> result = EventFilter.filter(eventList, EventCategory.CONCERT.toString(), "", null);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(e -> e.getCategory().equals(EventCategory.CONCERT.toString())));
    }

    @Test
    void filter_byLocation_returnsMatchingEvents() {
        List<Event> result = EventFilter.filter(eventList, "", "Montreal", null);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(e -> e.getLocation().equals("Montreal")));
    }

    @Test
    void filter_byLocation_caseInsensitiveAndPartialMatch() {
        List<Event> result = EventFilter.filter(eventList, "", "mont", null);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(e -> e.getLocation().equalsIgnoreCase("Montreal")));
    }

    @Test
    void filter_byDate_returnsMatchingEvents() {
        List<Event> result = EventFilter.filter(eventList, "", "", today);
        assertEquals(2, result.size());
        // Verify both events are from "today"
        for (Event e : result) {
            Calendar eventCal = Calendar.getInstance();
            eventCal.setTime(e.getDateTime());
            assertEquals(today.get(Calendar.YEAR), eventCal.get(Calendar.YEAR));
            assertEquals(today.get(Calendar.DAY_OF_YEAR), eventCal.get(Calendar.DAY_OF_YEAR));
        }
    }

    @Test
    void filter_byCategoryAndLocation_returnsMatchingEvents() {
        List<Event> result = EventFilter.filter(eventList, EventCategory.CONCERT.toString(), "Toronto", null);
        assertEquals(1, result.size());
        assertEquals("Jazz Night", result.get(0).getName());
    }

    @Test
    void filter_allCriteria_returnsMatchingEvents() {
        List<Event> result = EventFilter.filter(eventList, EventCategory.MOVIE.toString(), "Montreal", today);
        assertEquals(1, result.size());
        assertEquals("Action Movie", result.get(0).getName());
    }

    @Test
    void filter_noMatches_returnsEmptyList() {
        List<Event> result = EventFilter.filter(eventList, EventCategory.TRAVEL.toString(), "Quebec", null);
        assertTrue(result.isEmpty());
    }
}
